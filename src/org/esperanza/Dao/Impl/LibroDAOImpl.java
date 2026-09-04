package org.esperanza.Dao.Impl;

import org.esperanza.Dao.LibroDAO;
import bd.Conexion;
import org.esperanza.Model.Libro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LibroDAOImpl implements LibroDAO {

    @Override
    public Libro buscarPorIsbn(String isbn) {
        Libro libro = null;
        String sql = "SELECT * FROM libros WHERE isbn = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    libro = extraerLibro(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return libro;
    }

   