import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.esperanza.dao.VentaDao;
import org.esperanza.model.CarritoVenta;
import org.esperanza.model.Venta;

public class PruebaVentas {

    static void verificar(boolean condicion, String mensaje) {
        if (!condicion) throw new AssertionError(mensaje);
    }

    static class BaseSimulada {

        boolean commit;
        boolean rollback;
        boolean cerrada;
        boolean fallarDetalle;
        boolean fallarStock;

        int insertados;
        int actualizacionesStock;
        int stockDisponible = 100;

        @SuppressWarnings("unchecked")
        static <T> T proxy(Class<T> tipo, InvocationHandler handler) {
            return (T) Proxy.newProxyInstance(
                    tipo.getClassLoader(),
                    new Class<?>[]{tipo},
                    handler
            );
        }

        Connection conectar() {
            return proxy(Connection.class, (obj, metodo, args) -> {
                return switch (metodo.getName()) {
                    case "setAutoCommit" -> null;
                    case "commit" -> {
                        commit = true;
                        yield null;
                    }
                    case "rollback" -> {
                        rollback = true;
                        yield null;
                    }
                    case "close" -> {
                        cerrada = true;
                        yield null;
                    }
                    case "isClosed" -> cerrada;
                    case "prepareStatement" -> crearPreparedStatement((String) args[0]);
                    default -> throw new UnsupportedOperationException(
                            "Metodo Connection no simulado: " + metodo.getName()
                    );
                };
            });
        }

        private PreparedStatement crearPreparedStatement(String sql) {
            return proxy(PreparedStatement.class, (obj, metodo, args) -> {
                String nombre = metodo.getName();

                if (nombre.startsWith("set") || nombre.equals("close")) return null;

                if (nombre.equals("executeQuery")) {
                    return crearResultadoStock();
                }

                if (nombre.equals("executeUpdate")) {
                    if (fallarDetalle && sql.contains("detalle_venta")) {
                        throw new SQLException("Fallo simulado al insertar detalle");
                    }

                    if (sql.contains("UPDATE libros")) {
                        if (fallarStock) {
                            throw new SQLException("Fallo simulado al actualizar stock");
                        }

                        actualizacionesStock++;
                        return 1;
                    }

                    insertados++;
                    return 1;
                }

                if (nombre.equals("getGeneratedKeys")) {
                    return crearGeneratedKeys();
                }

                throw new UnsupportedOperationException(
                        "Metodo PreparedStatement no simulado: " + nombre
                );
            });
        }

        private ResultSet crearResultadoStock() {
            boolean[] leido = {false};

            return proxy(ResultSet.class, (obj, metodo, args) -> {
                return switch (metodo.getName()) {
                    case "next" -> {
                        if (!leido[0]) {
                            leido[0] = true;
                            yield true;
                        }
                        yield false;
                    }
                    case "getInt" -> stockDisponible;
                    case "getString" -> "Libro simulado";
                    case "close" -> null;
                    default -> throw new UnsupportedOperationException(
                            "Metodo ResultSet no simulado: " + metodo.getName()
                    );
                };
            });
        }

        private ResultSet crearGeneratedKeys() {
            boolean[] leido = {false};

            return proxy(ResultSet.class, (obj, metodo, args) -> {
                return switch (metodo.getName()) {
                    case "next" -> {
                        if (!leido[0]) {
                            leido[0] = true;
                            yield true;
                        }
                        yield false;
                    }
                    case "getInt" -> 42;
                    case "close" -> null;
                    default -> throw new UnsupportedOperationException(
                            "Metodo ResultSet no simulado: " + metodo.getName()
                    );
                };
            });
        }
    }

    static CarritoVenta crearCarrito() {
        CarritoVenta carrito = new CarritoVenta();
        carrito.agregarProducto("978-0-124", 2, new BigDecimal("25.50"));
        carrito.agregarProducto("978-0-126", 1, new BigDecimal("10.00"));
        return carrito;
    }

