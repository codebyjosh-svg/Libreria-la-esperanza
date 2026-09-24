import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.esperanza.dao.DevolucionVentaDao;
import org.esperanza.dao.ProveedorDAO;
import org.esperanza.model.EstadoVenta;
import org.esperanza.model.Proveedor;
import org.esperanza.model.Usuario;
import org.esperanza.service.SesionUsuario;

/**
 * Integración real para una base DE PRUEBA local, previamente creada con
 * test/fixtures/esquema_minimo.sql y las migraciones de T4.
 * Configuración: ESPERANZA_TEST_URL, ESPERANZA_TEST_USER, ESPERANZA_TEST_PASSWORD.
 * Rechaza cualquier catálogo distinto de esperanza_test y cualquier servidor remoto.
 */
public class PruebaIntegracionMySql {
    private static String url;
    private static String usuarioDb;
    private static String claveDb;
    private static final String SUFIJO = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    private static final List<Integer> ventasCreadas = new ArrayList<>();
    private static final List<Integer> proveedoresCreados = new ArrayList<>();
    private static final List<Integer> usuariosCreados = new ArrayList<>();
    private static final List<String> librosCreados = new ArrayList<>();
    private static long cui;
    private static int admin;
    private static int cajero;
    private static int bodega;
    private static String triggerCreado;

    interface Accion { void ejecutar() throws Exception; }
    record VentaPrueba(int id, String libroA, String libroB) {}

    private static void verificar(boolean condicion, String mensaje) {
        if (!condicion) throw new AssertionError(mensaje);
    }

    private static void rechaza(Class<? extends Throwable> esperado, Accion accion) throws Exception {
        try { accion.ejecutar(); }
        catch (Throwable ex) {
            if (esperado.isInstance(ex)) return;
            throw new AssertionError("Esperado " + esperado.getSimpleName() + ": " + ex, ex);
        }
        throw new AssertionError("No se rechazó una operación inválida: " + esperado.getSimpleName());
    }

    private static Connection conectar() throws SQLException {
        Connection con = DriverManager.getConnection(url, usuarioDb, claveDb);
        if (!"esperanza_test".equals(con.getCatalog())) {
            con.close();
            throw new SQLException("Integración permitida únicamente en el catálogo esperanza_test.");
        }
        return con;
    }

    private static void configurar() {
        url = System.getenv("ESPERANZA_TEST_URL");
        usuarioDb = System.getenv("ESPERANZA_TEST_USER");
        claveDb = System.getenv().getOrDefault("ESPERANZA_TEST_PASSWORD", "");
        if (url == null || !url.matches("jdbc:mysql://(?:127\\.0\\.0\\.1|localhost):[0-9]+/esperanza_test(?:\\?.*)?")) {
            throw new IllegalArgumentException("Configura ESPERANZA_TEST_URL con un MySQL local y catálogo esperanza_test.");
        }
        if (usuarioDb == null || usuarioDb.isBlank()) {
            throw new IllegalArgumentException("Configura ESPERANZA_TEST_USER para la instancia de prueba.");
        }
    }

    private static void sql(String consulta, Object... parametros) throws SQLException {
        try (Connection con = conectar(); PreparedStatement ps = con.prepareStatement(consulta)) {
            for (int i = 0; i < parametros.length; i++) ps.setObject(i + 1, parametros[i]);
            ps.executeUpdate();
        }
    }

    private static int insertar(String consulta, Object... parametros) throws SQLException {
        try (Connection con = conectar(); PreparedStatement ps = con.prepareStatement(consulta, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < parametros.length; i++) ps.setObject(i + 1, parametros[i]);
            verificar(ps.executeUpdate() == 1, "Insert de fixture");
            try (ResultSet rs = ps.getGeneratedKeys()) {
                verificar(rs.next(), "ID generado de fixture");
                return rs.getInt(1);
            }
        }
    }

    private static long numero(String consulta, Object... parametros) throws SQLException {
        try (Connection con = conectar(); PreparedStatement ps = con.prepareStatement(consulta)) {
            for (int i = 0; i < parametros.length; i++) ps.setObject(i + 1, parametros[i]);
            try (ResultSet rs = ps.executeQuery()) {
                verificar(rs.next(), "Consulta de verificación");
                return rs.getLong(1);
            }
        }
    }

    private static String texto(String consulta, Object... parametros) throws SQLException {
        try (Connection con = conectar(); PreparedStatement ps = con.prepareStatement(consulta)) {
            for (int i = 0; i < parametros.length; i++) ps.setObject(i + 1, parametros[i]);
            try (ResultSet rs = ps.executeQuery()) {
                verificar(rs.next(), "Consulta de texto");
                return rs.getString(1);
            }
        }
    }

