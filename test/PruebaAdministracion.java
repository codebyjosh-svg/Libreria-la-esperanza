import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.esperanza.dao.UsuarioDao;
import org.esperanza.model.Rol;
import org.esperanza.model.Usuario;
import org.esperanza.service.SesionUsuario;
import org.esperanza.util.PasswordUtil;

/** Pruebas sin servidor: autorizacion antes de JDBC y flujo administrativo. */
public class PruebaAdministracion {
    private static final SesionUsuario SESION = SesionUsuario.getInstancia();
    private static final String HASH = PasswordUtil.hashSHA256("ClavePrueba9");

    private static void verificar(boolean condicion, String mensaje) {
        if (!condicion) throw new AssertionError(mensaje);
    }

    private static Usuario usuario(int id, String rol) {
        return new Usuario(id, "usuario" + id, rol, "Persona", "Prueba",
                "usuario@example.test", true);
    }

    private static void entrar(int id, String rol) {
        SESION.cerrarSesion();
        verificar(SESION.iniciarSesion(usuario(id, rol)), "Sesion valida: " + rol);
    }

    private static boolean registrar(UsuarioDao dao, String username, String rol) {
        return dao.registrarUsuario(username, HASH, rol, "Persona", "Prueba", "alta@example.test");
    }

    private static void comprobarAdministracionDenegada(UsuarioDao dao, String caso) {
        verificar(dao.listarUsuarios().isEmpty(), caso + ": no puede listar usuarios");
        verificar(!registrar(dao, "alta.denegada", "ADMIN"), caso + ": no puede crear admin");
        verificar(!dao.activarUsuario(99), caso + ": no puede activar");
        verificar(!dao.desactivarUsuario(99), caso + ": no puede desactivar");
        verificar(!dao.actualizarPassword(99, "ClaveNueva9"), caso + ": no puede suplantar ID");
        verificar(!dao.validarPasswordActual(99, "ClavePrueba9"), caso + ": no puede consultar hash ajeno");
    }

    private static void probarDenegacionAntesDeJDBC() {
        UsuarioDao dao = new UsuarioDao(() -> {
            throw new AssertionError("Una operacion denegada intento acceder a la base de datos");
        });
        SESION.cerrarSesion();
        comprobarAdministracionDenegada(dao, "Sin sesion");
        for (String rol : List.of("CAJERO", "BODEGA")) {
            entrar(5, rol);
            comprobarAdministracionDenegada(dao, rol);
        }
        entrar(5, "ADMIN");
        verificar(!dao.desactivarUsuario(5), "El administrador no puede desactivarse a si mismo");
        verificar(!dao.actualizarPassword(6, "ClaveNueva9"), "Ni ADMIN cambia la clave ajena");
        SESION.cerrarSesion();
        comprobarAdministracionDenegada(dao, "Formulario retenido despues de salir");
        entrar(5, "ADMIN");
        SESION.getUsuarioActual().setActivo(false);
        comprobarAdministracionDenegada(dao, "Cuenta marcada inactiva");
        entrar(5, "ADMIN");
        verificar(!SESION.iniciarSesion(usuario(0, "ADMIN")), "ID cero no inicia sesión");
        comprobarAdministracionDenegada(dao, "Intento inválido limpia sesión anterior");
        entrar(5, "ADMIN");
        SESION.getUsuarioActual().setRol("CAJERO");
        comprobarAdministracionDenegada(dao, "Rol modificado no conserva privilegios");
        System.out.println("OK - Denegacion sin JDBC, tres roles, cierre y cuenta inactiva");
    }

