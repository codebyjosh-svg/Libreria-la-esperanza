import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import org.esperanza.dao.DevolucionVentaDao;
import org.esperanza.model.EstadoVenta;
import org.esperanza.model.Usuario;
import org.esperanza.model.Venta;
import org.esperanza.service.SesionUsuario;

/** Pruebas JDBC simuladas. No sustituyen las pruebas concurrentes contra MySQL real. */
public class PruebaDevoluciones {
    interface Accion { void ejecutar() throws Exception; }

    static void verificar(boolean condicion, String mensaje) {
        if (!condicion) throw new AssertionError(mensaje);
    }

    static void rechaza(Class<? extends Throwable> tipo, Accion accion) throws Exception {
        try { accion.ejecutar(); }
        catch (Throwable ex) {
            if (tipo.isInstance(ex)) return;
            throw new AssertionError("Se esperaba " + tipo.getSimpleName() + ", recibido " + ex, ex);
        }
        throw new AssertionError("No se rechazó la operación: " + tipo.getSimpleName());
    }

    static void sesion(String rol) {
        SesionUsuario.getInstancia().cerrarSesion();
        if (rol != null) verificar(SesionUsuario.getInstancia().iniciarSesion(
                new Usuario(7, "operador", rol, "Operador", "Prueba", "", true)), "Sesión de prueba");
    }

    @SuppressWarnings("unchecked")
    static <T> T proxy(Class<T> tipo, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(tipo.getClassLoader(), new Class<?>[]{tipo}, handler);
    }

    static Map<String, Object> fila(Object... valores) {
        Map<String, Object> fila = new HashMap<>();
        for (int i = 0; i < valores.length; i += 2) fila.put((String) valores[i], valores[i + 1]);
        return fila;
    }

    static ResultSet resultado(List<Map<String, Object>> filas) {
        int[] indice = {-1};
        return proxy(ResultSet.class, (obj, metodo, args) -> switch (metodo.getName()) {
            case "next" -> ++indice[0] < filas.size();
            case "getString" -> (String) filas.get(indice[0]).get(args[0]);
            case "getInt" -> ((Number) filas.get(indice[0]).get(args[0])).intValue();
            case "getLong" -> ((Number) filas.get(indice[0]).get(args[0])).longValue();
            case "getBigDecimal", "getTimestamp" -> filas.get(indice[0]).get(args[0]);
            case "close" -> null;
            default -> throw new UnsupportedOperationException("ResultSet: " + metodo.getName());
        });
    }

    static final class Base {
        final ReentrantLock bloqueoVenta = new ReentrantLock();
        final AtomicInteger conexiones = new AtomicInteger();
        final AtomicInteger commits = new AtomicInteger();
        final AtomicInteger rollbacks = new AtomicInteger();
        final AtomicInteger cierres = new AtomicInteger();
        String estado = "COMPLETADA";
        Map<String, Integer> stock = new HashMap<>(Map.of("A", 3, "B", 10));
        List<Map<String, Object>> detalles = new ArrayList<>(List.of(
                fila("isbn", "A", "cantidad", 2), fila("isbn", "B", "cantidad", 1),
                fila("isbn", "A", "cantidad", 3)));
        Map<String, Integer> movimientos = new HashMap<>();
        boolean auditoria;
        int usuarioAuditado;
        String motivoAuditado;
        boolean existe = true;
        String motor = "InnoDB";
        int tablas = 5;
        String fallo = "";
        Integer filtroConsulta;

        Connection conectar() {
            conexiones.incrementAndGet();
            Tx tx = new Tx();
            return proxy(Connection.class, (obj, metodo, args) -> switch (metodo.getName()) {
                case "setAutoCommit" -> { verificar(Boolean.FALSE.equals(args[0]), "Transacción explícita"); yield null; }
                case "prepareStatement" -> tx.preparar((String) args[0]);
                case "commit" -> { tx.confirmar(); yield null; }
                case "rollback" -> { rollbacks.incrementAndGet(); tx.liberar(); yield null; }
                case "close" -> { cierres.incrementAndGet(); tx.liberar(); yield null; }
                default -> throw new UnsupportedOperationException("Connection: " + metodo.getName());
            });
        }

        final class Tx {
            boolean bloqueada;
            String estadoLocal;
            Map<String, Integer> stockLocal;
            Map<String, Integer> movimientosLocales;
            boolean auditoriaLocal;
            int usuarioLocal;
            String motivoLocal;

