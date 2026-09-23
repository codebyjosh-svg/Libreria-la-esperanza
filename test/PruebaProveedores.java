import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.esperanza.dao.ProveedorDAO;
import org.esperanza.model.Proveedor;
import org.esperanza.model.Usuario;
import org.esperanza.service.SesionUsuario;

/** Prueba autónoma sin servidor: CRUD persistido en un doble JDBC y autorización real. */
public final class PruebaProveedores {
    public static void main(String[] args) throws Exception {
        Base base = new Base();
        ProveedorDAO dao = new ProveedorDAO(base::conexion);
        SesionUsuario sesion = SesionUsuario.getInstancia();
        sesion.cerrarSesion();
        denegado(dao);
        for (String rol : new String[]{"CAJERO", "BODEGA"}) {
            iniciar(rol);
            denegado(dao);
        }
        exigir(base.conexiones == 0, "Sin acceso JDBC para roles denegados o sesión ausente");

        iniciar("ADMIN");
        try {
            int id = dao.crear(proveedor(0, " 12345-k ", " Editorial O'Connor ", true));
            Proveedor creado = dao.buscarPorId(id).orElseThrow();
            exigir(id > 0 && "12345K".equals(creado.getNit())
                    && "Editorial O'Connor".equals(creado.getNombre()) && creado.isActivo(), "Alta y normalización");
            exigir(dao.listar("12345-k", false).size() == 1, "Buscar NIT con guion");
            exigir(dao.listar("O'Connor", false).size() == 1, "Comillas seguras en búsqueda y datos");
            exigir(dao.listar("%", false).isEmpty(), "Porcentaje se trata como texto literal");
            exigir(dao.listar("' OR 1=1 --", true).isEmpty(), "Búsqueda parametrizada");
            int otro = dao.crear(proveedor(0, "67890-1", "Papelería Norte", true));
            exigir(dao.listar("", false).size() == 2, "Listado de altas");

            Proveedor editado = new Proveedor(id, "12345K", "Editorial Central", "María Pérez",
                    "+502 2333-4455", "ventas@editorial.com", "Zona 1", false);
            dao.actualizar(editado);
            Proveedor leido = dao.buscarPorId(id).orElseThrow();
            exigir("Editorial Central".equals(leido.getNombre()) && "María Pérez".equals(leido.getContacto())
                    && "+502 2333-4455".equals(leido.getTelefono()) && "ventas@editorial.com".equals(leido.getCorreo())
                    && "Zona 1".equals(leido.getDireccion()) && leido.isActivo(), "Edición de todos los campos sin alterar estado");
            esperar(IllegalArgumentException.class, () -> dao.crear(proveedor(0, "12-345 k", "Duplicado", true)));
            esperar(IllegalArgumentException.class, () -> dao.actualizar(proveedor(otro, "12345-k", "Duplicado", true)));
            exigir("Papelería Norte".equals(dao.buscarPorId(otro).orElseThrow().getNombre()), "Duplicado no modifica la fila original");

            dao.cambiarEstado(id, false);
            exigir(dao.listar("", false).size() == 1 && dao.listar("", true).size() == 2
                    && !dao.buscarPorId(id).orElseThrow().isActivo() && base.filas.size() == 2,
                    "Baja lógica sin pérdida del ID ni de la información");
            esperar(IllegalArgumentException.class, () -> dao.cambiarEstado(id, false));
            esperar(IllegalArgumentException.class, () -> dao.crear(proveedor(0, "12345K", "Duplicado inactivo", true)));
            dao.cambiarEstado(id, true);
            exigir(dao.buscarPorId(id).orElseThrow().isActivo() && dao.listar("", false).size() == 2, "Reactivación");
            exigir(dao.buscarPorId(999).isEmpty(), "Consulta de inexistente");
            esperar(IllegalArgumentException.class, () -> dao.actualizar(proveedor(999, "9999K", "No existe", true)));
            esperar(IllegalArgumentException.class, () -> dao.cambiarEstado(999, false));

            int conexionesAntes = base.conexiones;
            esperar(IllegalArgumentException.class, () -> dao.crear(proveedor(id, "12345K", "Identificado", true)));
            esperar(IllegalArgumentException.class, () -> dao.crear(proveedor(0, "9999K", "Inactivo", false)));
            esperar(IllegalArgumentException.class, () -> dao.actualizar(proveedor(0, "9999K", "Sin ID", true)));
            esperar(IllegalArgumentException.class, () -> dao.buscarPorId(0));
            esperar(IllegalArgumentException.class, () -> dao.cambiarEstado(-1, false));
            esperar(IllegalArgumentException.class, () -> dao.listar("x".repeat(151), false));
            validarCampos();
            exigir(base.conexiones == conexionesAntes, "Validaciones antes de acceder a la base");

            base.fallarEscritura = true;
            esperar(SQLException.class, () -> dao.actualizar(proveedor(id, "12345K", "Fallo", true)));
            base.fallarEscritura = false;
            exigir("Editorial Central".equals(dao.buscarPorId(id).orElseThrow().getNombre()), "Fallo SQL no se presenta como éxito");
            exigir(base.conexiones == base.cerradas, "Conexiones cerradas incluso tras errores");
            exigir(base.sentencias == base.sentenciasCerradas, "Sentencias cerradas incluso tras errores");
            exigir(base.resultados == base.resultadosCerrados, "Resultados cerrados");

            Usuario admin = sesion.getUsuarioActual();
            admin.setActivo(false);
            denegado(dao);
            iniciar("CAJERO");
            denegado(dao); // El DAO no conserva privilegios de una sesión anterior.
            System.out.println("OK Proveedores: alta, consulta, edición, NIT único, baja lógica, reactivación, validaciones, SQL seguro, recursos y tres roles.");
        } finally { sesion.cerrarSesion(); }
    }

