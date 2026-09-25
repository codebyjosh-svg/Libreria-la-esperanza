package org.esperanza.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.Date;
import java.time.LocalDate;

import org.esperanza.Model.LibroMasVendido;
import org.esperanza.Model.StockValorizado;
import org.esperanza.util.Conexion;

public class ReporteInventarioDao {

    private final ProveedorConexion conexiones;

    public ReporteInventarioDao() {
        this(
                () -> Conexion
                        .getInstancia()
                        .conectar()
        );
    }

    public ReporteInventarioDao(
            ProveedorConexion conexiones) {

        this.conexiones = conexiones;
    }

    public List<LibroMasVendido> listarLibrosMasVendidos()
            throws SQLException {

        List<LibroMasVendido> libros = new ArrayList<>();

        String sql = """
                SELECT
                    l.isbn,
                    l.titulo,
                    SUM(dv.cantidad) AS cantidad_vendida,
                    COALESCE(SUM(dv.subtotal), 0) AS total_vendido
                FROM detalle_venta dv
                INNER JOIN ventas v
                    ON v.id_venta = dv.id_venta
                INNER JOIN libros l
                    ON l.isbn = dv.isbn
                WHERE v.estado = 'COMPLETADA'
                GROUP BY
                    l.isbn,
                    l.titulo
                ORDER BY
                    cantidad_vendida DESC,
                    total_vendido DESC,
                    l.titulo ASC
                """;

        try (
                Connection conexion
                = conexiones.conectar(); PreparedStatement ps
                = conexion.prepareStatement(sql); ResultSet rs
                = ps.executeQuery()) {

            while (rs.next()) {

                LibroMasVendido libro
                        = new LibroMasVendido(
                                rs.getString("isbn"),
                                rs.getString("titulo"),
                                rs.getInt("cantidad_vendida"),
                                rs.getBigDecimal("total_vendido")
                        );

                libros.add(libro);
            }
        }

        return libros;
    }

    public List<LibroMasVendido> listarLibrosMasVendidos(
            LocalDate fechaInicio,
            LocalDate fechaFin) throws SQLException {

        List<LibroMasVendido> libros = new ArrayList<>();

        String sql = """
            SELECT
                l.isbn,
                l.titulo,
                SUM(dv.cantidad) AS cantidad_vendida,
                COALESCE(SUM(dv.subtotal), 0) AS total_vendido
            FROM detalle_venta dv
            INNER JOIN ventas v
                ON v.id_venta = dv.id_venta
            INNER JOIN libros l
                ON l.isbn = dv.isbn
            WHERE v.estado = 'COMPLETADA'
              AND DATE(v.fecha_venta) BETWEEN ? AND ?
            GROUP BY
                l.isbn,
                l.titulo
            ORDER BY
                cantidad_vendida DESC,
                total_vendido DESC,
                l.titulo ASC
            """;

        try (
                Connection conexion = conexiones.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(fechaInicio));
            ps.setDate(2, Date.valueOf(fechaFin));

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    LibroMasVendido libro
                            = new LibroMasVendido(
                                    rs.getString("isbn"),
                                    rs.getString("titulo"),
                                    rs.getInt("cantidad_vendida"),
                                    rs.getBigDecimal("total_vendido")
                            );

                    libros.add(libro);
                }
            }
        }

        return libros;
    }

    public List<StockValorizado> listarStockValorizado()
            throws SQLException {

        List<StockValorizado> libros = new ArrayList<>();

        String sql = """
                SELECT
                    isbn,
                    titulo,
                    stock_actual,
                    precio,
                    CAST(
                        stock_actual * precio
                        AS DECIMAL(14,2)
                    ) AS valor_inventario
                FROM libros
                WHERE activo = TRUE
                ORDER BY
                    valor_inventario DESC,
                    titulo ASC
                """;

        try (
                Connection conexion
                = conexiones.conectar(); PreparedStatement ps
                = conexion.prepareStatement(sql); ResultSet rs
                = ps.executeQuery()) {

            while (rs.next()) {

                StockValorizado libro
                        = new StockValorizado(
                                rs.getString("isbn"),
                                rs.getString("titulo"),
                                rs.getInt("stock_actual"),
                                rs.getBigDecimal("precio"),
                                rs.getBigDecimal("valor_inventario")
                        );

                libros.add(libro);
            }
        }

        return libros;
    }
}
