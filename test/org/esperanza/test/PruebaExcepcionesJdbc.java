package org.esperanza.test;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.esperanza.service.AutenticacionService;
import org.esperanza.service.ResultadoLogin;
import org.esperanza.dao.UsuarioDao;

/** Recorre el DAO real con conexiones JDBC simuladas. */
public class PruebaExcepcionesJdbc {

    private static final class ConexionFalsa {
        boolean conexionCerrada;
        boolean procedimientoCerrado;
        boolean resultadoCerrado;
        boolean fallarConsulta;
        boolean columnaActivoFaltante;

        Connection conectar() {
            return PruebaVentas.BaseSimulada.proxy(Connection.class, (proxy, metodo, args) ->
                    switch (metodo.getName()) {
                        case "prepareCall" -> procedimiento();
                        case "close" -> {
                            conexionCerrada = true;
                            yield null;
                        }
                        default -> throw new UnsupportedOperationException(metodo.getName());
                    });
        }

        CallableStatement procedimiento() {
            return PruebaVentas.BaseSimulada.proxy(CallableStatement.class, (proxy, metodo, args) ->
                    switch (metodo.getName()) {
                        case "setString" -> null;
                        case "executeQuery" -> {
                            if (fallarConsulta) throw new SQLException("Fallo de JDBC");
                            yield resultado();
                        }
                        case "close" -> {
                            procedimientoCerrado = true;
                            yield null;
                        }
                        default -> throw new UnsupportedOperationException(metodo.getName());
                    });
        }

        ResultSet resultado() {
            boolean[] leido = {false};
            return PruebaVentas.BaseSimulada.proxy(ResultSet.class, (proxy, metodo, args) ->
                    switch (metodo.getName()) {
                        case "next" -> {
                            if (leido[0]) yield false;
                            leido[0] = true;
                            yield columnaActivoFaltante;
                        }
                        case "getInt" -> 1;
                        case "getString" -> switch ((String) args[0]) {
                            case "username", "nombre", "apellido" -> "usuario";
                            case "rol" -> "CAJERO";
                            case "correo" -> "";
                            default -> throw new SQLException("Columna desconocida");
                        };
                        case "getBoolean" -> throw new SQLException("Falta columna activo");
                        case "close" -> {
                            resultadoCerrado = true;
                            yield null;
                        }
                        default -> throw new UnsupportedOperationException(metodo.getName());
                    });
        }
    }

    public static void main(String[] args) throws SQLException {
        ConexionFalsa base = new ConexionFalsa();
        base.fallarConsulta = true;
        UsuarioDao dao = new UsuarioDao(base::conectar);
        try {
            dao.iniciarSesion("usuario", "hash");
            throw new AssertionError("No debe convertir un SQLException en usuario inexistente");
        } catch (SQLException esperado) {
            PruebaVentas.verificar(base.conexionCerrada && base.procedimientoCerrado,
                    "La excepción debe cerrar statement y conexión");
        }

        base = new ConexionFalsa();
        dao = new UsuarioDao(base::conectar);
        PruebaVentas.verificar(dao.buscarPorUsername("usuario") == null
                        && base.resultadoCerrado && base.procedimientoCerrado
                        && base.conexionCerrada,
                "La ausencia de usuario debe cerrar los recursos JDBC");

        base = new ConexionFalsa();
        base.columnaActivoFaltante = true;
        dao = new UsuarioDao(base::conectar);
        try {
            dao.iniciarSesion("usuario", "hash");
            throw new AssertionError("No debe autorizar usuarios sin campo activo");
        } catch (SQLException esperado) {
            PruebaVentas.verificar(base.resultadoCerrado && base.procedimientoCerrado
                            && base.conexionCerrada,
                    "Un error al leer el usuario también debe cerrar los recursos");
        }

        // El servicio traduce un fallo de conexión a un resultado apto para la UI.
        ResultadoLogin resultado = new AutenticacionService(
                new UsuarioDao(() -> { throw new SQLException("Conexión caída"); }))
                .autenticar("usuario", "secreto");
        PruebaVentas.verificar(resultado.getEstado() == ResultadoLogin.Estado.ERROR_BASE_DATOS
                        && resultado.getUsuario() == null,
                "Un error JDBC no debe aparecer como credenciales incorrectas");

        System.out.println("OK - T4.I.11: excepciones y cierre de recursos JDBC");
    }
}
