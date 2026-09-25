package org.esperanza.Service;

import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.esperanza.dao.UsuarioDao;
import org.esperanza.Model.Usuario;
import org.esperanza.util.PasswordUtil;

public class AutenticacionService {

    private static final Logger LOGGER =
            Logger.getLogger(
                    AutenticacionService.class.getName()
            );

    private final UsuarioDao usuarioDao;

    public AutenticacionService() {
        this(new UsuarioDao());
    }

    public AutenticacionService(
            UsuarioDao usuarioDao) {

        this.usuarioDao =
                Objects.requireNonNull(
                        usuarioDao,
                        "usuarioDao"
                );
    }

    public ResultadoLogin autenticar(
            String username,
            String passwordPlano) {

        if (username == null
                || username.trim().isEmpty()
                || passwordPlano == null
                || passwordPlano.isEmpty()) {

            return new ResultadoLogin(
                    ResultadoLogin.Estado.CAMPOS_VACIOS,
                    null
            );
        }

        String nombre = username.trim();

        String passwordHash =
                PasswordUtil.hashSHA256(
                        passwordPlano
                );

        try {

            Usuario usuario =
                    usuarioDao.iniciarSesion(
                            nombre,
                            passwordHash
                    );

            if (usuario != null) {

                if (!usuario.isActivo()) {

                    return new ResultadoLogin(
                            ResultadoLogin.Estado.USUARIO_INACTIVO,
                            null
                    );
                }

                return new ResultadoLogin(
                        ResultadoLogin.Estado.EXITO,
                        usuario
                );
            }

            Usuario existente =
                    usuarioDao.buscarPorUsername(
                            nombre
                    );

            if (existente == null) {

                return new ResultadoLogin(
                        ResultadoLogin.Estado.USUARIO_NO_ENCONTRADO,
                        null
                );
            }

            if (!existente.isActivo()) {

                return new ResultadoLogin(
                        ResultadoLogin.Estado.USUARIO_INACTIVO,
                        null
                );
            }

            return new ResultadoLogin(
                    ResultadoLogin.Estado.CONTRASENA_INCORRECTA,
                    null
            );

        } catch (SQLException | IllegalStateException ex) {

            LOGGER.log(
                    Level.WARNING,
                    "Error JDBC al autenticar usuario",
                    ex
            );

            return new ResultadoLogin(
                    ResultadoLogin.Estado.ERROR_BASE_DATOS,
                    null
            );
        }
    }
}