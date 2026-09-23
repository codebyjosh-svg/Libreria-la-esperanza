package org.esperanza.test;

import java.sql.SQLException;
import org.esperanza.model.Usuario;
import org.esperanza.service.AutenticacionService;
import org.esperanza.service.ResultadoLogin;
import org.esperanza.dao.UsuarioDao;
import org.esperanza.util.PasswordUtil;

/** Pruebas de autenticación sin necesitar una base MySQL instalada. */
public class PruebaAutenticacion {

    private static final class UsuarioDaoFalso extends UsuarioDao {
        Usuario validado;
        Usuario existente;
        SQLException falloLogin;
        SQLException falloBusqueda;
        String nombreRecibido;
        String hashRecibido;
        int busquedas;

        UsuarioDaoFalso() {
            super(() -> { throw new AssertionError("No debe conectarse a MySQL"); });
        }

        @Override
        public Usuario iniciarSesion(String username, String hash) throws SQLException {
            nombreRecibido = username;
            hashRecibido = hash;
            if (falloLogin != null) throw falloLogin;
            return validado;
        }

        @Override
        public Usuario buscarPorUsername(String username) throws SQLException {
            busquedas++;
            if (falloBusqueda != null) throw falloBusqueda;
            return existente;
        }
    }

    private static Usuario usuario(boolean activo) {
        return new Usuario(7, "diego", "CAJERO", "Diego", "Torres", "", activo);
    }

    private static void estado(ResultadoLogin resultado, ResultadoLogin.Estado esperado) {
        PruebaVentas.verificar(resultado.getEstado() == esperado,
                "Se esperaba " + esperado + ", se obtuvo " + resultado.getEstado());
    }

    public static void main(String[] args) {
        UsuarioDaoFalso base = new UsuarioDaoFalso();
        AutenticacionService servicio = new AutenticacionService(base);

        estado(servicio.autenticar(" ", "abc"), ResultadoLogin.Estado.CAMPOS_VACIOS);
        estado(servicio.autenticar("diego", null), ResultadoLogin.Estado.CAMPOS_VACIOS);
        PruebaVentas.verificar(base.nombreRecibido == null,
                "Los campos vacíos no deben consultar la base");

        base.validado = usuario(true);
        ResultadoLogin correcto = servicio.autenticar("  diego  ", "secreto");
        estado(correcto, ResultadoLogin.Estado.EXITO);
        PruebaVentas.verificar(correcto.esExitoso() && correcto.getUsuario() == base.validado,
                "Debe devolver al usuario válido");
        PruebaVentas.verificar("diego".equals(base.nombreRecibido)
                        && PasswordUtil.hashSHA256("secreto").equals(base.hashRecibido)
                        && base.busquedas == 0,
                "Debe normalizar el nombre y enviar el hash al procedimiento");

        base.validado = usuario(false);
        ResultadoLogin inactivo = servicio.autenticar("diego", "secreto");
        estado(inactivo, ResultadoLogin.Estado.USUARIO_INACTIVO);
        PruebaVentas.verificar(inactivo.getUsuario() == null,
                "No se debe entregar una sesión para un usuario inactivo");

        base.validado = null;
        base.existente = null;
        estado(servicio.autenticar("desconocido", "secreto"),
                ResultadoLogin.Estado.USUARIO_NO_ENCONTRADO);
        base.existente = usuario(true);
        estado(servicio.autenticar("diego", "erronea"),
                ResultadoLogin.Estado.CONTRASENA_INCORRECTA);
        base.existente = usuario(false);
        estado(servicio.autenticar("diego", "erronea"),
                ResultadoLogin.Estado.USUARIO_INACTIVO);

        base.falloLogin = new SQLException("Base inaccesible");
        estado(servicio.autenticar("diego", "secreto"),
                ResultadoLogin.Estado.ERROR_BASE_DATOS);
        base.falloLogin = null;
        base.falloBusqueda = new SQLException("Procedimiento no disponible");
        estado(servicio.autenticar("diego", "secreto"),
                ResultadoLogin.Estado.ERROR_BASE_DATOS);

        System.out.println("OK - T4.I.3: autenticación y errores JDBC");
    }
}
