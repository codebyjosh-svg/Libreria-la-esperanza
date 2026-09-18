package org.esperanza.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.esperanza.model.Categoria;
import org.esperanza.util.Conexion;

public class CategoriaDao {

    public List<Categoria> listarCategorias() throws SQLException {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT * FROM categorias";
        
        try (Connection conexion = Conexion.getInstancia().conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Categoria cat = new Categoria();
                cat.setIdCategoria(rs.getInt("id_categoria"));
                cat.setNombre(rs.getString("nombre"));
                cat.setDescripcion(rs.getString("descripcion"));
                lista.add(cat);
            }
        }
        return lista;
    }

    public void insertarCategoria(Categoria categoria) throws SQLException {
        String sql = "INSERT INTO categorias (nombre, descripcion) VALUES (?, ?)";
        
        try (Connection conexion = Conexion.getInstancia().conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            
            ps.setString(1, categoria.getNombre());
            ps.setString(2, categoria.getDescripcion());
            ps.executeUpdate();
        }
    }

    public void actualizarCategoria(Categoria categoria) throws SQLException {
        String sql = "UPDATE categorias SET nombre = ?, descripcion = ? WHERE id_categoria = ?";
        
        try (Connection conexion = Conexion.getInstancia().conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            
            ps.setString(1, categoria.getNombre());
            ps.setString(2, categoria.getDescripcion());
            ps.setInt(3, categoria.getIdCategoria());
            ps.executeUpdate();
        }
    }
}