    private static int crearUsuario(String rol) throws SQLException {
        int id = insertar("INSERT INTO usuarios (username,password_hash,rol,nombre,apellido,activo) VALUES (?,?,?,?,?,TRUE)",
                "it_" + rol + "_" + SUFIJO, "0".repeat(64), rol, "Prueba", "Integración");
        usuariosCreados.add(id);
        return id;
    }

    private static void sesion(int id, String rol) {
        SesionUsuario.getInstancia().cerrarSesion();
        verificar(SesionUsuario.getInstancia().iniciarSesion(
                new Usuario(id, "integracion", rol, "Prueba", "Integración", "", true)), "Sesión " + rol);
    }

    private static void crearFixture() throws SQLException {
        admin = crearUsuario("ADMIN");
        cajero = crearUsuario("CAJERO");
        bodega = crearUsuario("BODEGA");
        long candidato = 9000000000000L + (System.currentTimeMillis() % 999999999999L);
        sql("INSERT INTO clientes (cui,nombre_cliente,apellido_cliente) VALUES (?,?,?)", candidato, "Cliente", "Prueba " + SUFIJO);
        cui = candidato;
    }

    private static VentaPrueba crearVenta(String caso) throws SQLException {
        String isbnA = "IT-A-" + caso + "-" + SUFIJO;
        String isbnB = "IT-B-" + caso + "-" + SUFIJO;
        sql("INSERT INTO libros (isbn,titulo,precio,stock_actual,activo) VALUES (?,?,10,3,FALSE)", isbnA, "Libro inactivo " + caso);
        librosCreados.add(isbnA);
        sql("INSERT INTO libros (isbn,titulo,precio,stock_actual,activo) VALUES (?,?,10,10,TRUE)", isbnB, "Libro activo " + caso);
        librosCreados.add(isbnB);
        int id = insertar("INSERT INTO ventas (subtotal,descuento,total,estado,cui_cliente,id_usuario) VALUES (60,0,60,'COMPLETADA',?,?)", cui, cajero);
        ventasCreadas.add(id);
        for (Object[] detalle : List.of(new Object[]{isbnA, 2}, new Object[]{isbnB, 1}, new Object[]{isbnA, 3})) {
            int cantidad = (Integer) detalle[1];
            sql("INSERT INTO detalle_venta (id_venta,isbn,cantidad,precio_unitario,subtotal) VALUES (?,?,?,10,?)",
                    id, detalle[0], cantidad, cantidad * 10);
        }
        return new VentaPrueba(id, isbnA, isbnB);
    }

    private static void verificarDevuelta(VentaPrueba venta, int usuario) throws SQLException {
        verificar("DEVUELTA".equals(texto("SELECT estado FROM ventas WHERE id_venta=?", venta.id())), "Estado DEVUELTA");
        verificar(numero("SELECT stock_actual FROM libros WHERE isbn=?", venta.libroA()) == 8, "Stock libro inactivo +5");
        verificar(numero("SELECT activo FROM libros WHERE isbn=?", venta.libroA()) == 0, "No reactivar libro al devolver");
        verificar(numero("SELECT stock_actual FROM libros WHERE isbn=?", venta.libroB()) == 11, "Stock libro activo +1");
        verificar(numero("SELECT COUNT(*) FROM devoluciones_venta WHERE id_venta=?", venta.id()) == 1, "Una auditoría por venta");
        verificar(numero("SELECT id_usuario FROM devoluciones_venta WHERE id_venta=?", venta.id()) == usuario, "Usuario de sesión auditado");
        verificar(numero("SELECT COUNT(*) FROM movimientos_devolucion_venta WHERE id_venta=?", venta.id()) == 2, "Dos movimientos por ISBN distinto");
        verificar(numero("SELECT SUM(cantidad) FROM movimientos_devolucion_venta WHERE id_venta=?", venta.id()) == 6, "Cantidades exactas");
        verificar(numero("SELECT total FROM ventas WHERE id_venta=?", venta.id()) == 60, "Conservar importe histórico");
    }

