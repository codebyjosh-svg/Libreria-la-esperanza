package org.esperanza.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.esperanza.model.Rol;
import org.esperanza.model.Usuario;
import org.esperanza.service.SesionUsuario;
import org.esperanza.util.Conexion;
import org.esperanza.util.PasswordUtil;

public class UsuarioDao {

    private final ProveedorConexion conexiones;

    public UsuarioDao() {
        this(() -> Conexion.getInstancia().conectar());
    }

    public UsuarioDao(ProveedorConexion conexiones) {
        this.conexiones = Objects.requireNonNull(conexiones);
    }

    private boolean esAdministrador() {
        SesionUsuario sesion = SesionUsuario.getInstancia();
        return sesion.haySesionActiva()
                && sesion.getUsuarioActual().isActivo()
                && sesion.esAdmin();
    }

    private boolean esUsuarioActual(int idUsuario) {
        SesionUsuario sesion = SesionUsuario.getInstancia();
        return idUsuario > 0 && sesion.haySesionActiva()
                && sesion.getUsuarioActual().isActivo()
                && sesion.getUsuarioActual().getId() == idUsuario;
    }

    public Usuario iniciarSesion(String username, String passwordHash) {
        String sql = "{call sp_iniciar_sesion(?, ?)}";

        try (Connection con = conexiones.conectar();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setString(1, username);
            cs.setString(2, passwordHash);

            try (ResultSet rs = cs.executeQuery()) {
                return rs.next() ? mapearUsuario(rs) : null;
            }

        } catch (SQLException e) {
            System.err.println("Error al iniciar sesión: " + e.getMessage());
            return null;
        }
    }

    public Usuario buscarPorUsername(String username) {
        String sql = "{call sp_buscar_usuario(?)}";

        try (Connection con = conexiones.conectar();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setString(1, username);

            try (ResultSet rs = cs.executeQuery()) {
                return rs.next() ? mapearUsuario(rs) : null;
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar usuario: " + e.getMessage());
            return null;
        }
    }

    public List<Usuario> listarUsuarios() {
        List<Usuario> usuarios = new ArrayList<>();
        if (!esAdministrador()) return usuarios;

        String sql = """
                SELECT id, username, rol, nombre, apellido, correo, activo
                FROM usuarios
                ORDER BY id
                """;

        try (Connection con = conexiones.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) usuarios.add(mapearUsuario(rs));

        } catch (SQLException e) {
            System.err.println("Error al listar usuarios: " + e.getMessage());
        }

        return usuarios;
    }

    public boolean registrarUsuario(
            String username,
            String passwordHash,
            String rol,
            String nombre,
            String apellido,
            String correo) {

        if (!esAdministrador() || !datosAltaValidos(
                username, passwordHash, rol, nombre, apellido, correo)) {
            return false;
        }

        String sql = "{call sp_registrar_usuario(?, ?, ?, ?, ?, ?)}";

        try (Connection con = conexiones.conectar();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setString(1, username.trim());
            cs.setString(2, passwordHash);
            cs.setString(3, Rol.fromString(rol).name());
            cs.setString(4, nombre.trim());
            cs.setString(5, apellido.trim());
            cs.setString(6, correo.trim());
            cs.execute();

            return true;

        } catch (SQLException e) {
            System.err.println("Error al registrar usuario: " + e.getMessage());
            return false;
        }
    }

    public boolean cambiarEstadoUsuario(int id, boolean activo) {
        if (!esAdministrador() || id <= 0 || esUsuarioActual(id)) return false;
        String sql = "UPDATE usuarios SET activo = ? WHERE id = ?";

        try (Connection con = conexiones.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBoolean(1, activo);
            ps.setInt(2, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al cambiar estado: " + e.getMessage());
            return false;
        }
    }

    public boolean desactivarUsuario(int id) {
        return cambiarEstadoUsuario(id, false);
    }

    public boolean activarUsuario(int id) {
        return cambiarEstadoUsuario(id, true);
    }

    public boolean existeUsername(String username) {
        if (!esAdministrador() || username == null || username.isBlank()) return false;
        String sql = "SELECT 1 FROM usuarios WHERE username = ? LIMIT 1";

        try (Connection con = conexiones.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.err.println("Error al validar username: " + e.getMessage());
            return false;
        }
    }

    public boolean validarPasswordActual(int idUsuario, String password) {
        if (!esUsuarioActual(idUsuario) || password == null || password.isEmpty()) return false;
        String sql = "SELECT password_hash FROM usuarios WHERE id = ?";
        String hash = PasswordUtil.hashSHA256(password);

        try (Connection con = conexiones.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && hash.equals(rs.getString("password_hash"));
            }

        } catch (SQLException e) {
            System.err.println("Error al validar contraseña: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizarPassword(int idUsuario, String nuevaPassword) {
        if (!esUsuarioActual(idUsuario) || nuevaPassword == null
                || nuevaPassword.isBlank() || nuevaPassword.length() < 6) return false;
        String sql = "UPDATE usuarios SET password_hash = ? WHERE id = ?";

        try (Connection con = conexiones.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, PasswordUtil.hashSHA256(nuevaPassword));
            ps.setInt(2, idUsuario);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar contraseña: " + e.getMessage());
            return false;
        }
    }

    private boolean datosAltaValidos(String username, String passwordHash, String rol,
            String nombre, String apellido, String correo) {
        return username != null && username.trim().matches("[A-Za-z0-9._-]{4,20}")
                && passwordHash != null && passwordHash.matches("[0-9a-fA-F]{64}")
                && Rol.fromString(rol) != null
                && nombre != null && nombre.trim().matches("[\\p{L} ]{2,40}")
                && apellido != null && apellido.trim().matches("[\\p{L} ]{2,40}")
                && correo != null && correo.trim().length() <= 120
                && correo.trim().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();

        usuario.setId(rs.getInt("id"));
        usuario.setUsrname(rs.getString("username"));
        usuario.setRol(rs.getString("rol"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setApellido(rs.getString("apellido"));

        try {
            usuario.setCorreo(rs.getString("correo"));
        } catch (SQLException ignored) {
        }

        // La consulta de autenticación debe declarar el estado de la cuenta.
        usuario.setActivo(rs.getBoolean("activo"));

        return usuario;
    }
}
