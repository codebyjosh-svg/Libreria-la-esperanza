package org.esperanza.Service;

import org.esperanza.Model.Rol;
import org.esperanza.Model.Usuario;

public final class SesionUsuario {

    private static SesionUsuario instancia;

    private Usuario usuarioActual;
    private Rol rolActual;

    private SesionUsuario() {
    }

    public static synchronized SesionUsuario getInstancia() {

        if (instancia == null) {

            instancia =
                    new SesionUsuario();
        }

        return instancia;
    }

    public synchronized boolean iniciarSesion(
            Usuario usuario) {

        if (usuario == null) {
            return false;
        }

        if (!usuario.isActivo()) {
            return false;
        }

        Rol rol =
                Rol.fromString(
                        usuario.getRol()
                );

        if (rol == null) {
            return false;
        }

        usuarioActual =
                usuario;

        rolActual =
                rol;

        return true;
    }

    public synchronized void cerrarSesion() {

        usuarioActual =
                null;

        rolActual =
                null;
    }

    public boolean haySesionActiva() {

        return usuarioActual != null
                && rolActual != null;
    }

    public Usuario getUsuarioActual() {

        return usuarioActual;
    }

    public Rol getRolActual() {

        return rolActual;
    }

    public boolean tienePermiso(
            String permiso) {

        return rolActual != null
                && rolActual
                        .tienePermiso(
                                permiso
                        );
    }

    public boolean esAdmin() {

        return rolActual
                == Rol.ADMIN;
    }

    public boolean esCajero() {

        return rolActual
                == Rol.CAJERO;
    }

    public boolean esBodega() {

        return rolActual
                == Rol.BODEGA;
    }

    public String getNombreCompleto() {

        if (usuarioActual == null) {
            return "Sin usuario";
        }

        String nombre =
                usuarioActual.getNombre() == null
                        ? ""
                        : usuarioActual
                                .getNombre()
                                .trim();

        String apellido =
                usuarioActual.getApellido() == null
                        ? ""
                        : usuarioActual
                                .getApellido()
                                .trim();

        String completo =
                (nombre + " " + apellido)
                        .trim();

        if (!completo.isEmpty()) {

            return completo;
        }

        if (usuarioActual.getUsrname()
                != null) {

            return usuarioActual
                    .getUsrname();
        }

        return "Usuario";
    }
}