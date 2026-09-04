package org.esperanza.Dao.Impl;

import org.esperanza.Dao.LibroDAO;
import org.esperanza.Util.Conexion;
import org.esperanza.Model.Libro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LibroDAOImpl implements LibroDAO {
    private static final Logger LOGGER = Logger.getLogger(LibroDAOImpl.class.getName());

    // Consulta base uniendo la tabla de libros con autores mediante JOIN
    private static final String SELECT_BASE = 
        "SELECT l.*, GROUP_CONCAT(CONCAT(a.nombre_autor, ' ', a.apellido_autor) SEPARATOR ', ') AS nombre_autor " +
        "FROM libros l " +
        "LEFT JOIN autores_libro al ON l.isbn = al.isbn " +
        "LEFT JOIN autores a ON al.id_autor = a.id_autor ";

    @Override
    public Libro buscarPorIsbn(String isbn) {
        Libro libro = null;
        String sql = SELECT_BASE + "WHERE l.isbn = ? GROUP BY l.isbn";
        try (Connection con = Conexion.getInstancia().conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    libro = extraerLibro(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error en buscar por ISBN: " + isbn, e);
        }
        return libro;
    }

    @Override
    public List<Libro> buscarPorTitulo(String titulo) {
        List<Libro> lista = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE l.titulo LIKE ? GROUP BY l.isbn";
        try (Connection con = Conexion.getInstancia().conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + titulo + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(extraerLibro(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error en buscar por título: " + titulo, e);
        }
        return lista;
    }

    @Override
    public List<Libro> buscarPorAutor(String autor) {
        List<Libro> lista = new ArrayList<>();
        String sql = SELECT_BASE + "GROUP BY l.isbn HAVING nombre_autor LIKE ?";
        try (Connection con = Conexion.getInstancia().conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + autor + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(extraerLibro(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error en buscar por autor: " + autor, e);
        }
        return lista;
    }

    @Override
    public List<Libro> listarTodos() {
        List<Libro> lista = new ArrayList<>();
        String sql = SELECT_BASE + "GROUP BY l.isbn";
        try (Connection con = Conexion.getInstancia().conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(extraerLibro(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar todos los libros", e);
        }
        return lista;
    }

    private Libro extraerLibro(ResultSet rs) throws SQLException {
        Libro libro = new Libro();
        try { libro.setIsbn(rs.getString("isbn")); } catch (Exception e) {}
        try { libro.setTitulo(rs.getString("titulo")); } catch (Exception e) {}
        try { libro.setFechaPublicacion(rs.getString("fecha_publicacion")); } catch (Exception e) {}
        try { libro.setPrecio(rs.getDouble("precio")); } catch (Exception e) {}
        try { libro.setIdCategoria(rs.getInt("id_categoria")); } catch (Exception e) {}
        try { libro.setNitEditorial(rs.getString("nit_editorial")); } catch (Exception e) {}
        try { libro.setIdProveedor(rs.getInt("id_proveedor")); } catch (Exception e) {}
        try { libro.setStockActual(rs.getInt("stock_actual")); } catch (Exception e) {}
        try { libro.setStockMinimo(rs.getInt("stock_minimo")); } catch (Exception e) {}
        try { libro.setActivo(rs.getBoolean("activo")); } catch (Exception e) {}
        try { libro.setNombreAutor(rs.getString("nombre_autor")); } catch (Exception e) {}
        return libro;
    }
}