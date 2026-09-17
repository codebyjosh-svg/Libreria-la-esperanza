package org.esperanza.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.esperanza.model.DetalleVenta;
import org.esperanza.util.Conexion;

public class DetalleVentaDao {

    private final ProveedorConexion conexiones;

    public DetalleVentaDao() {

        this(
                () -> Conexion
                        .getInstancia()
                        .conectar()
        );
    }

    public DetalleVentaDao(
            ProveedorConexion conexiones) {

        this.conexiones =
                Objects.requireNonNull(conexiones);
    }

    /**
     * Utiliza la conexion que recibe VentaDao.
     * NO hace commit y NO cierra la conexion.
     */
    public int insertar(
            Connection conexion,
            int idVenta,
            DetalleVenta detalle)
            throws SQLException {

        Objects.requireNonNull(
                conexion,
                "La conexion es obligatoria"
        );

        Objects.requireNonNull(
                detalle,
                "El detalle es obligatorio"
        );

        if (idVenta <= 0) {
            throw new IllegalArgumentException(
                    "La venta debe tener un ID valido"
            );
        }

        String sql = """
                INSERT INTO detalle_venta
                (
                    id_venta,
                    isbn,
                    cantidad,
                    precio_unitario,
                    subtotal
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps =
                conexion.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS
                )) {

            ps.setInt(
                    1,
                    idVenta
            );

            ps.setString(
                    2,
                    detalle.getIsbn()
            );

            ps.setInt(
                    3,
                    detalle.getCantidad()
            );

            ps.setBigDecimal(
                    4,
                    detalle.getPrecioUnitario()
            );

            ps.setBigDecimal(
                    5,
                    detalle.getSubtotal()
            );

            int filas =
                    ps.executeUpdate();

            if (filas != 1) {
                throw new SQLException(
                        "No se pudo insertar el detalle de venta"
                );
            }

            try (ResultSet claves =
                    ps.getGeneratedKeys()) {

                if (!claves.next()) {
                    throw new SQLException(
                            "No se obtuvo el ID del detalle"
                    );
                }

                return claves.getInt(1);
            }
        }
    }

    public List<DetalleVenta> listarPorVenta(
            int idVenta) throws SQLException {

        List<DetalleVenta> detalles =
                new ArrayList<>();

        String sql = """
                SELECT
                    id_detalle,
                    id_venta,
                    isbn,
                    cantidad,
                    precio_unitario
                FROM detalle_venta
                WHERE id_venta = ?
                ORDER BY id_detalle
                """;

        try (Connection conexion =
                    conexiones.conectar();

             PreparedStatement ps =
                    conexion.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    idVenta
            );

            try (ResultSet rs =
                    ps.executeQuery()) {

                while (rs.next()) {

                    DetalleVenta detalle =
                            new DetalleVenta(
                                    rs.getInt(
                                            "id_detalle"
                                    ),
                                    rs.getInt(
                                            "id_venta"
                                    ),
                                    rs.getString(
                                            "isbn"
                                    ),
                                    rs.getInt(
                                            "cantidad"
                                    ),
                                    rs.getBigDecimal(
                                            "precio_unitario"
                                    )
                            );

                    detalles.add(detalle);
                }
            }
        }

        return detalles;
    }
}