            void bloquear() {
                bloqueoVenta.lock();
                bloqueada = true;
                estadoLocal = estado;
                stockLocal = new HashMap<>(stock);
                movimientosLocales = new HashMap<>(movimientos);
                auditoriaLocal = auditoria;
            }

            void liberar() {
                if (bloqueada) { bloqueada = false; bloqueoVenta.unlock(); }
            }

            void confirmar() throws SQLException {
                if (fallo.equals("commit")) throw new SQLException("Fallo al confirmar");
                verificar(bloqueada, "Debe mantener bloqueo hasta commit");
                estado = estadoLocal;
                stock = stockLocal;
                movimientos = movimientosLocales;
                auditoria = auditoriaLocal;
                usuarioAuditado = usuarioLocal;
                motivoAuditado = motivoLocal;
                commits.incrementAndGet();
                liberar();
            }

            PreparedStatement preparar(String sql) {
                Map<Integer, Object> parametros = new HashMap<>();
                return proxy(PreparedStatement.class, (obj, metodo, args) -> {
                    String nombre = metodo.getName();
                    if (nombre.startsWith("set")) { parametros.put((Integer) args[0], args[1]); return null; }
                    return switch (nombre) {
                        case "close" -> null;
                        case "executeQuery" -> consultar(sql, parametros);
                        case "executeUpdate" -> actualizar(sql, parametros);
                        default -> throw new UnsupportedOperationException("PreparedStatement: " + nombre);
                    };
                });
            }

            ResultSet consultar(String sql, Map<Integer, Object> p) throws SQLException {
                if (sql.contains("information_schema")) {
                    List<Map<String, Object>> filas = new ArrayList<>();
                    for (int i = 0; i < tablas; i++) filas.add(fila("ENGINE", motor));
                    return resultado(filas);
                }
                if (sql.startsWith("SELECT estado FROM ventas")) {
                    verificar(sql.endsWith("FOR UPDATE"), "La venta necesita bloqueo de escritura");
                    bloquear();
                    return resultado(existe ? List.of(fila("estado", estadoLocal)) : List.of());
                }
                if (sql.contains("FROM detalle_venta")) {
                    verificar(sql.contains("FOR UPDATE"), "Los detalles deben bloquearse al devolver");
                    return resultado(detalles);
                }
                if (sql.contains("FROM ventas")) {
                    verificar(sql.contains("INTERVAL 30 DAY") && sql.contains("fecha_venta <= NOW()"), "Ventana reciente");
                    filtroConsulta = (Integer) p.get(1);
                    verificar((filtroConsulta != null) == sql.contains("AND id_venta = ?"), "Filtro preparado por ID");
                    return resultado(List.of(fila("id_venta", 42, "fecha_venta", Timestamp.valueOf(LocalDateTime.now()),
                            "subtotal", new BigDecimal("75.00"), "descuento", BigDecimal.ZERO,
                            "total", new BigDecimal("75.00"), "cui_cliente", 1234567890123L,
                            "id_usuario", 7, "estado", estado)));
                }
                throw new SQLException("Consulta no simulada: " + sql);
            }

            int actualizar(String sql, Map<Integer, Object> p) throws SQLException {
                verificar(bloqueada, "No mutar antes de bloquear la venta");
                if (sql.contains("INSERT INTO devoluciones_venta")) {
                    if (fallo.equals("auditoria") || auditoriaLocal) throw new SQLException("Auditoría duplicada o fallida");
                    auditoriaLocal = true;
                    motivoLocal = (String) p.get(2);
                    usuarioLocal = (Integer) p.get(3);
                    return 1;
                }
                if (sql.contains("UPDATE libros")) {
                    verificar(!sql.contains("activo"), "La devolución debe reponer libros inactivos");
                    if (fallo.equals("stock")) throw new SQLException("Fallo stock");
                    String isbn = (String) p.get(2);
                    Integer actual = stockLocal.get(isbn);
                    if (actual == null || actual < 0 || actual > (Integer) p.get(3)) return 0;
                    stockLocal.put(isbn, actual + (Integer) p.get(1));
                    return 1;
                }
                if (sql.contains("INSERT INTO movimientos_devolucion_venta")) {
                    if (fallo.equals("movimiento")) throw new SQLException("Fallo movimiento");
                    String isbn = (String) p.get(2);
                    if (movimientosLocales.containsKey(isbn)) throw new SQLException("Movimiento duplicado");
                    movimientosLocales.put(isbn, (Integer) p.get(3));
                    return 1;
                }
                if (sql.contains("UPDATE ventas")) {
                    verificar(sql.contains("AND estado = 'COMPLETADA'"), "El cambio debe ser condicional");
                    if (fallo.equals("estado")) return 0;
                    estadoLocal = "DEVUELTA";
                    return 1;
                }
                throw new SQLException("Mutación no simulada: " + sql);
            }
        }
    }

