package org.esperanza.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import org.esperanza.Model.SalidaInventario;
import org.esperanza.util.Conexion;

public class SalidaInventarioDao {
    private final ProveedorConexion conexiones;
    private final StockDao stock;

    public SalidaInventarioDao() {
        this(() -> Conexion.getInstancia().conectar());
    }

    public SalidaInventarioDao(ProveedorConexion conexiones) {
        this.conexiones = Objects.requireNonNull(conexiones);
        this.stock = new StockDao(conexiones);
    }

    /** Stock y movimiento se confirman juntos, usando la misma conexión. */
    public void registrar(SalidaInventario salida) throws SQLException {
        Objects.requireNonNull(salida, "La salida es obligatoria.");
        try (Connection con = conexiones.conectar()) {
            verificarTablasTransaccionales(con);
            con.setAutoCommit(false);
            try {
                stock.validarStock(con, salida.isbn(), salida.cantidad());
                stock.descontarStock(con, salida.isbn(), salida.cantidad());
                String sql = """
                        INSERT INTO movimientos_salida_inventario
                            (isbn, tipo_salida, cantidad, observacion, usuario_id)
                        VALUES (?, ?, ?, ?, ?)
                        """;
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, salida.isbn());
                    ps.setString(2, salida.tipo().name());
                    ps.setInt(3, salida.cantidad());
                    ps.setString(4, salida.observacion());
                    ps.setInt(5, salida.usuarioId());
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("No se pudo registrar el movimiento de inventario.");
                    }
                }
                con.commit();
            } catch (SQLException | RuntimeException ex) {
                try { con.rollback(); } catch (SQLException rollback) { ex.addSuppressed(rollback); }
                throw ex;
            }
        }
    }

    private void verificarTablasTransaccionales(Connection con) throws SQLException {
        String sql = """
                SELECT TABLE_NAME, ENGINE FROM information_schema.TABLES
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME IN ('libros', 'movimientos_salida_inventario')
                """;
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            int tablas = 0;
            while (rs.next()) {
                tablas++;
                if (!"InnoDB".equalsIgnoreCase(rs.getString("ENGINE"))) {
                    throw new SQLException("Las tablas de inventario deben usar InnoDB para garantizar la transacción.");
                }
            }
            if (tablas != 2) {
                throw new SQLException("Falta la tabla de salidas. Ejecuta sql/US-3.2-salida-inventario.sql en tu base de datos.");
            }
        }
    }
}
