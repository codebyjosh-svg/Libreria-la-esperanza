package org.esperanza.dao;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.esperanza.model.DetalleVenta;
import org.esperanza.model.Venta;
import org.esperanza.util.Conexion;

public class VentaDao {

    private final ProveedorConexion conexiones;
    private final DetalleVentaDao detalleVentaDao;
    private final StockDao stockDao;

    public VentaDao() {

        this(
                () -> Conexion
                        .getInstancia()
                        .conectar()
        );
    }

    public VentaDao(
            ProveedorConexion conexiones) {

        this.conexiones =
                Objects.requireNonNull(
                        conexiones,
                        "El proveedor de conexion es obligatorio"
                );

        this.detalleVentaDao =
                new DetalleVentaDao(conexiones);

        this.stockDao =
                new StockDao(conexiones);
    }

    public Venta registrar(
            long cuiCliente,
            int idUsuario,
            List<DetalleVenta> detalles)
            throws SQLException {

        if (cuiCliente <= 0) {

            throw new IllegalArgumentException(
                    "El CUI del cliente es obligatorio"
            );
        }

        if (idUsuario <= 0) {

            throw new IllegalArgumentException(
                    "El usuario debe ser valido"
            );
        }

        Objects.requireNonNull(
                detalles,
                "Los detalles son obligatorios"
        );

        if (detalles.isEmpty()) {

            throw new IllegalArgumentException(
                    "La venta debe tener productos"
            );
        }

        List<DetalleVenta> copia =
                new ArrayList<>();

        BigDecimal subtotal =
                BigDecimal.ZERO;

        for (DetalleVenta detalle : detalles) {

            Objects.requireNonNull(
                    detalle,
                    "No se permiten detalles nulos"
            );

            BigDecimal precio =
                    detalle
                            .getPrecioUnitario()
                            .setScale(
                                    2,
                                    RoundingMode.UNNECESSARY
                            );

            DetalleVenta item =
                    new DetalleVenta(
                            0,
                            0,
                            detalle.getIsbn(),
                            detalle.getCantidad(),
                            precio
                    );

            copia.add(item);

            subtotal =
                    subtotal.add(
                            item.getSubtotal()
                    );
        }

        BigDecimal descuento =
                BigDecimal.ZERO;

        BigDecimal total =
                subtotal.subtract(
                        descuento
                );

        Venta venta =
                new Venta(
                        0,
                        LocalDateTime.now()
                                .withNano(0),
                        subtotal,
                        descuento,
                        total,
                        cuiCliente,
                        idUsuario
                );

        try (Connection conexion =
                conexiones.conectar()) {

            conexion.setAutoCommit(false);

            try {

                for (DetalleVenta detalle : copia) {

                    stockDao.validarStock(
                            conexion,
                            detalle.getIsbn(),
                            detalle.getCantidad()
                    );
                }

                String sqlVenta = """
                        INSERT INTO ventas
                        (
                            subtotal,
                            descuento,
                            total,
                            estado,
                            cui_cliente,
                            id_usuario
                        )
                        VALUES (?, ?, ?, 'COMPLETADA', ?, ?)
                        """;

                try (PreparedStatement ps =
                        conexion.prepareStatement(
                                sqlVenta,
                                Statement.RETURN_GENERATED_KEYS
                        )) {

                    ps.setBigDecimal(
                            1,
                            subtotal
                    );

                    ps.setBigDecimal(
                            2,
                            descuento
                    );

                    ps.setBigDecimal(
                            3,
                            total
                    );

                    ps.setLong(
                            4,
                            cuiCliente
                    );

                    ps.setInt(
                            5,
                            idUsuario
                    );

                    int filas =
                            ps.executeUpdate();

                    if (filas != 1) {

                        throw new SQLException(
                                "No se pudo registrar la venta"
                        );
                    }

                    try (ResultSet claves =
                            ps.getGeneratedKeys()) {

                        if (!claves.next()) {

                            throw new SQLException(
                                    "No se obtuvo el ID de la venta"
                            );
                        }

                        venta.setIdVenta(
                                claves.getInt(1)
                        );
                    }
                }

                for (DetalleVenta detalle : copia) {

                    detalleVentaDao.insertar(
                            conexion,
                            venta.getIdVenta(),
                            detalle
                    );

                    stockDao.descontarStock(
                            conexion,
                            detalle.getIsbn(),
                            detalle.getCantidad()
                    );
                }

                conexion.commit();

            } catch (SQLException
                    | RuntimeException e) {

                try {

                    conexion.rollback();

                } catch (SQLException rollbackError) {

                    e.addSuppressed(
                            rollbackError
                    );
                }

                throw e;

            } finally {

                try {

                    conexion.setAutoCommit(true);

                } catch (SQLException ignored) {

                }
            }
        }

        return venta;
    }

    public Optional<Venta> buscarPorId(
            int idVenta)
            throws SQLException {

        String sql = """
                SELECT
                    id_venta,
                    fecha_venta,
                    subtotal,
                    descuento,
                    total,
                    cui_cliente,
                    id_usuario
                FROM ventas
                WHERE id_venta = ?
                """;

        try (
                Connection conexion =
                        conexiones.conectar();

                PreparedStatement ps =
                        conexion.prepareStatement(sql)
        ) {

            ps.setInt(
                    1,
                    idVenta
            );

            try (ResultSet rs =
                    ps.executeQuery()) {

                if (rs.next()) {

                    return Optional.of(
                            leer(rs)
                    );
                }

                return Optional.empty();
            }
        }
    }

    public List<Venta> listar()
            throws SQLException {

        List<Venta> ventas =
                new ArrayList<>();

        String sql = """
                SELECT
                    id_venta,
                    fecha_venta,
                    subtotal,
                    descuento,
                    total,
                    cui_cliente,
                    id_usuario
                FROM ventas
                ORDER BY id_venta DESC
                """;

        try (
                Connection conexion =
                        conexiones.conectar();

                PreparedStatement ps =
                        conexion.prepareStatement(sql);

                ResultSet rs =
                        ps.executeQuery()
        ) {

            while (rs.next()) {

                ventas.add(
                        leer(rs)
                );
            }
        }

        return ventas;
    }

    private Venta leer(
            ResultSet rs)
            throws SQLException {

        return new Venta(
                rs.getInt(
                        "id_venta"
                ),
                rs.getTimestamp(
                        "fecha_venta"
                ).toLocalDateTime(),
                rs.getBigDecimal(
                        "subtotal"
                ),
                rs.getBigDecimal(
                        "descuento"
                ),
                rs.getBigDecimal(
                        "total"
                ),
                rs.getLong(
                        "cui_cliente"
                ),
                rs.getInt(
                        "id_usuario"
                )
        );
    }
}