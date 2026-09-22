package org.esperanza.Service;

import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.esperanza.Model.Usuario;
import org.esperanza.dao.UsuarioDao;
import org.esperanza.util.PasswordUtil;

public class AutenticacionService {

    private static final Logger LOGGER = Logger.getLogger(AutenticacionService.class.getName());
    private final UsuarioDao usuarioDao;

    public AutenticacionService() {
        this(new UsuarioDao());
    }

    public AutenticacionService(UsuarioDao usuarioDao) {
        this.usuarioDao = Objects.requireNonNull(usuarioDao, "usuarioDao");
    }

    public ResultadoLogin autenticar(String username, String passwordPlano) {
        if (username == null || username.trim().isEmpty()
                || passwordPlano == null || passwordPlano.isEmpty()) {
            return new ResultadoLogin(ResultadoLogin.Estado.CAMPOS_VACIOS, null);
        }

        String nombre = username.trim();
        String passwordHash = PasswordUtil.hashSHA256(passwordPlano);
        try {
            Usuario usuario = usuarioDao.iniciarSesion(nombre, passwordHash);
            if (usuario != null) {
                return new ResultadoLogin(usuario.isActivo()
                        ? ResultadoLogin.Estado.EXITO
                        : ResultadoLogin.Estado.USUARIO_INACTIVO,
                        usuario.isActivo() ? usuario : null);
            }

            Usuario existente = usuarioDao.buscarPorUsername(nombre);
            if (existente == null) {
                return new ResultadoLogin(ResultadoLogin.Estado.USUARIO_NO_ENCONTRADO, null);
            }
            return new ResultadoLogin(existente.isActivo()
                    ? ResultadoLogin.Estado.CONTRASENA_INCORRECTA
                    : ResultadoLogin.Estado.USUARIO_INACTIVO, null);
        } catch (SQLException | IllegalStateException e) {
            LOGGER.log(Level.WARNING, "Error JDBC al autenticar usuario", e);
            return new ResultadoLogin(ResultadoLogin.Estado.ERROR_BASE_DATOS, null);
        }
    }
}
