package org.esperanza.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

import org.esperanza.util.Conexion;

/**
 * DAO encargado de consultar, validar
 * y actualizar el stock de los libros.
 *
 * T2.17 - Validar stock.
 * T2.19 - Actualizar stock.
 */
public class StockDao {

    private final ProveedorConexion conexiones;

    public StockDao() {

        this(
                () -> Conexion
                        .getInstancia()
                        .conectar()
        );
    }

    public StockDao(
            ProveedorConexion conexiones) {

        this.conexiones =
                Objects.requireNonNull(
                        conexiones,
                        "El proveedor de conexion es obligatorio"
                );
    }

    // =====================================================
    // T2.17 - OBTENER STOCK ACTUAL
    // =====================================================

    public int obtenerStockActual(
            String isbn) throws SQLException {

        validarIsbn(isbn);

        String sql = """
                SELECT stock_actual
                FROM libros
                WHERE isbn = ?
                  AND activo = 1
                """;

        try (
                Connection conexion =
                        conexiones.conectar();

                PreparedStatement ps =
                        conexion.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    isbn.trim()
            );

            try (ResultSet rs =
                    ps.executeQuery()) {

                if (rs.next()) {

                    return rs.getInt(
                            "stock_actual"
                    );
                }

                return -1;
            }
        }
    }

    // =====================================================
    // T2.17 - COMPROBAR STOCK
    // =====================================================

    public boolean hayStockSuficiente(
            String isbn,
            int cantidad) throws SQLException {

        validarIsbn(isbn);
        validarCantidad(cantidad);

        int stockActual =
                obtenerStockActual(isbn);

        return stockActual >= cantidad;
    }

    // =====================================================
    // T2.17 - COMPROBAR STOCK EN TRANSACCION
    // =====================================================

    public boolean hayStockSuficiente(
            Connection conexion,
            String isbn,
            int cantidad) throws SQLException {

        Objects.requireNonNull(
                conexion,
                "La conexion es obligatoria"
        );

        validarIsbn(isbn);
        validarCantidad(cantidad);

        String sql = """
                SELECT stock_actual
                FROM libros
                WHERE isbn = ?
                  AND activo = 1
                FOR UPDATE
                """;

        try (PreparedStatement ps =
                conexion.prepareStatement(sql)) {

            ps.setString(
                    1,
                    isbn.trim()
            );

            try (ResultSet rs =
                    ps.executeQuery()) {

                if (!rs.next()) {

                    return false;
                }

                int stockActual =
                        rs.getInt(
                                "stock_actual"
                        );

                return stockActual >= cantidad;
            }
        }
    }

    // =====================================================
    // T2.17 - VALIDAR STOCK
    // =====================================================

    public void validarStock(
            Connection conexion,
            String isbn,
            int cantidad) throws SQLException {

        Objects.requireNonNull(
                conexion,
                "La conexion es obligatoria"
        );

        validarIsbn(isbn);
        validarCantidad(cantidad);

        String sql = """
                SELECT titulo, stock_actual
                FROM libros
                WHERE isbn = ?
                  AND activo = 1
                FOR UPDATE
                """;

        try (PreparedStatement ps =
                conexion.prepareStatement(sql)) {

            ps.setString(
                    1,
                    isbn.trim()
            );

            try (ResultSet rs =
                    ps.executeQuery()) {

                if (!rs.next()) {

                    throw new IllegalArgumentException(
                            "El libro no existe o esta inactivo."
                    );
                }

                String titulo =
                        rs.getString(
                                "titulo"
                        );

                int stockActual =
                        rs.getInt(
                                "stock_actual"
                        );

                if (stockActual < cantidad) {

                    throw new IllegalArgumentException(
                            "Stock insuficiente para "
                            + titulo
                            + ". Disponible: "
                            + stockActual
                            + ", solicitado: "
                            + cantidad
                    );
                }
            }
        }
    }

    // =====================================================
    // T2.19 - DESCONTAR STOCK
    // =====================================================

    public void descontarStock(
            Connection conexion,
            String isbn,
            int cantidad) throws SQLException {

        Objects.requireNonNull(
                conexion,
                "La conexion es obligatoria"
        );

        validarIsbn(isbn);
        validarCantidad(cantidad);

        String sql = """
                UPDATE libros
                SET stock_actual = stock_actual - ?
                WHERE isbn = ?
                  AND activo = 1
                  AND stock_actual >= ?
                """;

        try (PreparedStatement ps =
                conexion.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    cantidad
            );

            ps.setString(
                    2,
                    isbn.trim()
            );

            ps.setInt(
                    3,
                    cantidad
            );

            int filasAfectadas =
                    ps.executeUpdate();

            if (filasAfectadas != 1) {

                throw new SQLException(
                        "No se pudo descontar el stock "
                        + "del libro con ISBN: "
                        + isbn
                );
            }
        }
    }

    // =====================================================
    // VALIDACIONES
    // =====================================================

    private void validarIsbn(
            String isbn) {

        if (isbn == null
                || isbn.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El ISBN es obligatorio."
            );
        }
    }

    private void validarCantidad(
            int cantidad) {

        if (cantidad <= 0) {

            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que cero."
            );
        }
    }
}