    private static void probarValidacionesDeAlta() {
        entrar(1, "ADMIN");
        UsuarioDao dao = new UsuarioDao(() -> {
            throw new AssertionError("Los datos invalidos alcanzaron JDBC");
        });
        verificar(!registrar(dao, "x", "ADMIN"), "Username corto");
        verificar(!registrar(dao, "valido", "SUPERADMIN"), "Rol desconocido");
        verificar(!dao.registrarUsuario("valido", "texto", "ADMIN", "Ana", "Perez", "a@example.test"),
                "La clave debe llegar con hash SHA-256");
        verificar(!dao.registrarUsuario("valido", HASH, "ADMIN", "", "Perez", "a@example.test"),
                "Nombre obligatorio");
        verificar(!dao.registrarUsuario("valido", HASH, "ADMIN", "Ana", "Perez", "invalido"),
                "Correo invalido");
        verificar(!dao.actualizarPassword(1, "123"), "Clave nueva muy corta");
        System.out.println("OK - Validaciones administrativas antes de JDBC");
    }

    private static void probarFlujoAdministrativo() {
        BaseSimulada base = new BaseSimulada();
        UsuarioDao dao = new UsuarioDao(base::conectar);
        entrar(1, "ADMINISTRADOR");
        verificar(registrar(dao, "persona.nueva", "BODEGUERO"), "ADMINISTRADOR puede dar de alta");
        verificar(dao.existeUsername("persona.nueva"), "El username queda registrado");
        verificar(!registrar(dao, "persona.nueva", "BODEGA"), "El indice unico rechaza duplicados");
        List<Usuario> usuarios = dao.listarUsuarios();
        verificar(usuarios.size() == 1, "Listado contiene alta");
        Usuario creado = usuarios.get(0);
        verificar(creado.getRol().equals("BODEGA") && creado.isActivo(), "Rol normalizado y alta activa");
        verificar(dao.desactivarUsuario(creado.getId()), "Desactivar otra cuenta");
        verificar(!dao.listarUsuarios().get(0).isActivo(), "Listado refleja desactivacion");
        verificar(dao.activarUsuario(creado.getId()), "Reactivar otra cuenta");
        verificar(dao.listarUsuarios().get(0).isActivo(), "Listado refleja reactivacion");
        for (String rol : List.of("ADMIN", "CAJERO", "BODEGA")) {
            entrar(creado.getId(), rol);
            verificar(dao.validarPasswordActual(creado.getId(), "ClavePrueba9"), "Clave propia: " + rol);
            verificar(!dao.validarPasswordActual(creado.getId(), "incorrecta"), "Clave incorrecta: " + rol);
            verificar(dao.actualizarPassword(creado.getId(), "ClaveNueva9"), "Cada rol cambia clave propia");
            verificar(dao.validarPasswordActual(creado.getId(), "ClaveNueva9"), "Hash actualizado");
            verificar(dao.actualizarPassword(creado.getId(), "ClavePrueba9"), "Restauracion de dato simulado");
        }
        verificar(base.conexiones == base.cierres, "Todas las conexiones se cierran");
        System.out.println("OK - Alta, duplicado, listado, activacion y claves propias de los tres roles");
    }

    private static void probarMatrizRoles() {
        verificar(Rol.ADMIN.tienePermiso("GESTION_USUARIOS"), "Solo administrador gestiona usuarios");
        verificar(!Rol.CAJERO.tienePermiso("GESTION_USUARIOS"), "Cajero sin usuarios");
        verificar(!Rol.BODEGA.tienePermiso("GESTION_USUARIOS"), "Bodega sin usuarios");
        verificar(Rol.ADMIN.tienePermiso("VENTAS") && Rol.CAJERO.tienePermiso("VENTAS"), "Admin y cajero venden");
        verificar(!Rol.BODEGA.tienePermiso("VENTAS"), "Bodega no vende");
        verificar(Rol.ADMIN.tienePermiso("ENTRADAS_SALIDAS") && Rol.BODEGA.tienePermiso("ENTRADAS_SALIDAS"),
                "Admin y bodega modifican inventario");
        verificar(!Rol.CAJERO.tienePermiso("ENTRADAS_SALIDAS"), "Cajero no modifica inventario");
        for (Rol rol : Rol.values()) {
            verificar(rol.tienePermiso("CONSULTAR_PRODUCTOS"), "Todos consultan productos");
            verificar(!rol.tienePermiso("PERMISO_DESCONOCIDO"), "Permiso desconocido denegado");
        }
        SESION.cerrarSesion();
        Usuario inactivo = usuario(2, "ADMIN");
        inactivo.setActivo(false);
        verificar(!SESION.iniciarSesion(inactivo), "No iniciar cuenta inactiva");
        verificar(!SESION.iniciarSesion(usuario(2, "ROL_INVALIDO")), "No iniciar rol desconocido");
        System.out.println("OK - Matriz de permisos de ADMIN, CAJERO y BODEGA");
    }