    private static void iniciar(String rol) {
        exigir(SesionUsuario.getInstancia().iniciarSesion(new Usuario(1, "prueba", rol, "Prueba", "", "", true)), "Inicio de sesión " + rol);
    }

    private static Proveedor proveedor(int id, String nit, String nombre, boolean activo) {
        return new Proveedor(id, nit, nombre, "", "2222-3333", "", "", activo);
    }

    private static void denegado(ProveedorDAO dao) throws Exception {
        esperar(SecurityException.class, () -> dao.listar("", true));
        esperar(SecurityException.class, () -> dao.buscarPorId(1));
        esperar(SecurityException.class, () -> dao.crear(proveedor(0, "123K", "Proveedor", true)));
        esperar(SecurityException.class, () -> dao.actualizar(proveedor(1, "123K", "Proveedor", true)));
        esperar(SecurityException.class, () -> dao.cambiarEstado(1, false));
    }

    private static void validarCampos() throws Exception {
        for (String nit : new String[]{"", "  ", "12", "ABC'OR", "x".repeat(26)}) {
            esperar(IllegalArgumentException.class, () -> proveedor(0, nit, "Proveedor", true));
        }
        for (String nombre : new String[]{"", " ", "A", "x".repeat(151), "Nombre\nOtro"}) {
            esperar(IllegalArgumentException.class, () -> proveedor(0, "123K", nombre, true));
        }
        for (String telefono : new String[]{"", "123456", "1234567890123456", "2222-ABCD", "2222+3333"}) {
            esperar(IllegalArgumentException.class, () -> new Proveedor(0, "123K", "Proveedor", "", telefono, "", "", true));
        }
        for (String correo : new String[]{"sin-arroba", "a@", "a@.com", "a..b@ejemplo.com", ".a@ejemplo.com", "a@ejemplo..com", "a b@ejemplo.com"}) {
            esperar(IllegalArgumentException.class, () -> new Proveedor(0, "123K", "Proveedor", "", "22223333", correo, "", true));
        }
        esperar(IllegalArgumentException.class, () -> new Proveedor(0, "123K", "Proveedor", "x".repeat(101), "22223333", "", "", true));
        esperar(IllegalArgumentException.class, () -> new Proveedor(0, "123K", "Proveedor", "", "22223333", "", "x".repeat(251), true));
        esperar(IllegalArgumentException.class, () -> proveedor(-1, "123K", "Proveedor", true));
        Proveedor limites = new Proveedor(0, "1".repeat(25), "x".repeat(150), "x".repeat(100),
                "+123456789012345", "prueba+ventas@sub.ejemplo.com", "x".repeat(250), true);
        exigir(limites.getNombre().length() == 150, "Límites válidos");
        Proveedor opcionales = new Proveedor(0, "123K", "Proveedor", null, "22223333", null, null, true);
        exigir(opcionales.getCorreo().isEmpty() && opcionales.getDireccion().isEmpty(), "Opcionales nulos");
    }

    private static void exigir(boolean condicion, String mensaje) {
        if (!condicion) throw new AssertionError(mensaje);
    }
    private interface Accion { void ejecutar() throws Exception; }
    private static void esperar(Class<? extends Exception> clase, Accion accion) throws Exception {
        try { accion.ejecutar(); }
        catch (Exception ex) { if (clase.isInstance(ex)) return; throw ex; }
        throw new AssertionError("Se esperaba " + clase.getSimpleName());
    }

    private static final class Base {
        final Map<Integer, Map<String, Object>> filas = new LinkedHashMap<>();
        int secuencia, conexiones, cerradas, sentencias, sentenciasCerradas, resultados, resultadosCerrados;
        boolean fallarEscritura;