    private static void devolucionExitosaYConsulta() throws Exception {
        sesion(admin, "ADMIN");
        VentaPrueba venta = crearVenta("exito");
        DevolucionVentaDao dao = new DevolucionVentaDao(PruebaIntegracionMySql::conectar);
        verificar(dao.listarDetalles(venta.id()).size() == 3, "Mostrar detalle original");
        verificar(dao.listarRecientes(venta.id()).size() == 1, "Consulta por ID reciente");
        dao.devolver(venta.id(), "  Productos equivocados  ");
        verificarDevuelta(venta, admin);
        verificar("Productos equivocados".equals(texto("SELECT motivo FROM devoluciones_venta WHERE id_venta=?", venta.id())), "Motivo normalizado");
        verificar(dao.listarRecientes(venta.id()).get(0).getEstado() == EstadoVenta.DEVUELTA, "Consulta lee DEVUELTA");
        rechaza(IllegalStateException.class, () -> dao.devolver(venta.id(), "Intento duplicado"));
        verificarDevuelta(venta, admin);
        VentaPrueba antigua = crearVenta("antigua");
        sql("UPDATE ventas SET fecha_venta=DATE_SUB(NOW(),INTERVAL 40 DAY) WHERE id_venta=?", antigua.id());
        verificar(dao.listarRecientes(antigua.id()).isEmpty(), "Excluir ventas anteriores a 30 días");
        sql("UPDATE ventas SET fecha_venta=DATE_ADD(NOW(),INTERVAL 1 DAY) WHERE id_venta=?", antigua.id());
        verificar(dao.listarRecientes(antigua.id()).isEmpty(), "Excluir fechas futuras");
        System.out.println("OK MySQL - devolución total, libro inactivo, auditoría, duplicada y consulta de 30 días");
    }

    private static void rollbackReal() throws Exception {
        sesion(cajero, "CAJERO");
        VentaPrueba venta = crearVenta("rollback");
        triggerCreado = "it_fallo_devolucion_" + SUFIJO;
        sql("CREATE TRIGGER " + triggerCreado + " BEFORE INSERT ON movimientos_devolucion_venta FOR EACH ROW "
                + "SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Fallo intencional de integración T4'");
        try {
            DevolucionVentaDao dao = new DevolucionVentaDao(PruebaIntegracionMySql::conectar);
            rechaza(SQLException.class, () -> dao.devolver(venta.id(), "Validar rollback real"));
            verificar("COMPLETADA".equals(texto("SELECT estado FROM ventas WHERE id_venta=?", venta.id())), "Rollback de estado");
            verificar(numero("SELECT stock_actual FROM libros WHERE isbn=?", venta.libroA()) == 3, "Rollback de stock ya incrementado");
            verificar(numero("SELECT stock_actual FROM libros WHERE isbn=?", venta.libroB()) == 10, "Stock del segundo libro intacto");
            verificar(numero("SELECT COUNT(*) FROM devoluciones_venta WHERE id_venta=?", venta.id()) == 0, "Rollback de auditoría insertada");
            verificar(numero("SELECT COUNT(*) FROM movimientos_devolucion_venta WHERE id_venta=?", venta.id()) == 0, "Sin movimientos parciales");
        } finally {
            sql("DROP TRIGGER IF EXISTS " + triggerCreado);
            triggerCreado = null;
        }
        new DevolucionVentaDao(PruebaIntegracionMySql::conectar).devolver(venta.id(), "Reintento después del fallo");
        verificarDevuelta(venta, cajero);
        System.out.println("OK MySQL - rollback real por trigger y reintento íntegro");
    }

    private static void concurrenciaReal() throws Exception {
        sesion(cajero, "CAJERO");
        VentaPrueba venta = crearVenta("concurrente");
        CountDownLatch preparados = new CountDownLatch(2);
        CountDownLatch inicio = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(2);
        try {
            List<Future<Boolean>> intentos = new ArrayList<>();
            for (int i = 0; i < 2; i++) intentos.add(executor.submit(() -> {
                preparados.countDown();
                verificar(inicio.await(10, TimeUnit.SECONDS), "Inicio de concurrencia");
                try {
                    new DevolucionVentaDao(PruebaIntegracionMySql::conectar).devolver(venta.id(), "Devolución concurrente");
                    return true;
                } catch (IllegalStateException esperado) { return false; }
            }));
            verificar(preparados.await(10, TimeUnit.SECONDS), "Dos hilos preparados");
            inicio.countDown();
            int exitos = 0;
            for (Future<Boolean> intento : intentos) if (intento.get(20, TimeUnit.SECONDS)) exitos++;
            verificar(exitos == 1, "Exactamente una devolución concurrente aceptada");
            verificarDevuelta(venta, cajero);
        } finally { executor.shutdownNow(); }
        System.out.println("OK MySQL - dos conexiones concurrentes, una devolución y stock sin duplicar");
    }

