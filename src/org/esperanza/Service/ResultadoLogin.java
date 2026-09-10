package org.esperanza.Service;

import org.esperanza.Model.Usuario;

public class ResultadoLogin {

    public enum Estado {
        EXITO,
        CAMPOS_VACIOS,
        USUARIO_NO_ENCONTRADO,
        CONTRASENA_INCORRECTA,
        USUARIO_INACTIVO
    }

    private final Estado estado;
    private final Usuario usuario;

    public ResultadoLogin(Estado estado, Usuario usuario) {
        this.estado = estado;
        this.usuario = usuario;
    }

    public Estado getEstado() {
        return estado;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public boolean esExitoso() {
        return estado == Estado.EXITO;
    }
}