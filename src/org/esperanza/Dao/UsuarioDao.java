package org.esperanza.dao;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.CallableStatement;
import org.esperanza.Model.Usuario;
import org.esperanza.util.Conexion;

public class UsuarioDao {

    public Usuario iniciarSesion(String username, String passwordHash) {
        Usuario usuario = null;
        String sql = "{call sp_iniciar_sesion(?, ?)}";

        try (Connection conexion = Conexion.getInstancia().conectar();
                CallableStatement consultaCall = conexion.prepareCall(sql)) {

            consultaCall.setString(1, username);
            consultaCall.setString(2, passwordHash);

            try (ResultSet tablaResultado = consultaCall.executeQuery()) {
                if (tablaResultado.next()) {
                    usuario = new Usuario();
                    usuario.setId(tablaResultado.getInt("id"));
                    usuario.setUsrname(tablaResultado.getString("username"));
                    usuario.setRol(tablaResultado.getString("rol"));
                    usuario.setNombre(tablaResultado.getString("nombre"));
                    usuario.setApellido(tablaResultado.getString("apellido"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en iniciar sesion: " + e.getMessage());
        }
        return usuario;
    }

    // NUEVO: usado por AutenticacionService para saber por que fallo el login
    public Usuario buscarPorUsername(String username) {
        Usuario usuario = null;
        String sql = "{call sp_buscar_usuario(?)}";

        try (Connection conexion = Conexion.getInstancia().conectar();
                CallableStatement consultaCall = conexion.prepareCall(sql)) {

            consultaCall.setString(1, username);

            try (ResultSet tablaResultado = consultaCall.executeQuery()) {
                if (tablaResultado.next()) {
                    usuario = new Usuario();
                    usuario.setId(tablaResultado.getInt("id"));
                    usuario.setUsrname(tablaResultado.getString("username"));
                    usuario.setRol(tablaResultado.getString("rol"));
                    usuario.setActivo(tablaResultado.getBoolean("activo"));
                    usuario.setNombre(tablaResultado.getString("nombre"));
                    usuario.setApellido(tablaResultado.getString("apellido"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar usuario: " + e.getMessage());
        }
        return usuario;
    }

    public boolean registrarUsuario(String username, String passwordHash, String rol, String nombre, String apellido, String correo) {
        String sql = "{call sp_registrar_usuario(?, ?, ?, ?, ?, ?)}";

        try (Connection conexion = Conexion.getInstancia().conectar();
                CallableStatement consultaCall = conexion.prepareCall(sql)) {

            consultaCall.setString(1, username);
            consultaCall.setString(2, passwordHash);
            consultaCall.setString(3, rol);
            consultaCall.setString(4, nombre);
            consultaCall.setString(5, apellido);
            consultaCall.setString(6, correo);

            consultaCall.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al registrar usuario: " + e.getMessage());
            return false;
        }
    }
}