        Connection conexion() {
            conexiones++;
            return proxy(Connection.class, (obj, metodo, args) -> switch (metodo.getName()) {
                case "prepareStatement" -> preparar((String) args[0]);
                case "close" -> { cerradas++; yield null; }
                default -> throw new UnsupportedOperationException(metodo.getName());
            });
        }

        PreparedStatement preparar(String sql) {
            sentencias++;
            Map<Integer, Object> parametros = new HashMap<>();
            int[] generado = {0};
            exigir(!sql.contains("O'Connor") && !sql.contains("' OR 1=1"), "SQL parametrizado");
            return proxy(PreparedStatement.class, (obj, metodo, args) -> switch (metodo.getName()) {
                case "setString", "setInt", "setBoolean" -> { parametros.put((int) args[0], args[1]); yield null; }
                case "close" -> { sentenciasCerradas++; yield null; }
                case "getGeneratedKeys" -> resultado(List.of(Map.of("1", generado[0])));
                case "executeQuery" -> consultar(sql, parametros);
                case "executeUpdate" -> {
                    if (fallarEscritura) throw new SQLException("Fallo simulado de escritura", "08006");
                    if (sql.startsWith("INSERT")) {
                        verificarUnico((String) parametros.get(1), 0);
                        int id = ++secuencia;
                        Map<String, Object> fila = datos(parametros);
                        fila.put("id", id);
                        fila.put("activo", true);
                        filas.put(id, fila);
                        generado[0] = id;
                        yield 1;
                    }
                    if (sql.startsWith("UPDATE proveedores SET nit")) {
                        int id = (int) parametros.get(7);
                        if (!filas.containsKey(id)) yield 0;
                        verificarUnico((String) parametros.get(1), id);
                        filas.get(id).putAll(datos(parametros));
                        yield 1;
                    }
                    exigir(sql.startsWith("UPDATE proveedores SET activo") && sql.contains("AND activo <> ?"), "Baja lógica condicionada");
                    Map<String, Object> fila = filas.get((int) parametros.get(2));
                    if (fila == null || fila.get("activo").equals(parametros.get(3))) yield 0;
                    fila.put("activo", parametros.get(1));
                    yield 1;
                }
                default -> throw new UnsupportedOperationException(metodo.getName());
            });
        }

        private void verificarUnico(String nit, int idExcluido) throws SQLException {
            for (Map<String, Object> fila : filas.values()) {
                if (!fila.get("id").equals(idExcluido) && nit.equalsIgnoreCase((String) fila.get("nit"))) {
                    throw new SQLException("NIT duplicado", "23000", 1062);
                }
            }
        }

        private Map<String, Object> datos(Map<Integer, Object> p) {
            Map<String, Object> datos = new HashMap<>();
            String[] campos = {"nit", "nombre", "contacto", "telefono", "correo", "direccion"};
            for (int i = 0; i < campos.length; i++) datos.put(campos[i], p.get(i + 1));
            return datos;
        }

        private ResultSet consultar(String sql, Map<Integer, Object> p) {
            if (sql.endsWith("WHERE id = ?")) {
                Map<String, Object> fila = filas.get((int) p.get(1));
                return resultado(fila == null ? List.of() : List.of(new HashMap<>(fila)));
            }
            exigir(sql.contains("LOCATE") && sql.contains("ORDER BY nombre, id"), "Listado filtrado y ordenado");
            List<Map<String, Object>> seleccion = new ArrayList<>();
            for (Map<String, Object> fila : filas.values()) {
                if (!(boolean) p.get(1) && !(boolean) fila.get("activo")) continue;
                if (p.get(2).equals("") || contiene(fila.get("nombre"), p.get(3))
                        || contiene(fila.get("nit"), p.get(4)) || contiene(fila.get("contacto"), p.get(5))) {
                    seleccion.add(new HashMap<>(fila));
                }
            }
            seleccion.sort(Comparator.comparing(f -> (String) f.get("nombre")));
            return resultado(seleccion);
        }

        private boolean contiene(Object texto, Object filtro) {
            return texto.toString().toLowerCase(Locale.ROOT).contains(filtro.toString().toLowerCase(Locale.ROOT));
        }

        private ResultSet resultado(List<Map<String, Object>> filasResultado) {
            resultados++;
            int[] indice = {-1};
            return proxy(ResultSet.class, (obj, metodo, args) -> switch (metodo.getName()) {
                case "next" -> ++indice[0] < filasResultado.size();
                case "getString", "getInt", "getBoolean" -> filasResultado.get(indice[0]).get(args[0].toString());
                case "close" -> { resultadosCerrados++; yield null; }
                default -> throw new UnsupportedOperationException(metodo.getName());
            });
        }
    }

    private static <T> T proxy(Class<T> tipo, InvocationHandler handler) {
        return tipo.cast(Proxy.newProxyInstance(tipo.getClassLoader(), new Class<?>[]{tipo}, handler));
    }
}
