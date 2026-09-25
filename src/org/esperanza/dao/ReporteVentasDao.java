package org.esperanza.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.esperanza.Model.FiltroFecha;
import org.esperanza.Model.Venta;
import org.esperanza.util.Conexion;

public class ReporteVentasDao {

    public List<Venta> obtenerVentasPorFiltro(
            FiltroFecha filtro) throws SQLException {

        List<Venta> lista = new ArrayList<>();

        if (filtro == null) {
            throw new IllegalArgumentException(
                    "El filtro de fecha no puede ser nulo."
            );
        }

        String sql;

        switch (filtro) {

            case DIA:
                sql = """
                      SELECT
                          id_venta,
                          fecha_venta,
                          subtotal,
                          descuento,
                          total,
                          cui_cliente,
                          id_usuario
                      FROM ventas
                      WHERE DATE(fecha_venta) = CURDATE()
                      ORDER BY fecha_venta DESC
                      """;
                break;

            case SEMANA:
                sql = """
                      SELECT
                          id_venta,
                          fecha_venta,
                          subtotal,
                          descuento,
                          total,
                          cui_cliente,
                          id_usuario
                      FROM ventas
                      WHERE YEARWEEK(fecha_venta, 1)
                          = YEARWEEK(CURDATE(), 1)
                      ORDER BY fecha_venta DESC
                      """;
                break;

            case MES:
                sql = """
                      SELECT
                          id_venta,
                          fecha_venta,
                          subtotal,
                          descuento,
                          total,
                          cui_cliente,
                          id_usuario
                      FROM ventas
                      WHERE MONTH(fecha_venta) = MONTH(CURDATE())
                        AND YEAR(fecha_venta) = YEAR(CURDATE())
                      ORDER BY fecha_venta DESC
                      """;
                break;

            default:
                throw new IllegalArgumentException(
                        "Filtro de fecha no válido."
                );
        }

        try (
                Connection conexion =
                        Conexion.getInstancia().conectar();

                PreparedStatement ps =
                        conexion.prepareStatement(sql);

                ResultSet rs =
                        ps.executeQuery()
        ) {

            while (rs.next()) {

                Venta venta =
                        mapearVenta(rs);

                lista.add(venta);
            }
        }

        return lista;
    }

    private Venta mapearVenta(
            ResultSet rs) throws SQLException {

        Venta venta = new Venta();

        venta.setIdVenta(
                rs.getInt("id_venta")
        );

        Timestamp fecha =
                rs.getTimestamp("fecha_venta");

        if (fecha != null) {
            venta.setFechaVenta(
                    fecha.toLocalDateTime()
            );
        }

        venta.setSubtotal(
                rs.getBigDecimal("subtotal")
        );

        venta.setDescuento(
                rs.getBigDecimal("descuento")
        );

        venta.setTotal(
                rs.getBigDecimal("total")
        );

        venta.setCuiCliente(
                rs.getLong("cui_cliente")
        );

        venta.setIdUsuario(
                rs.getInt("id_usuario")
        );

        return venta;
    }
}