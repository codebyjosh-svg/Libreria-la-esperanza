import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.*;
import org.esperanza.Model.SalidaInventario;
import org.esperanza.Model.TipoSalida;
import org.esperanza.dao.SalidaInventarioDao;

/** Pruebas deterministas con un doble JDBC; no modifican una base real. */
public class PruebaSalidaInventario {
    public static void main(String[] args) throws Exception {
        for (TipoSalida tipo : TipoSalida.values()) {
            Base base = new Base();
            new SalidaInventarioDao(base::conexion).registrar(salida(tipo, 3));
            exigir(base.stock == 7 && base.movimientos == 1 && base.commit, "Salida " + tipo);
            exigir(tipo.name().equals(base.tipo) && "Motivo de prueba".equals(base.motivo)
                    && base.usuario == 1, "Auditoría " + tipo);
        }
        Base excesiva = new Base();
        esperar(IllegalArgumentException.class,
                () -> new SalidaInventarioDao(excesiva::conexion).registrar(salida(TipoSalida.MERMA, 11)));
        exigir(excesiva.stock == 10 && excesiva.movimientos == 0 && excesiva.rollback, "Stock insuficiente");

        Base fallo = new Base();
        fallo.fallarInsert = true;
        esperar(SQLException.class,
                () -> new SalidaInventarioDao(fallo::conexion).registrar(salida(TipoSalida.MERMA, 3)));
        exigir(fallo.stock == 10 && fallo.movimientos == 0 && fallo.rollback && !fallo.commit,
                "Rollback si falla el movimiento");

        Base exacta = new Base();
        new SalidaInventarioDao(exacta::conexion).registrar(salida(TipoSalida.TRASLADO, 10));
        exigir(exacta.stock == 0 && exacta.movimientos == 1, "Salida de todo el stock");

        Base ausente = new Base();
        ausente.existe = false;
        esperar(IllegalArgumentException.class,
                () -> new SalidaInventarioDao(ausente::conexion).registrar(salida(TipoSalida.MERMA, 1)));
        exigir(ausente.stock == 10 && ausente.movimientos == 0, "Libro inexistente/inactivo");

        Base sinTransaccion = new Base();
        sinTransaccion.engine = "MyISAM";
        esperar(SQLException.class,
                () -> new SalidaInventarioDao(sinTransaccion::conexion).registrar(salida(TipoSalida.MERMA, 1)));
        exigir(sinTransaccion.stock == 10 && sinTransaccion.movimientos == 0, "Rechazo de MyISAM");

        for (int cantidad : new int[]{0, -1}) {
            esperar(IllegalArgumentException.class, () -> salida(TipoSalida.MERMA, cantidad));
        }
        esperar(IllegalArgumentException.class, () -> salida(null, 1));
        esperar(IllegalArgumentException.class, () -> new SalidaInventario(" ", TipoSalida.MERMA, 1, "Motivo", 1));
        esperar(IllegalArgumentException.class, () -> new SalidaInventario("ISBN", TipoSalida.MERMA, 1, " ", 1));
        esperar(IllegalArgumentException.class, () -> new SalidaInventario("ISBN", TipoSalida.MERMA, 1, "x".repeat(501), 1));
        esperar(IllegalArgumentException.class, () -> new SalidaInventario("ISBN", TipoSalida.MERMA, 1, "Motivo", 0));
        System.out.println("OK: merma, traslado, devolución, exceso, rollback, stock exacto, libro ausente, motor y campos inválidos.");
    }

    private static SalidaInventario salida(TipoSalida tipo, int cantidad) {
        return new SalidaInventario("978-PRUEBA", tipo, cantidad, "Motivo de prueba", 1);
    }
    private static void exigir(boolean condicion, String caso) {
        if (!condicion) throw new AssertionError(caso);
    }
    private interface Accion { void ejecutar() throws Exception; }
    private static void esperar(Class<? extends Exception> clase, Accion accion) throws Exception {
        try { accion.ejecutar(); }
        catch (Exception ex) { if (clase.isInstance(ex)) return; throw ex; }
        throw new AssertionError("Se esperaba " + clase.getSimpleName());
    }

    private static class Base {
        int stock = 10, movimientos, usuario;
        String tipo, motivo, engine = "InnoDB";
        boolean fallarInsert, commit, rollback, transaccion, bloqueado, existe = true;
        Connection conexion() {
            return proxy(Connection.class, (obj, metodo, args) -> {
                switch (metodo.getName()) {
                    case "prepareStatement": return preparar((String) args[0]);
                    case "setAutoCommit": transaccion = !(boolean) args[0]; return null;
                    case "commit": exigir(transaccion && movimientos == 1, "Commit después del movimiento"); commit = true; return null;
                    case "rollback": stock = 10; movimientos = 0; rollback = true; return null;
                    case "close": return null;
                    default: throw new UnsupportedOperationException(metodo.getName());
                }
            });
        }
        PreparedStatement preparar(String sql) {
            Map<Integer, Object> parametros = new HashMap<>();
            return proxy(PreparedStatement.class, (obj, metodo, args) -> {
                switch (metodo.getName()) {
                    case "setString": case "setInt": parametros.put((int) args[0], args[1]); return null;
                    case "close": return null;
                    case "executeQuery":
                        if (sql.contains("information_schema")) {
                            return filas(List.of(Map.of("ENGINE", engine), Map.of("ENGINE", engine)));
                        }
                        exigir(transaccion && sql.contains("FOR UPDATE"), "Bloqueo dentro de la transacción");
                        exigir("978-PRUEBA".equals(parametros.get(1)), "ISBN parametrizado");
                        bloqueado = true;
                        return filas(existe ? List.of(Map.of("titulo", "Libro", "stock_actual", stock)) : List.of());
                    case "executeUpdate":
                        exigir(transaccion && bloqueado, "Descuento después de bloquear");
                        if (sql.contains("UPDATE libros")) {
                            exigir(sql.contains("stock_actual >= ?"), "Descuento condicionado al stock");
                            stock -= (int) parametros.get(1);
                            return 1;
                        }
                        exigir(sql.contains("INSERT INTO movimientos_salida_inventario"), "Tabla del movimiento");
                        if (fallarInsert) throw new SQLException("Fallo simulado al insertar");
                        movimientos++;
                        tipo = (String) parametros.get(2);
                        motivo = (String) parametros.get(4);
                        usuario = (int) parametros.get(5);
                        return 1;
                    default: throw new UnsupportedOperationException(metodo.getName());
                }
            });
        }
        ResultSet filas(List<Map<String, Object>> filas) {
            int[] indice = {-1};
            return proxy(ResultSet.class, (obj, metodo, args) -> {
                switch (metodo.getName()) {
                    case "next": return ++indice[0] < filas.size();
                    case "getString": case "getInt": return filas.get(indice[0]).get(args[0]);
                    case "close": return null;
                    default: throw new UnsupportedOperationException(metodo.getName());
                }
            });
        }
        private static <T> T proxy(Class<T> tipo, java.lang.reflect.InvocationHandler handler) {
            return tipo.cast(Proxy.newProxyInstance(tipo.getClassLoader(), new Class<?>[]{tipo}, handler));
        }
    }
}
