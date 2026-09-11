package org.esperanza.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.esperanza.model.ResumenVentasDia;
import org.esperanza.model.VentaDia;
import org.esperanza.util.Conexion;

public class VentaDiaDao {

    public List<VentaDia> listarVentasDelDia()
            throws SQLException {

        List<VentaDia> ventas = new ArrayList<>();

        String sql = """
                SELECT
                    v.id_venta,
                    v.fecha_venta,
                    v.cui_cliente,
                    v.id_usuario,
                    v.subtotal,
                    v.descuento,
                    v.total,
                    v.estado,
                    COALESCE(
                        NULLIF(
                            TRIM(
                                CONCAT_WS(
                                    ' ',
                                    c.nombre_cliente,
                                    c.apellido_cliente
                                )
                            ),
                            ''
                        ),
                        'Sin cliente'
                    ) AS cliente,
                    COALESCE(
                        u.username,
                        CONCAT('Usuario #', v.id_usuario)
                    ) AS usuario
                FROM ventas v
                LEFT JOIN clientes c
                    ON c.cui = v.cui_cliente
                LEFT JOIN usuarios u
                    ON u.id = v.id_usuario
                WHERE v.fecha_venta >= CURDATE()
                  AND v.fecha_venta < CURDATE() + INTERVAL 1 DAY
                  AND v.estado = 'COMPLETADA'
                ORDER BY v.fecha_venta DESC
                """;

        try (Connection conexion =
                     Conexion.getInstancia().conectar();

             PreparedStatement ps =
                     conexion.prepareStatement(sql);

             ResultSet rs =
                     ps.executeQuery()) {

            while (rs.next()) {

                ventas.add(
                        new VentaDia(
                                rs.getInt("id_venta"),

                                rs.getTimestamp(
                                        "fecha_venta"
                                ).toLocalDateTime(),

                                rs.getLong(
                                        "cui_cliente"
                                ),

                                rs.getString(
                                        "cliente"
                                ),

                                rs.getInt(
                                        "id_usuario"
                                ),

                                rs.getString(
                                        "usuario"
                                ),

                                rs.getBigDecimal(
                                        "subtotal"
                                ),

                                rs.getBigDecimal(
                                        "descuento"
                                ),

                                rs.getBigDecimal(
                                        "total"
                                ),

                                rs.getString(
                                        "estado"
                                )
                        )
                );
            }
        }

        return ventas;
    }

    public ResumenVentasDia obtenerResumenDelDia()
            throws SQLException {

        String sql = """
                SELECT
                    COUNT(*) AS cantidad_ventas,
                    COALESCE(SUM(subtotal), 0) AS subtotal,
                    COALESCE(SUM(descuento), 0) AS descuentos,
                    COALESCE(SUM(total), 0) AS total
                FROM ventas
                WHERE fecha_venta >= CURDATE()
                  AND fecha_venta < CURDATE() + INTERVAL 1 DAY
                  AND estado = 'COMPLETADA'
                """;

        try (Connection conexion =
                     Conexion.getInstancia().conectar();

             PreparedStatement ps =
                     conexion.prepareStatement(sql);

             ResultSet rs =
                     ps.executeQuery()) {

            if (rs.next()) {

                return new ResumenVentasDia(
                        rs.getInt(
                                "cantidad_ventas"
                        ),

                        valor(
                                rs.getBigDecimal(
                                        "subtotal"
                                )
                        ),

                        valor(
                                rs.getBigDecimal(
                                        "descuentos"
                                )
                        ),

                        valor(
                                rs.getBigDecimal(
                                        "total"
                                )
                        )
                );
            }
        }

        return new ResumenVentasDia(
                0,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
    }

    private BigDecimal valor(BigDecimal valor) {

        return valor == null
                ? BigDecimal.ZERO
                : valor;
    }
}