    private static class BaseSimulada {
        private final Map<Integer, Map<String, Object>> usuarios = new LinkedHashMap<>();
        int conexiones;
        int cierres;

        @SuppressWarnings("unchecked")
        private static <T> T proxy(Class<T> tipo, InvocationHandler handler) {
            return (T) Proxy.newProxyInstance(tipo.getClassLoader(), new Class<?>[]{tipo}, handler);
        }

        Connection conectar() {
            conexiones++;
            return proxy(Connection.class, (obj, metodo, args) -> switch (metodo.getName()) {
                case "prepareStatement" -> statement((String) args[0], false);
                case "prepareCall" -> statement((String) args[0], true);
                case "close" -> { cierres++; yield null; }
                default -> throw new UnsupportedOperationException(metodo.getName());
            });
        }

        private PreparedStatement statement(String sql, boolean callable) {
            Map<Integer, Object> parametros = new HashMap<>();
            InvocationHandler handler = (obj, metodo, args) -> {
                String nombre = metodo.getName();
                if (nombre.startsWith("set")) { parametros.put((int) args[0], args[1]); return null; }
                if (nombre.equals("close")) return null;
                if (nombre.equals("execute")) {
                    String username = (String) parametros.get(1);
                    if (usuarios.values().stream().anyMatch(u -> username.equals(u.get("username")))) {
                        throw new SQLException("Username duplicado (simulado)");
                    }
                    int id = usuarios.size() + 10;
                    Map<String, Object> fila = new HashMap<>();
                    fila.put("id", id); fila.put("username", username);
                    fila.put("password_hash", parametros.get(2)); fila.put("rol", parametros.get(3));
                    fila.put("nombre", parametros.get(4)); fila.put("apellido", parametros.get(5));
                    fila.put("correo", parametros.get(6)); fila.put("activo", true);
                    usuarios.put(id, fila);
                    return false;
                }
                if (nombre.equals("executeUpdate")) {
                    Map<String, Object> usuario = usuarios.get((Integer) parametros.get(2));
                    if (usuario == null) return 0;
                    usuario.put(sql.contains("password_hash") ? "password_hash" : "activo", parametros.get(1));
                    return 1;
                }
                if (nombre.equals("executeQuery")) {
                    List<Map<String, Object>> filas = new ArrayList<>(usuarios.values());
                    if (sql.contains("username = ?")) {
                        filas.removeIf(u -> !parametros.get(1).equals(u.get("username")));
                    } else if (sql.contains("WHERE id = ?")) {
                        filas.removeIf(u -> !parametros.get(1).equals(u.get("id")));
                    }
                    return resultado(filas);
                }
                throw new UnsupportedOperationException(nombre + " " + sql);
            };
            return callable ? proxy(CallableStatement.class, handler) : proxy(PreparedStatement.class, handler);
        }

        private ResultSet resultado(List<Map<String, Object>> filas) {
            int[] indice = {-1};
            return proxy(ResultSet.class, (obj, metodo, args) -> switch (metodo.getName()) {
                case "next" -> ++indice[0] < filas.size();
                case "getString", "getInt", "getBoolean" -> filas.get(indice[0]).get((String) args[0]);
                case "close" -> null;
                default -> throw new UnsupportedOperationException(metodo.getName());
            });
        }
    }

    public static void main(String[] args) {
        try {
            probarMatrizRoles();
            probarDenegacionAntesDeJDBC();
            probarValidacionesDeAlta();
            probarFlujoAdministrativo();
            System.out.println("TODAS LAS PRUEBAS DE ADMINISTRACION PASARON");
        } finally {
            SESION.cerrarSesion();
        }
    }
}
