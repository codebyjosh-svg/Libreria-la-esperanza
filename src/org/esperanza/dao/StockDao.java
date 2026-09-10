package org.esperanza.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import org.esperanza.util.Conexion;

/**
 * DAO encargado de consultar y validar el stock de los libros.
 *
 * T2.17 - Validar stock.
 */
public class StockDao {

    private final ProveedorConexion conexiones;

    public StockDao() {
        this(() -> Conexion.getInstancia().conectar());
    }

    public StockDao(ProveedorConexion conexiones) {
        this.conexiones = Objects.requireNonNull(
                conexiones,
                "El proveedor de conexion es obligatorio"
        );
    }

    /**
     * Obtiene el stock actual de un libro activo.
     *
     * @param isbn ISBN del libro.
     * @return stock disponible, o -1 si no existe o esta inactivo.
     */
    public int obtenerStockActual(String isbn) throws SQLException {

        validarIsbn(isbn);

        String sql = """
                SELECT stock_actual
                FROM libros
                WHERE isbn = ?
                  AND activo = 1
                """;

        try (Connection conexion = conexiones.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, isbn.trim());

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt("stock_actual");
                }

                return -1;
            }
        }
    }

    /**
     * Comprueba si existe suficiente stock.
     */
    public boolean hayStockSuficiente(
            String isbn,
            int cantidad) throws SQLException {

        validarCantidad(cantidad);

        int stockActual = obtenerStockActual(isbn);

        return stockActual >= cantidad;
    }

    /**
     * Version que utiliza una conexion existente.
     *
     * Se usara en T2.18 para que la validacion
     * forme parte de la misma transaccion JDBC.
     */
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

            ps.setString(1, isbn.trim());

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return false;
                }

                int stockActual =
                        rs.getInt("stock_actual");

                return stockActual >= cantidad;
            }
        }
    }

    /**
     * Valida stock y genera mensajes entendibles
     * para la interfaz.
     */
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

            ps.setString(1, isbn.trim());

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {

                    throw new IllegalArgumentException(
                            "El libro no existe o esta inactivo."
                    );
                }

                String titulo =
                        rs.getString("titulo");

                int stockActual =
                        rs.getInt("stock_actual");

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

    private void validarIsbn(String isbn) {

        if (isbn == null || isbn.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El ISBN es obligatorio."
            );
        }
    }

    private void validarCantidad(int cantidad) {

        if (cantidad <= 0) {

            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que cero."
            );
        }
    }
}