package org.esperanza.Service;

import org.esperanza.Model.Rol;
import org.esperanza.Model.Usuario;
import org.esperanza.dao.AuditoriaDao;

/**
 * Mantiene la sesión del usuario actual del sistema.
 */
public final class SesionUsuario {

    private static SesionUsuario instancia;

    private Usuario usuarioActual;
    private Rol rolActual;
    private long auditoriaId = -1;

    private final AuditoriaDao auditoriaDao =
            new AuditoriaDao();

    private SesionUsuario() {
    }

    public static synchronized SesionUsuario getInstancia() {

        if (instancia == null) {
            instancia = new SesionUsuario();
        }

        return instancia;
    }

    /**
     * T1.16
     * Inicia la sesión únicamente si el usuario
     * existe, está activo y posee un rol válido.
     */
    public synchronized boolean iniciarSesion(
            Usuario usuario) {

        if (usuario == null || !usuario.isActivo()) {
            return false;
        }

        Rol nuevoRol =
                Rol.fromString(
                        usuario.getRol()
                );

        if (nuevoRol == null) {
            return false;
        }

        // Si ya está iniciada la misma sesión
        if (usuarioActual != null
                && usuarioActual.getId()
                == usuario.getId()
                && rolActual == nuevoRol) {

            return true;
        }

        cerrarSesion();

        usuarioActual = usuario;
        rolActual = nuevoRol;

        auditoriaId =
                auditoriaDao.iniciarSesion(
                        usuario.getId()
                );

        return true;
    }

    public synchronized void cerrarSesion() {

        if (auditoriaId > 0) {

            auditoriaDao.cerrarSesion(
                    auditoriaId
            );
        }

        auditoriaId = -1;
        usuarioActual = null;
        rolActual = null;
    }

    public void registrarCambio(
            String tipo,
            String detalle) {

        if (usuarioActual != null) {

            auditoriaDao.registrarCambio(
                    usuarioActual.getId(),
                    tipo,
                    detalle
            );
        }
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

        String nombreCompleto =
                (nombre + " " + apellido)
                        .trim();

        if (!nombreCompleto.isEmpty()) {
            return nombreCompleto;
        }

        return usuarioActual.getUsrname() == null
                ? "Usuario"
                : usuarioActual.getUsrname();
    }

    public boolean tienePermiso(
            String permiso) {

        return rolActual != null
                && rolActual.tienePermiso(
                        permiso
                );
    }

    public boolean esAdmin() {
        return rolActual == Rol.ADMIN;
    }

    public boolean esCajero() {
        return rolActual == Rol.CAJERO;
    }

    public boolean esBodega() {
        return rolActual == Rol.BODEGA;
    }
}