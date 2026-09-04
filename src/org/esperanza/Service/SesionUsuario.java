package org.esperanza.Service;

import org.esperanza.Model.Rol;
import org.esperanza.Model.Usuario;
import org.esperanza.Dao.AuditoriaDao;

/**
 * Usuario seleccionado para el uso actual del sistema.
 * No realiza autenticación: solo mantiene el usuario que está utilizando la aplicación.
 */
public final class SesionUsuario {
    private static SesionUsuario instancia;
    private Usuario usuarioActual;
    private Rol rolActual;
    private long auditoriaId = -1;
    private final AuditoriaDao auditoriaDao = new AuditoriaDao();

    private SesionUsuario() {}

    public static synchronized SesionUsuario getInstancia() {
        if (instancia == null) instancia = new SesionUsuario();
        return instancia;
    }

    public synchronized void iniciarSesion(Usuario usuario) {
        if (usuario == null) return;
        if (usuarioActual != null && usuarioActual.getId() == usuario.getId()) return;
        cerrarSesion();
        usuarioActual = usuario;
        rolActual = Rol.fromString(usuario.getRol());
        auditoriaId = auditoriaDao.iniciarSesion(usuario.getId());
    }

    public synchronized void cerrarSesion() {
        if (auditoriaId > 0) auditoriaDao.cerrarSesion(auditoriaId);
        auditoriaId = -1;
        usuarioActual = null;
        rolActual = null;
    }

    public void registrarCambio(String tipo, String detalle) {
        if (usuarioActual != null) auditoriaDao.registrarCambio(usuarioActual.getId(), tipo, detalle);
    }

    public boolean haySesionActiva() { return usuarioActual != null; }
    public Usuario getUsuarioActual() { return usuarioActual; }
    public Rol getRolActual() { return rolActual; }

    public String getNombreCompleto() {
        if (usuarioActual == null) return "Sin usuario seleccionado";
        String n = usuarioActual.getNombre() == null ? "" : usuarioActual.getNombre();
        String a = usuarioActual.getApellido() == null ? "" : usuarioActual.getApellido();
        return (n + " " + a).trim();
    }

    public boolean tienePermiso(String permiso) {
        return rolActual != null && rolActual.tienePermiso(permiso);
    }
    public boolean esAdmin() { return rolActual == Rol.ADMIN; }
    public boolean esCajero() { return rolActual == Rol.CAJERO; }
    public boolean esBodega() { return rolActual == Rol.BODEGA; }
}