    static void exitoYDuplicada() throws Exception {
        sesion("ADMIN");
        Base base = new Base();
        DevolucionVentaDao dao = new DevolucionVentaDao(base::conectar);
        dao.devolver(42, "  Producto equivocado  ");
        verificar(base.commits.get() == 1 && base.rollbacks.get() == 0, "Confirmación única");
        verificar(base.estado.equals("DEVUELTA"), "Cambio de estado");
        verificar(base.stock.equals(Map.of("A", 8, "B", 11)), "Restauración exacta, sumando ISBN repetidos");
        verificar(base.movimientos.equals(Map.of("A", 5, "B", 1)), "Un movimiento por ISBN");
        verificar(base.auditoria && base.usuarioAuditado == 7 && base.motivoAuditado.equals("Producto equivocado"), "Auditoría de sesión y motivo");
        rechaza(IllegalStateException.class, () -> dao.devolver(42, "Segundo intento"));
        verificar(base.commits.get() == 1 && base.rollbacks.get() == 1 && base.stock.get("A") == 8,
                "No duplicar stock al devolver otra vez");
        verificar(base.cierres.get() == 2, "Cerrar todas las conexiones");
    }

    static void fallosAtomicos() throws Exception {
        sesion("CAJERO");
        for (String fase : List.of("auditoria", "stock", "movimiento", "estado", "commit")) {
            Base base = new Base(); base.fallo = fase;
            rechaza(SQLException.class, () -> new DevolucionVentaDao(base::conectar).devolver(42, "Motivo válido"));
            verificar(base.commits.get() == 0 && base.rollbacks.get() == 1, "Rollback en " + fase);
            verificar(base.estado.equals("COMPLETADA") && base.stock.equals(Map.of("A", 3, "B", 10))
                    && !base.auditoria && base.movimientos.isEmpty(), "Sin cambios parciales en " + fase);
            verificar(base.cierres.get() == 1, "Cierre en fallo " + fase);
        }
    }

    static void validaciones() throws Exception {
        sesion("ADMIN");
        Base base = new Base(); DevolucionVentaDao dao = new DevolucionVentaDao(base::conectar);
        for (String motivo : new String[]{null, "", "    ", "abcd", "x".repeat(501)}) {
            rechaza(IllegalArgumentException.class, () -> dao.devolver(42, motivo));
        }
        rechaza(IllegalArgumentException.class, () -> dao.devolver(0, "Motivo válido"));
        rechaza(IllegalArgumentException.class, () -> dao.listarRecientes(-1));
        verificar(base.conexiones.get() == 0, "Validar antes de abrir conexión");
        verificar(DevolucionVentaDao.validarMotivo("a".repeat(500)).length() == 500, "Límite superior aceptado");
        for (String estado : List.of("ANULADA", "PENDIENTE")) {
            Base otra = new Base(); otra.estado = estado;
            rechaza(IllegalStateException.class, () -> new DevolucionVentaDao(otra::conectar).devolver(42, "Motivo válido"));
            verificar(otra.commits.get() == 0 && otra.rollbacks.get() == 1, "Estado no elegible: " + estado);
        }
        Base ausente = new Base(); ausente.existe = false;
        rechaza(IllegalArgumentException.class, () -> new DevolucionVentaDao(ausente::conectar).devolver(42, "Motivo válido"));
        Base vacia = new Base(); vacia.detalles.clear();
        rechaza(IllegalStateException.class, () -> new DevolucionVentaDao(vacia::conectar).devolver(42, "Motivo válido"));
        Base corrupta = new Base(); corrupta.detalles = List.of(fila("isbn", "A", "cantidad", 0));
        rechaza(SQLException.class, () -> new DevolucionVentaDao(corrupta::conectar).devolver(42, "Motivo válido"));
        Base desborde = new Base(); desborde.stock.put("A", Integer.MAX_VALUE);
        rechaza(SQLException.class, () -> new DevolucionVentaDao(desborde::conectar).devolver(42, "Motivo válido"));
        verificar(!desborde.auditoria && desborde.stock.get("A") == Integer.MAX_VALUE, "Rollback al desbordar stock");
        Base sinLibro = new Base(); sinLibro.stock.remove("B");
        rechaza(SQLException.class, () -> new DevolucionVentaDao(sinLibro::conectar).devolver(42, "Motivo válido"));
        verificar(sinLibro.stock.get("A") == 3, "Rollback del primer libro si el segundo falta");
        Base duplicada = new Base(); duplicada.auditoria = true;
        rechaza(SQLException.class, () -> new DevolucionVentaDao(duplicada::conectar).devolver(42, "Motivo válido"));
        verificar(duplicada.stock.get("A") == 3, "Auditoría única protege estado alterado externamente");
        Base motor = new Base(); motor.motor = "MyISAM";
        rechaza(SQLException.class, () -> new DevolucionVentaDao(motor::conectar).devolver(42, "Motivo válido"));
        verificar(motor.commits.get() == 0 && motor.rollbacks.get() == 0, "Rechazo antes de transacción no segura");
        Base tablas = new Base(); tablas.tablas = 4;
        rechaza(SQLException.class, () -> new DevolucionVentaDao(tablas::conectar).devolver(42, "Motivo válido"));
    }

