package org.esperanza.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import org.esperanza.model.IndicadoresDashboardAdmin;
import org.esperanza.util.Conexion;

/** Lee los tres indicadores del dashboard en una sola consulta. */
public class DashboardIndicadoresDao {

    private static final String SQL_INDICADORES = """
            SELECT
                (SELECT COALESCE(SUM(total), 0)
                   FROM ventas
                  WHERE estado = 'COMPLETADA') AS ventas_totales,
                (SELECT COUNT(*)
                   FROM libros
                  WHERE activo = 1) AS cantidad_libros,
                (SELECT COUNT(*)
                   FROM usuarios
                  WHERE activo = 1) AS usuarios_activos
            """;

    private final ProveedorConexion conexiones;

    public DashboardIndicadoresDao() {
        this(() -> Conexion.getInstancia().conectar());
    }

    public DashboardIndicadoresDao(ProveedorConexion conexiones) {
        this.conexiones = Objects.requireNonNull(conexiones, "conexiones");
    }

    public IndicadoresDashboardAdmin obtenerIndicadores() throws SQLException {
        try (Connection conexion = conexiones.conectar();
             PreparedStatement consulta = conexion.prepareStatement(SQL_INDICADORES);
             ResultSet resultado = consulta.executeQuery()) {
            if (!resultado.next()) {
                throw new SQLException("La consulta de indicadores no devolvió datos");
            }
            BigDecimal ventas = resultado.getBigDecimal("ventas_totales");
            return new IndicadoresDashboardAdmin(
                    ventas == null ? BigDecimal.ZERO : ventas,
                    resultado.getLong("cantidad_libros"),
                    resultado.getLong("usuarios_activos")
            );
        }
    }
}
