package org.esperanza.Service;

import org.esperanza.Model.Usuario;
import org.esperanza.dao.UsuarioDao;
import org.esperanza.util.PasswordUtil;

public class AutenticacionService {

    private final UsuarioDao usuarioDao = new UsuarioDao();

public ResultadoLogin autenticar(String username, String passwordPlano) {
    if (username == null || username.trim().isEmpty()
            || passwordPlano == null || passwordPlano.isEmpty()) {
        return new ResultadoLogin(ResultadoLogin.Estado.CAMPOS_VACIOS, null);
    }
    String passwordHash = PasswordUtil.hashSHA256(passwordPlano);
    Usuario usuario = usuarioDao.iniciarSesion(username.trim(), passwordHash);
    if (usuario != null) {
        return new ResultadoLogin(ResultadoLogin.Estado.EXITO, usuario);
    }
    Usuario existente = usuarioDao.buscarPorUsername(username.trim());
    if (existente == null) {
        return new ResultadoLogin(ResultadoLogin.Estado.USUARIO_NO_ENCONTRADO, null);
    }
    if (!existente.isActivo()) {
        return new ResultadoLogin(ResultadoLogin.Estado.USUARIO_INACTIVO, null);
    }
    return new ResultadoLogin(ResultadoLogin.Estado.CONTRASENA_INCORRECTA, null);
}
}