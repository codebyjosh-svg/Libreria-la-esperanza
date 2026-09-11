package org.esperanza.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.esperanza.util.Conexion;

public class MovimientoInventarioDAO {

    public MovimientoInventarioDAO() {
    }

    public Connection obtenerConexion() throws SQLException {
        return Conexion.getInstancia().conectar();
    }
    
    
    public List<LibroDisponible> listarLibrosDisponibles()
            throws SQLException {

        List<LibroDisponible> libros = new ArrayList<>();

        String sql = """
                SELECT isbn, titulo, stock_actual
                FROM libros
                WHERE activo = 1
                ORDER BY titulo
                """;

        try (Connection conexion = obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                libros.add(
                        new LibroDisponible(
                                rs.getString("isbn"),
                                rs.getString("titulo"),
                                rs.getInt("stock_actual")
                        )
                );
            }
        }

        return libros;
    }

    public static class LibroDisponible {

        private final String isbn;
        private final String titulo;
        private final int stockActual;

        public LibroDisponible(
                String isbn,
                String titulo,
                int stockActual) {

            this.isbn = isbn;
            this.titulo = titulo;
            this.stockActual = stockActual;
        }

        public String getIsbn() {
            return isbn;
        }

        public String getTitulo() {
            return titulo;
        }

        public int getStockActual() {
            return stockActual;
        }

        @Override
        public String toString() {
            return titulo + " - " + isbn
                    + " (Stock: " + stockActual + ")";
        }
    }
}