    private static Proveedor proveedor(int id, String nit, String nombre, boolean activo) {
        return new Proveedor(id, nit, nombre, "Contacto de prueba", "+502 5555-0101",
                "prueba@example.com", "Dirección de prueba", activo);
    }

    private static void proveedoresYPermisos() throws Exception {
        sesion(admin, "ADMIN");
        ProveedorDAO dao = new ProveedorDAO(PruebaIntegracionMySql::conectar);
        String nit = "IT" + SUFIJO;
        int id = dao.crear(proveedor(0, nit, "Proveedor integración " + SUFIJO, true));
        proveedoresCreados.add(id);
        verificar(dao.buscarPorId(id).orElseThrow().getNit().equals(nit.toUpperCase()), "Crear/leer proveedor");
        verificar(dao.listar(SUFIJO, false).stream().anyMatch(p -> p.getId() == id), "Buscar proveedor");
        rechaza(IllegalArgumentException.class, () -> dao.crear(proveedor(0, nit.toLowerCase(), "Duplicado", true)));
        dao.actualizar(proveedor(id, nit, "Proveedor editado " + SUFIJO, true));
        verificar(dao.buscarPorId(id).orElseThrow().getNombre().startsWith("Proveedor editado"), "Actualizar proveedor");
        dao.cambiarEstado(id, false);
        verificar(dao.listar(SUFIJO, false).stream().noneMatch(p -> p.getId() == id), "Ocultar baja lógica");
        verificar(dao.listar(SUFIJO, true).stream().anyMatch(p -> p.getId() == id && !p.isActivo()), "Conservar proveedor inactivo");
        rechaza(IllegalArgumentException.class, () -> dao.crear(proveedor(0, nit, "Duplicado inactivo", true)));
        dao.cambiarEstado(id, true);
        verificar(dao.buscarPorId(id).orElseThrow().isActivo(), "Reactivar proveedor");
        for (Object[] rol : List.of(new Object[]{cajero, "CAJERO"}, new Object[]{bodega, "BODEGA"})) {
            sesion((Integer) rol[0], (String) rol[1]);
            rechaza(SecurityException.class, () -> dao.listar("", true));
            rechaza(SecurityException.class, () -> dao.crear(proveedor(0, "OTRO" + SUFIJO, "No autorizado", true)));
            rechaza(SecurityException.class, () -> dao.actualizar(proveedor(id, nit, "Cambio no autorizado", true)));
            rechaza(SecurityException.class, () -> dao.cambiarEstado(id, false));
        }
        DevolucionVentaDao devoluciones = new DevolucionVentaDao(PruebaIntegracionMySql::conectar);
        rechaza(SecurityException.class, () -> devoluciones.listarRecientes(null));
        rechaza(SecurityException.class, () -> devoluciones.devolver(ventasCreadas.get(0), "Acceso no autorizado"));
        SesionUsuario.getInstancia().cerrarSesion();
        rechaza(SecurityException.class, () -> dao.buscarPorId(id));
        rechaza(SecurityException.class, () -> devoluciones.listarDetalles(ventasCreadas.get(0)));
        System.out.println("OK MySQL - proveedores CRUD, NIT único, baja/reactivación y permisos de tres roles");
    }

    /** Solo elimina los identificadores creados por esta ejecución. */
    private static void limpiar() throws SQLException {
        if (triggerCreado != null) sql("DROP TRIGGER IF EXISTS " + triggerCreado);
        for (int id : ventasCreadas) {
            sql("DELETE FROM movimientos_devolucion_venta WHERE id_venta=?", id);
            sql("DELETE FROM devoluciones_venta WHERE id_venta=?", id);
            sql("DELETE FROM detalle_venta WHERE id_venta=?", id);
            sql("DELETE FROM ventas WHERE id_venta=?", id);
        }
        for (String isbn : librosCreados) sql("DELETE FROM libros WHERE isbn=?", isbn);
        for (int id : proveedoresCreados) sql("DELETE FROM proveedores WHERE id=?", id);
        if (cui != 0) sql("DELETE FROM clientes WHERE cui=?", cui);
        for (int id : usuariosCreados) sql("DELETE FROM usuarios WHERE id=?", id);
    }

    public static void main(String[] args) throws Exception {
        configurar();
        Class.forName("com.mysql.cj.jdbc.Driver");
        try {
            crearFixture();
            devolucionExitosaYConsulta();
            rollbackReal();
            concurrenciaReal();
            proveedoresYPermisos();
            System.out.println("TODAS LAS PRUEBAS DE INTEGRACIÓN MYSQL PASARON.");
        } finally {
            SesionUsuario.getInstancia().cerrarSesion();
            limpiar();
        }
    }
}
