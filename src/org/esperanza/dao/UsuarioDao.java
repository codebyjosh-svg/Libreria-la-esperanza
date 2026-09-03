package org.esperanza.dao;

import org.esperanza.util.Conexion;
import org.esperanza.util.HashUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDao {

    public boolean validarPasswordActual(int idUsuario, String passwordIngresada) {
        String sql = "SELECT password_hash FROM usuarios WHERE id = ?";
        String hashIngresado = HashUtil.sha256(passwordIngresada);
        
        System.out.println("--- DEPURACIÓN CAMBIO DE CONTRASEÑA ---");
        System.out.println("ID Usuario consultado: " + idUsuario);
        System.out.println("Contraseña ingresada en pantalla: " + passwordIngresada);
        System.out.println("Hash generado por Java: " + hashIngresado);

        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String hashDb = rs.getString("password_hash");
                    System.out.println("Hash encontrado en la Base de Datos: " + hashDb);
                    boolean coincide = hashDb.equals(hashIngresado);
                    System.out.println("¿Los hashes coinciden?: " + coincide);
                    return coincide;
                } else {
                    System.out.println("¡Alerta! No se encontró ningún usuario con el ID: " + idUsuario);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean actualizarPassword(int idUsuario, String nuevaPassword) {
        String sql = "UPDATE usuarios SET password_hash = ? WHERE id = ?";
        String nuevoHash = HashUtil.sha256(nuevaPassword);
        
        try (Connection conn = Conexion.getInstancia().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nuevoHash);
            stmt.setInt(2, idUsuario);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}