    static void probarVentaExitosa() throws Exception {
        CarritoVenta carrito = crearCarrito();
        BaseSimulada base = new BaseSimulada();

        Venta venta = carrito.confirmarVenta(
                1L,
                1,
                new VentaDao(base::conectar)
        );

        verificar(base.commit, "La venta debio hacer commit");
        verificar(!base.rollback, "No debio hacer rollback");
        verificar(base.cerrada, "La conexion debio cerrarse");
        verificar(base.insertados == 3, "Debieron realizarse 3 inserts");
        verificar(base.actualizacionesStock == 2, "Debieron actualizarse 2 stocks");
        verificar(venta.getIdVenta() == 42, "ID de venta incorrecto");
        verificar(venta.getTotal().compareTo(new BigDecimal("61.00")) == 0,
                "Total incorrecto");
        verificar(carrito.estaVacio(), "El carrito debio vaciarse");

        System.out.println("OK - Venta correcta");
    }

    static void probarStockInsuficiente() throws Exception {
        CarritoVenta carrito = new CarritoVenta();
        carrito.agregarProducto("978-0-124", 5, new BigDecimal("25.50"));

        BaseSimulada base = new BaseSimulada();
        base.stockDisponible = 2;

        try {
            carrito.confirmarVenta(1L, 1, new VentaDao(base::conectar));
            throw new AssertionError("Debio rechazar la venta por falta de stock");
        } catch (IllegalArgumentException esperado) {
            verificar(base.rollback, "Debio hacer rollback");
            verificar(!base.commit, "No debio hacer commit");
            verificar(base.insertados == 0, "No debio insertar registros");
            verificar(!carrito.estaVacio(), "El carrito debe conservarse");

            System.out.println("OK - Stock insuficiente rechazado");
        }
    }

    static void probarFalloDetalle() throws Exception {
        CarritoVenta carrito = crearCarrito();

        BaseSimulada base = new BaseSimulada();
        base.fallarDetalle = true;

        try {
            carrito.confirmarVenta(1L, 1, new VentaDao(base::conectar));
            throw new AssertionError("La venta debio fallar");
        } catch (SQLException esperado) {
            verificar(base.rollback, "Debio hacer rollback");
            verificar(!base.commit, "No debio hacer commit");
            verificar(!carrito.estaVacio(), "El carrito debe conservarse");

            System.out.println("OK - Rollback por fallo de detalle");
        }
    }

    static void probarFalloStock() throws Exception {
        CarritoVenta carrito = new CarritoVenta();
        carrito.agregarProducto("978-0-127", 1, new BigDecimal("165.00"));

        BaseSimulada base = new BaseSimulada();
        base.fallarStock = true;

        try {
            carrito.confirmarVenta(1L, 1, new VentaDao(base::conectar));
            throw new AssertionError("La venta debio fallar");
        } catch (SQLException esperado) {
            verificar(base.rollback, "Debio hacer rollback");
            verificar(!base.commit, "No debio hacer commit");
            verificar(base.insertados == 2, "Venta y detalle debieron intentarse");
            verificar(!carrito.estaVacio(), "El carrito debe conservarse");

            System.out.println("OK - Rollback por fallo de stock");
        }
    }

    static void probarVentaVacia() throws Exception {
        CarritoVenta carrito = new CarritoVenta();
        BaseSimulada base = new BaseSimulada();

        try {
            carrito.confirmarVenta(1L, 1, new VentaDao(base::conectar));
            throw new AssertionError("No debe permitir una venta vacia");
        } catch (IllegalArgumentException esperado) {
            verificar(!base.commit, "No debe existir commit");
            System.out.println("OK - Venta vacia rechazada");
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("==============================");
        System.out.println("T2.21 - PRUEBAS DE VENTA");
        System.out.println("==============================");

        probarVentaExitosa();
        probarStockInsuficiente();
        probarFalloDetalle();
        probarFalloStock();
        probarVentaVacia();

        System.out.println("==============================");
        System.out.println("TODAS LAS PRUEBAS PASARON");
        System.out.println("==============================");
    }
}