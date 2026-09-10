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

    static void verificar(boolean valor) {

        if (!valor) {

            throw new AssertionError(
                    "Una de las pruebas no obtuvo "
                    + "el resultado esperado."
            );
        }
    }

    static class BaseSimulada {

        boolean commit;
        boolean rollback;
        boolean cerrada;

        int insertados;
        int actualizacionesStock;

        boolean fallarDetalle;
        boolean fallarStock;

        int stockDisponible = 100;

        @SuppressWarnings("unchecked")
        static <T> T proxy(
                Class<T> tipo,
                InvocationHandler handler) {

            return (T) Proxy.newProxyInstance(
                    tipo.getClassLoader(),
                    new Class<?>[]{tipo},
                    handler
            );
        }

        Connection conectar() {

            return proxy(
                    Connection.class,
                    (objeto, metodo, argumentos) -> {

                        switch (metodo.getName()) {

                            case "setAutoCommit":
                                return null;

                            case "commit":

                                commit = true;

                                return null;

                            case "rollback":

                                rollback = true;

                                return null;

                            case "close":

                                cerrada = true;

                                return null;

                            case "isClosed":

                                return cerrada;

                            case "prepareStatement":

                                String sql =
                                        (String) argumentos[0];

                                return crearPreparedStatement(
                                        sql
                                );

                            default:

                                throw new UnsupportedOperationException(
                                        "Metodo Connection no simulado: "
                                        + metodo.getName()
                                );
                        }
                    }
            );
        }

        private PreparedStatement crearPreparedStatement(
                String sql) {

            return proxy(
                    PreparedStatement.class,
                    (objeto, metodo, argumentos) -> {

                        String nombreMetodo =
                                metodo.getName();

                        if (nombreMetodo.startsWith("set")) {

                            return null;
                        }

                        if (nombreMetodo.equals("close")) {

                            return null;
                        }

                        if (nombreMetodo.equals("executeQuery")) {

                            return crearResultadoStock();
                        }

                        if (nombreMetodo.equals("executeUpdate")) {

                            if (fallarDetalle
                                    && sql.contains(
                                            "detalle_venta"
                                    )) {

                                throw new SQLException(
                                        "Fallo simulado "
                                        + "al insertar detalle"
                                );
                            }

                            if (sql.contains("UPDATE libros")) {

                                if (fallarStock) {

                                    throw new SQLException(
                                            "Fallo simulado "
                                            + "al actualizar stock"
                                    );
                                }

                                actualizacionesStock++;

                                return 1;
                            }

                            insertados++;

                            return 1;
                        }

                        if (nombreMetodo.equals(
                                "getGeneratedKeys"
                        )) {

                            return crearGeneratedKeys();
                        }

                        throw new UnsupportedOperationException(
                                "Metodo PreparedStatement "
                                + "no simulado: "
                                + nombreMetodo
                        );
                    }
            );
        }

        private ResultSet crearResultadoStock() {

            boolean[] leido = {false};

            return proxy(
                    ResultSet.class,
                    (objeto, metodo, argumentos) -> {

                        switch (metodo.getName()) {

                            case "next":

                                if (!leido[0]) {

                                    leido[0] = true;

                                    return true;
                                }

                                return false;

                            case "getInt":

                                return stockDisponible;

                            case "getString":

                                return "Libro simulado";

                            case "close":

                                return null;

                            default:

                                throw new UnsupportedOperationException(
                                        "Metodo ResultSet "
                                        + "no simulado: "
                                        + metodo.getName()
                                );
                        }
                    }
            );
        }

        private ResultSet crearGeneratedKeys() {

            boolean[] leido = {false};

            return proxy(
                    ResultSet.class,
                    (objeto, metodo, argumentos) -> {

                        switch (metodo.getName()) {

                            case "next":

                                if (!leido[0]) {

                                    leido[0] = true;

                                    return true;
                                }

                                return false;

                            case "getInt":

                                return 42;

                            case "close":

                                return null;

                            default:

                                throw new UnsupportedOperationException(
                                        "Metodo ResultSet "
                                        + "no simulado: "
                                        + metodo.getName()
                                );
                        }
                    }
            );
        }
    }

    public static void main(
            String[] args) throws Exception {

        System.out.println(
                "=================================="
        );

        System.out.println(
                "PRUEBAS DE VENTA"
        );

        System.out.println(
                "=================================="
        );

        CarritoVenta carrito =
                new CarritoVenta();

        carrito.agregarProducto(
                "978-0-124",
                2,
                new BigDecimal("25.50")
        );

        carrito.agregarProducto(
                "978-0-124",
                1,
                new BigDecimal("25.50")
        );

        verificar(
                carrito
                        .getTotal()
                        .compareTo(
                                new BigDecimal("76.50")
                        ) == 0
        );

        verificar(
                carrito
                        .getDetalles()
                        .get(0)
                        .getCantidad() == 3
        );

        System.out.println(
                "OK - Agregar y sumar productos"
        );

        carrito
                .getDetalles()
                .get(0)
                .setCantidad(100);

        verificar(
                carrito
                        .getDetalles()
                        .get(0)
                        .getCantidad() == 3
        );

        System.out.println(
                "OK - Copia protegida del carrito"
        );

        carrito.cambiarCantidad(
                "978-0-124",
                2
        );

        carrito.agregarProducto(
                "978-0-126",
                3,
                new BigDecimal("0.10")
        );

        verificar(
                carrito
                        .getTotal()
                        .compareTo(
                                new BigDecimal("51.30")
                        ) == 0
        );

        System.out.println(
                "OK - Cambio de cantidad"
        );

        try {

            carrito.cambiarCantidad(
                    "978-0-124",
                    0
            );

            throw new AssertionError(
                    "Debio rechazar cantidad 0"
            );

        } catch (IllegalArgumentException esperado) {

            System.out.println(
                    "OK - Cantidad invalida rechazada"
            );
        }

        try {

            carrito.agregarProducto(
                    "978-0-124",
                    1,
                    new BigDecimal("26.00")
            );

            throw new AssertionError(
                    "Debio rechazar precio diferente"
            );

        } catch (IllegalArgumentException esperado) {

            System.out.println(
                    "OK - Precio diferente rechazado"
            );
        }

        verificar(
                carrito.quitarProducto(
                        "978-0-126"
                )
        );

        verificar(
                carrito
                        .getTotal()
                        .compareTo(
                                new BigDecimal("51.00")
                        ) == 0
        );

        System.out.println(
                "OK - Eliminar producto"
        );

        BaseSimulada falloDetalle =
                new BaseSimulada();

        falloDetalle.fallarDetalle =
                true;

        VentaDao ventaDaoFalloDetalle =
                new VentaDao(
                        () -> falloDetalle.conectar()
                );

        try {

            carrito.confirmarVenta(
                    1L,
                    1,
                    ventaDaoFalloDetalle
            );

            throw new AssertionError(
                    "La venta debio fallar"
            );

        } catch (SQLException esperado) {

            verificar(
                    falloDetalle.rollback
            );

            verificar(
                    !falloDetalle.commit
            );

            verificar(
                    falloDetalle.cerrada
            );

            verificar(
                    !carrito.estaVacio()
            );

            System.out.println(
                    "OK - Rollback por fallo de detalle"
            );
        }

        CarritoVenta carritoRollbackStock =
                new CarritoVenta();

        carritoRollbackStock.agregarProducto(
                "978-0-127",
                1,
                new BigDecimal("165.00")
        );

        BaseSimulada falloStock =
                new BaseSimulada();

        falloStock.fallarStock =
                true;

        VentaDao ventaDaoFalloStock =
                new VentaDao(
                        () -> falloStock.conectar()
                );

        try {

            carritoRollbackStock.confirmarVenta(
                    1L,
                    1,
                    ventaDaoFalloStock
            );

            throw new AssertionError(
                    "La venta debio hacer rollback"
            );

        } catch (SQLException esperado) {

            verificar(
                    falloStock.rollback
            );

            verificar(
                    !falloStock.commit
            );

            verificar(
                    falloStock.cerrada
            );

            verificar(
                    falloStock.insertados == 2
            );

            verificar(
                    !carritoRollbackStock.estaVacio()
            );

            System.out.println(
                    "OK - T2.20 rollback "
                    + "por fallo al actualizar stock"
            );
        }

        carrito.agregarProducto(
                "978-0-126",
                3,
                new BigDecimal("0.10")
        );

        BaseSimulada exito =
                new BaseSimulada();

        VentaDao ventaDaoExito =
                new VentaDao(
                        () -> exito.conectar()
                );

        Venta venta =
                carrito.confirmarVenta(
                        1L,
                        1,
                        ventaDaoExito
                );

        verificar(
                exito.commit
        );

        verificar(
                !exito.rollback
        );

        verificar(
                exito.cerrada
        );

        verificar(
                exito.insertados == 3
        );

        verificar(
                exito.actualizacionesStock == 2
        );

        verificar(
                venta.getIdVenta() == 42
        );

        verificar(
                venta
                        .getTotal()
                        .compareTo(
                                new BigDecimal("51.30")
                        ) == 0
        );

        verificar(
                carrito.estaVacio()
        );

        verificar(
                carrito
                        .getTotal()
                        .compareTo(
                                BigDecimal.ZERO
                        ) == 0
        );

        System.out.println(
                "OK - Venta, detalles y stock "
                + "confirmados con commit"
        );

        carrito.agregarProducto(
                "978-0-127",
                1,
                new BigDecimal("10.00")
        );

        carrito.vaciar();

        verificar(
                carrito
                        .getDetalles()
                        .isEmpty()
        );

        verificar(
                carrito
                        .getTotal()
                        .signum() == 0
        );

        System.out.println(
                "OK - Vaciar carrito"
        );

        try {

            carrito.confirmarVenta(
                    1L,
                    1,
                    ventaDaoExito
            );

            throw new AssertionError(
                    "No debe permitir una venta vacia"
            );

        } catch (IllegalArgumentException esperado) {

            System.out.println(
                    "OK - Venta vacia rechazada"
            );
        }

        System.out.println(
                "=================================="
        );

        System.out.println(
                "TODAS LAS PRUEBAS PASARON"
        );

        System.out.println(
                "=================================="
        );

        System.out.println(
                "OK: T2.17, T2.18, T2.19 "
                + "y T2.20 funcionando."
        );
    }
}