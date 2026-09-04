package org.esperanza.Dao;

import java.sql.*;
import java.util.*;
import org.esperanza.model.DetalleVenta;
import org.esperanza.util.Conexion;

public class DetalleVentaDao {
    private final ProveedorConexion conexiones;

    public DetalleVentaDao() {
        this(() -> Conexion.getInstancia().conectar());
    }

    public DetalleVentaDao(ProveedorConexion conexiones) {
        this.conexiones = Objects.requireNonNull(conexiones);
    }

    /** Usa la transaccion del llamador; no confirma ni cierra su conexion.
     * Invocar desde VentaDao para mantener el total consistente. */
    public int insertar(Connection conexion, int idVenta, DetalleVenta detalle) throws SQLException {
        if (idVenta <= 0 || detalle.getIdProducto() <= 0) {
            throw new IllegalArgumentException("Venta y producto deben tener identificadores positivos");
        }
        String sql = "INSERT INTO detalle_venta (id_venta, id_producto, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idVenta);
            ps.setInt(2, detalle.getIdProducto());
            ps.setInt(3, detalle.getCantidad());
            ps.setBigDecimal(4, detalle.getPrecioUnitario());
            if (ps.executeUpdate() != 1) throw new SQLException("No se inserto el detalle");
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (!claves.next()) throw new SQLException("No se obtuvo el ID del detalle");
                return claves.getInt(1);
            }
        }
    }

    public List<DetalleVenta> listarPorVenta(int idVenta) throws SQLException {
        List<DetalleVenta> detalles = new ArrayList<>();
        try (Connection c = conexiones.conectar();
                PreparedStatement ps = c.prepareStatement(
                        "SELECT id_detalle_venta, id_venta, id_producto, cantidad, precio_unitario FROM detalle_venta WHERE id_venta = ? ORDER BY id_detalle_venta")) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    detalles.add(new DetalleVenta(rs.getInt("id_detalle_venta"), rs.getInt("id_venta"),
                            rs.getInt("id_producto"), rs.getInt("cantidad"), rs.getBigDecimal("precio_unitario")));
                }
            }
        }
        return detalles;
    }
}