    static void permisosYConsulta() throws Exception {
        for (String rol : new String[]{null, "BODEGA"}) {
            sesion(rol);
            Base base = new Base(); DevolucionVentaDao dao = new DevolucionVentaDao(base::conectar);
            rechaza(SecurityException.class, () -> dao.devolver(42, "Motivo válido"));
            rechaza(SecurityException.class, () -> dao.listarRecientes(null));
            rechaza(SecurityException.class, () -> dao.listarDetalles(42));
            verificar(base.conexiones.get() == 0, "Rechazo DAO para " + rol);
        }
        for (String rol : List.of("ADMIN", "CAJERO")) {
            sesion(rol);
            Base base = new Base(); base.estado = "DEVUELTA";
            DevolucionVentaDao dao = new DevolucionVentaDao(base::conectar);
            List<Venta> ventas = dao.listarRecientes(42);
            verificar(ventas.size() == 1 && ventas.get(0).getEstado() == EstadoVenta.DEVUELTA
                    && Integer.valueOf(42).equals(base.filtroConsulta), "Consulta y estado para " + rol);
            dao.listarRecientes(null);
            verificar(base.filtroConsulta == null, "Filtro opcional");
        }
        SesionUsuario.getInstancia().getUsuarioActual().setActivo(false);
        Base inactivo = new Base();
        rechaza(SecurityException.class, () -> new DevolucionVentaDao(inactivo::conectar).devolver(42, "Motivo válido"));
    }

    static void concurrenciaSimulada() throws Exception {
        sesion("CAJERO");
        Base base = new Base(); DevolucionVentaDao dao = new DevolucionVentaDao(base::conectar);
        CountDownLatch inicio = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(2);
        try {
            List<Future<Boolean>> intentos = new ArrayList<>();
            for (int i = 0; i < 2; i++) intentos.add(executor.submit(() -> {
                inicio.await();
                try { dao.devolver(42, "Intento concurrente"); return true; }
                catch (IllegalStateException esperado) { return false; }
            }));
            inicio.countDown();
            int exitos = 0;
            for (Future<Boolean> intento : intentos) if (intento.get(5, TimeUnit.SECONDS)) exitos++;
            verificar(exitos == 1 && base.commits.get() == 1 && base.rollbacks.get() == 1,
                    "Solo una devolución concurrente confirmada");
            verificar(base.stock.equals(Map.of("A", 8, "B", 11)) && base.movimientos.size() == 2,
                    "Concurrencia sin duplicar existencias ni movimientos");
        } finally { executor.shutdownNow(); }
    }

    public static void main(String[] args) throws Exception {
        try {
            exitoYDuplicada();
            fallosAtomicos();
            validaciones();
            permisosYConsulta();
            concurrenciaSimulada();
            System.out.println("OK - Devoluciones: éxito, duplicada, rollback de 5 fases, validaciones, 3 roles, consulta y concurrencia simulada.");
        } finally { SesionUsuario.getInstancia().cerrarSesion(); }
    }
}
