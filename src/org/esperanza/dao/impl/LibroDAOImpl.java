package org.esperanza.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.esperanza.dao.LibroDAO;
import org.esperanza.model.Libro;
import org.esperanza.util.Conexion;

public class LibroDAOImpl implements LibroDAO {

    @Override
    public List<Libro> listarTodos() {
        List<Libro> lista = new ArrayList<>();
        String sql = "{call sp_listarlibros()}";
        
        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {
            
            while (rs.next()) {
                Libro l = new Libro();
                l.setIsbn(rs.getString("isbn"));
                l.setTitulo(rs.getString("titulo"));
                l.setFechaPublicacion(rs.getDate("fecha_publicacion"));
                l.setPrecio(rs.getDouble("precio"));
                l.setIdCategoria(rs.getInt("id_categoria"));
                l.setNitEditorial(rs.getString("nit_editorial"));
                l.setIdProveedor(rs.getInt("id_proveedor"));
                l.setStockActual(rs.getInt("stock_actual"));
                l.setStockMinimo(rs.getInt("stock_minimo"));
                l.setActivo(rs.getBoolean("activo"));
                lista.add(l);
            }
        } catch (SQLException e) {
            System.err.println("Error listar libros: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public Libro buscarLibro(String isbn) {
        String sql = "{call sp_buscarlibro(?)}";
        Libro l = null;
        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {
             
            cs.setString(1, isbn);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    l = new Libro(
                        rs.getString("isbn"), rs.getString("titulo"), rs.getDate("fecha_publicacion"),
                        rs.getDouble("precio"), rs.getInt("id_categoria"), rs.getString("nit_editorial"),
                        rs.getInt("id_proveedor"), rs.getInt("stock_actual"), rs.getInt("stock_minimo"),
                        rs.getBoolean("activo")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error buscar libro: " + e.getMessage());
        }
        return l;
    }

    // --- AQUÍ ESTÁ LA CORRECCIÓN PRINCIPAL ---
    @Override
    public Libro buscarPorIsbn(String isbn) {
        return buscarLibro(isbn); // Ahora devuelve un Libro, no un List<Libro>
    }
    // -----------------------------------------

    @Override
    public List<Libro> buscarPorTitulo(String titulo) {
        List<Libro> resultado = new ArrayList<>();
        for (Libro l : listarTodos()) {
            if (l.getTitulo() != null && l.getTitulo().toLowerCase().contains(titulo.toLowerCase())) {
                resultado.add(l);
            }
        }
        return resultado;
    }

    @Override
    public List<Libro> buscarPorAutor(String autor) {
        List<Libro> resultado = new ArrayList<>();
        for (Libro l : listarTodos()) {
            if (l.getNombreAutor() != null && l.getNombreAutor().toLowerCase().contains(autor.toLowerCase())) {
                resultado.add(l);
            }
        }
        return resultado;
    }

    @Override
    public boolean insertar(Libro libro) {
        String sql = "{call sp_insertarlibro(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {
             
            cs.setString(1, libro.getIsbn());
            cs.setString(2, libro.getTitulo());
            cs.setDate(3, libro.getFechaPublicacion());
            cs.setDouble(4, libro.getPrecio());
            cs.setInt(5, libro.getIdCategoria());
            cs.setString(6, libro.getNitEditorial());
            cs.setInt(7, libro.getIdProveedor());
            cs.setInt(8, libro.getStockActual());
            cs.setInt(9, libro.getStockMinimo());
            
            return cs.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error insertar libro: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean actualizar(Libro libro) {
        String sql = "{call sp_actualizarlibro(?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {
             
            cs.setString(1, libro.getIsbn());
            cs.setString(2, libro.getTitulo());
            cs.setDate(3, libro.getFechaPublicacion());
            cs.setDouble(4, libro.getPrecio());
            cs.setInt(5, libro.getIdCategoria());
            cs.setString(6, libro.getNitEditorial());
            cs.setInt(7, libro.getIdProveedor());
            cs.setInt(8, libro.getStockMinimo());
            
            return cs.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error actualizar libro: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean eliminar(String isbn) {
        String sql = "{call sp_eliminarlibro(?)}";
        try (Connection con = Conexion.getInstancia().conectar();
             CallableStatement cs = con.prepareCall(sql)) {
            cs.setString(1, isbn);
            return cs.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error desactivar libro: " + e.getMessage());
            return false;
        }
    }
}