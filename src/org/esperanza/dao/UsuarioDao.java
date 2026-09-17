package org.esperanza.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.esperanza.Model.Usuario;
import org.esperanza.util.Conexion;
import org.esperanza.util.PasswordUtil;

public class UsuarioDao {

    public Usuario iniciarSesion(String username, String passwordHash) {
        String sql = "{call sp_iniciar_sesion(?, ?)}";

        try (Connection con = Conexion.getInstancia().conectar();
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

        try (Connection con = Conexion.getInstancia().conectar();
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

        String sql = """
                SELECT id, username, rol, nombre, apellido, correo, activo
                FROM usuarios
                ORDER BY id
                """;

        try (Connection con = Conexion.getInstancia().conectar();
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

        String sql = "{call sp_registrar_usuario(?, ?, ?, ?, ?, ?)}";

        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setString(1, username);
            cs.setString(2, passwordHash);
            cs.setString(3, rol);
            cs.setString(4, nombre);
            cs.setString(5, apellido);
            cs.setString(6, correo);
            cs.execute();

            return true;

        } catch (SQLException e) {
            System.err.println("Error al registrar usuario: " + e.getMessage());
            return false;
        }
    }

    public boolean cambiarEstadoUsuario(int id, boolean activo) {
        String sql = "UPDATE usuarios SET activo = ? WHERE id = ?";

        try (Connection con = Conexion.getInstancia().conectar();
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
        String sql = "SELECT 1 FROM usuarios WHERE username = ? LIMIT 1";

        try (Connection con = Conexion.getInstancia().conectar();
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
        String sql = "SELECT password_hash FROM usuarios WHERE id = ?";
        String hash = PasswordUtil.hashSHA256(password);

        try (Connection con = Conexion.getInstancia().conectar();
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
        String sql = "UPDATE usuarios SET password_hash = ? WHERE id = ?";

        try (Connection con = Conexion.getInstancia().conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, PasswordUtil.hashSHA256(nuevaPassword));
            ps.setInt(2, idUsuario);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar contraseña: " + e.getMessage());
            return false;
        }
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

        try {
            usuario.setActivo(rs.getBoolean("activo"));
        } catch (SQLException ignored) {
            usuario.setActivo(true);
        }

        return usuario;
    }
}