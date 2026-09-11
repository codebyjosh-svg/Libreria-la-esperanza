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

    public boolean actualizarStock(
            String isbn,
            int cantidad) throws SQLException {

        try (Connection conexion = obtenerConexion()) {
            return actualizarStock(
                    conexion,
                    isbn,
                    cantidad
            );
        }
    }

    private boolean actualizarStock(
            Connection conexion,
            String isbn,
            int cantidad) throws SQLException {

        String sql = """
                UPDATE libros
                SET stock_actual = stock_actual + ?
                WHERE isbn = ?
                  AND activo = 1
                """;

        try (PreparedStatement ps =
                     conexion.prepareStatement(sql)) {

            ps.setInt(1, cantidad);
            ps.setString(2, isbn);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean registrarMovimiento(
            String isbn,
            int idUsuario,
            int cantidad,
            String observacion) throws SQLException {

        try (Connection conexion = obtenerConexion()) {
            return registrarMovimiento(
                    conexion,
                    isbn,
                    idUsuario,
                    cantidad,
                    observacion
            );
        }
    }

    private boolean registrarMovimiento(
            Connection conexion,
            String isbn,
            int idUsuario,
            int cantidad,
            String observacion) throws SQLException {

        String sql = """
                INSERT INTO movimientos_inventario
                (
                    isbn,
                    tipo_movimiento,
                    cantidad,
                    id_usuario,
                    observacion
                )
                VALUES (?, 'INGRESO', ?, ?, ?)
                """;

        try (PreparedStatement ps =
                     conexion.prepareStatement(sql)) {

            ps.setString(1, isbn);
            ps.setInt(2, cantidad);
            ps.setInt(3, idUsuario);
            ps.setString(4, observacion);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean registrarIngresoInventario(
            String isbn,
            int idUsuario,
            int cantidad,
            String observacion) throws SQLException {

        try (Connection conexion = obtenerConexion()) {

            boolean autoCommitOriginal =
                    conexion.getAutoCommit();

            conexion.setAutoCommit(false);

            try {

                boolean stockActualizado =
                        actualizarStock(
                                conexion,
                                isbn,
                                cantidad
                        );

                if (!stockActualizado) {
                    throw new SQLException(
                            "No se pudo actualizar el stock del libro."
                    );
                }

                boolean movimientoRegistrado =
                        registrarMovimiento(
                                conexion,
                                isbn,
                                idUsuario,
                                cantidad,
                                observacion
                        );

                if (!movimientoRegistrado) {
                    throw new SQLException(
                            "No se pudo registrar el movimiento."
                    );
                }

                conexion.commit();

                return true;

            } catch (SQLException | RuntimeException e) {

                conexion.rollback();
                throw e;

            } finally {

                conexion.setAutoCommit(
                        autoCommitOriginal
                );
            }
        }
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