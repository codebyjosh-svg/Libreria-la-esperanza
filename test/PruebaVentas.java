import java.lang.reflect.*;
import java.math.BigDecimal;
import java.sql.*;
import org.esperanza.Dao.*;
import org.esperanza.model.*;


/** Pruebas sin dependencias externas; JDBC simulado, sin MySQL. */
public class PruebaVentas {
    static void verificar(boolean valor) {
        if (!valor) throw new AssertionError();
    }
    static class BaseSimulada {
        boolean commit, rollback, cerrada;
        int insertados;
        boolean fallarDetalle;
        @SuppressWarnings("unchecked")
        static <T> T proxy(Class<T> tipo, InvocationHandler handler) {
            return (T) Proxy.newProxyInstance(tipo.getClassLoader(), new Class<?>[]{tipo}, handler);
        }
        Connection conectar() {
            return proxy(Connection.class, (obj, metodo, args) -> {
                switch (metodo.getName()) {
                    case "setAutoCommit": return null;
                    case "commit": commit = true; return null;
                    case "rollback": rollback = true; return null;
                    case "close": cerrada = true; return null;
                    case "prepareStatement":
                        String sql = (String) args[0];
                        return proxy(PreparedStatement.class, (p, m, a) -> {
                            if (m.getName().startsWith("set") || m.getName().equals("close")) return null;
                            if (m.getName().equals("executeUpdate")) {
                                if (fallarDetalle && sql.contains("detalle_venta")) throw new SQLException("Fallo simulado");
                                insertados++;
                                return 1;
                            }
                            if (m.getName().equals("getGeneratedKeys")) {
                                boolean[] leido = {false};
                                return proxy(ResultSet.class, (r, rm, ra) -> {
                                    if (rm.getName().equals("next")) {
                                        boolean resultado = !leido[0]; leido[0] = true; return resultado;
                                    }
                                    if (rm.getName().equals("getInt")) return 42;
                                    if (rm.getName().equals("close")) return null;
                                    throw new UnsupportedOperationException(rm.getName());
                                });
                            }
                            throw new UnsupportedOperationException(m.getName());
                        });
                    default: throw new UnsupportedOperationException(metodo.getName());
                }
            });
        }
    }
    public static void main(String[] args) throws Exception {
        CarritoVenta carrito = new CarritoVenta();
        carrito.agregarProducto(5, 2, new BigDecimal("25.50"));
        carrito.agregarProducto(5, 1, new BigDecimal("25.50"));
        verificar(carrito.getTotal().compareTo(new BigDecimal("76.50")) == 0);
        carrito.getDetalles().get(0).setCantidad(100);
        verificar(carrito.getDetalles().get(0).getCantidad() == 3);
        carrito.cambiarCantidad(5, 2);
        carrito.agregarProducto(6, 3, new BigDecimal("0.10"));
        verificar(carrito.getTotal().compareTo(new BigDecimal("51.30")) == 0);
        try { carrito.cambiarCantidad(5, 0); throw new AssertionError(); }
        catch (IllegalArgumentException esperado) { }
        try { carrito.agregarProducto(5, 1, new BigDecimal("26")); throw new AssertionError(); }
        catch (IllegalArgumentException esperado) { }
        verificar(carrito.quitarProducto(6));
        verificar(carrito.getTotal().compareTo(new BigDecimal("51.00")) == 0);
        BaseSimulada fallo = new BaseSimulada();
        fallo.fallarDetalle = true;
        try { carrito.confirmarVenta(1, 1, new VentaDao(fallo::conectar)); throw new AssertionError(); }
        catch (SQLException esperado) { }
        verificar(fallo.rollback && !fallo.commit && fallo.cerrada && !carrito.estaVacio());
        carrito.agregarProducto(6, 3, new BigDecimal("0.10"));
        BaseSimulada exito = new BaseSimulada();
        Venta venta = carrito.confirmarVenta(1, 1, new VentaDao(exito::conectar));
        verificar(exito.commit && !exito.rollback && exito.cerrada && exito.insertados == 3);
        verificar(venta.getIdVenta() == 42 && venta.getTotal().compareTo(new BigDecimal("51.30")) == 0);
        verificar(carrito.estaVacio());
        verificar(carrito.getTotal().compareTo(BigDecimal.ZERO) == 0);
        carrito.agregarProducto(9, 1, new BigDecimal("10.00"));
        carrito.vaciar();
        verificar(carrito.getDetalles().isEmpty() && carrito.getTotal().signum() == 0);
        try { carrito.confirmarVenta(1, 1, new VentaDao(exito::conectar)); throw new AssertionError(); }
        catch (IllegalArgumentException esperado) { }
        System.out.println("OK: calculos, cantidades, copias, retiro, commit, rollback y conservacion del carrito.");
    }
}


