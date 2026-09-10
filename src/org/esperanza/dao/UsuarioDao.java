package org.esperanza.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.esperanza.Model.Usuario;
import org.esperanza.util.Conexion;
import org.esperanza.util.PasswordUtil;

public class UsuarioDao {

    public Usuario iniciarSesion(String username, String passwordHash) {

        Usuario usuario = null;

        String sql = "{call sp_iniciar_sesion(?, ?)}";

        try (
            Connection conexion = Conexion.getInstancia().conectar();
            CallableStatement consultaCall = conexion.prepareCall(sql)
        ) {

            consultaCall.setString(1, username);
            consultaCall.setString(2, passwordHash);

            try (ResultSet rs = consultaCall.executeQuery()) {

                if (rs.next()) {
                    usuario = mapearUsuario(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println(
                    "No es posible iniciar sesión: " + e.getMessage()
            );
        }

        return usuario;
    }

    public Usuario buscarPorUsername(String username) {

        Usuario usuario = null;

        String sql = "{call sp_buscar_usuario(?)}";

        try (
            Connection conexion = Conexion.getInstancia().conectar();
            CallableStatement consultaCall = conexion.prepareCall(sql)
        ) {

            consultaCall.setString(1, username);

            try (ResultSet rs = consultaCall.executeQuery()) {

                if (rs.next()) {
                    usuario = mapearUsuario(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println(
                    "Error al buscar usuario: " + e.getMessage()
            );
        }

        return usuario;
    }

    public List<Usuario> listarUsuarios() {

        List<Usuario> usuarios = new ArrayList<>();

        String sql =
                "SELECT id, username, rol, nombre, apellido, correo, activo "
                + "FROM usuarios ORDER BY id";

        try (
            Connection conexion = Conexion.getInstancia().conectar();
            PreparedStatement ps = conexion.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }

        } catch (SQLException e) {
            System.err.println(
                    "Error al listar usuarios: " + e.getMessage()
            );
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

        String sql =
                "{call sp_registrar_usuario(?, ?, ?, ?, ?, ?)}";

        try (
            Connection conexion = Conexion.getInstancia().conectar();
            CallableStatement call = conexion.prepareCall(sql)
        ) {

            call.setString(1, username);
            call.setString(2, passwordHash);
            call.setString(3, rol);
            call.setString(4, nombre);
            call.setString(5, apellido);
            call.setString(6, correo);

            call.execute();

            return true;

        } catch (SQLException e) {

            System.err.println(
                    "Error al registrar usuario: " + e.getMessage()
            );

            return false;
        }
    }


    public boolean desactivarUsuario(int id) {

        String sql =
                "UPDATE usuarios SET activo = 0 WHERE id = ?";

        try (
            Connection conexion = Conexion.getInstancia().conectar();
            PreparedStatement ps = conexion.prepareStatement(sql)
        ) {

            ps.setInt(1, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            System.err.println(
                    "Error al desactivar usuario: " + e.getMessage()
            );

            return false;
        }
    }

    public boolean existeUsername(String username) {

        String sql =
                "SELECT 1 FROM usuarios WHERE username = ? LIMIT 1";

        try (
            Connection conexion = Conexion.getInstancia().conectar();
            PreparedStatement ps = conexion.prepareStatement(sql)
        ) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error al validar username: " + e.getMessage()
            );

            return false;
        }
    }

    public boolean validarPasswordActual(
            int idUsuario,
            String passwordIngresada) {

        String sql =
                "SELECT password_hash FROM usuarios WHERE id = ?";

        String hashIngresado =
                PasswordUtil.hashSHA256(passwordIngresada);

        try (
            Connection conexion = Conexion.getInstancia().conectar();
            PreparedStatement ps = conexion.prepareStatement(sql)
        ) {

            ps.setInt(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    String hashBaseDatos =
                            rs.getString("password_hash");

                    return hashBaseDatos != null
                            && hashBaseDatos.equals(hashIngresado);
                }
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error al validar contraseña actual: "
                    + e.getMessage()
            );
        }

        return false;
    }


    public boolean actualizarPassword(
            int idUsuario,
            String nuevaPassword) {

        String sql =
                "UPDATE usuarios SET password_hash = ? WHERE id = ?";

        String nuevoHash =
                PasswordUtil.hashSHA256(nuevaPassword);

        try (
            Connection conexion = Conexion.getInstancia().conectar();
            PreparedStatement ps = conexion.prepareStatement(sql)
        ) {

            ps.setString(1, nuevoHash);
            ps.setInt(2, idUsuario);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            System.err.println(
                    "Error al actualizar contraseña: "
                    + e.getMessage()
            );

            return false;
        }
    }

    // =====================================================
    // MAPEAR USUARIO
    // =====================================================

    private Usuario mapearUsuario(ResultSet rs)
            throws SQLException {

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