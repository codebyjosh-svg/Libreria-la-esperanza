package org.esperanza.Dao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import org.esperanza.model.*;
import org.esperanza.util.Conexion;

public class VentaDao {
    private final ProveedorConexion conexiones;
    private final DetalleVentaDao detalleDao;

    public VentaDao() {
        this(() -> Conexion.getInstancia().conectar());
    }

    public VentaDao(ProveedorConexion conexiones) {
        this.conexiones = Objects.requireNonNull(conexiones);
        this.detalleDao = new DetalleVentaDao(conexiones);
    }

    /** Registra cabecera y detalles de forma atomica. No modifica los objetos recibidos. */
    public Venta registrar(int idCliente, int idEmpleado, List<DetalleVenta> detalles) throws SQLException {
        if (idCliente <= 0 || idEmpleado <= 0) {
            throw new IllegalArgumentException("Cliente y empleado deben tener identificadores positivos");
        }
        Objects.requireNonNull(detalles, "Los detalles son obligatorios");
        if (detalles.isEmpty()) throw new IllegalArgumentException("La venta debe tener productos");
        List<DetalleVenta> copia = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (DetalleVenta d : detalles) {
            Objects.requireNonNull(d, "No se permiten detalles nulos");
            if (d.getIdProducto() <= 0) throw new IllegalArgumentException("Producto invalido");
            BigDecimal precio = d.getPrecioUnitario().setScale(2, RoundingMode.UNNECESSARY);
            if (precio.precision() > 19) throw new IllegalArgumentException("Precio fuera de rango");
            DetalleVenta item = new DetalleVenta(0, 0, d.getIdProducto(), d.getCantidad(), precio);
            copia.add(item);
            total = total.add(item.getSubtotal());
        }
        if (total.precision() > 19) throw new IllegalArgumentException("Total fuera de rango");
        Venta venta = new Venta(0, LocalDateTime.now().withNano(0), idCliente, idEmpleado, total);
        try (Connection c = conexiones.conectar()) {
            c.setAutoCommit(false);
            try {
                String sql = "INSERT INTO venta (fecha, id_cliente, id_empleado, total) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setTimestamp(1, Timestamp.valueOf(venta.getFecha()));
                    ps.setInt(2, idCliente);
                    ps.setInt(3, idEmpleado);
                    ps.setBigDecimal(4, total);
                    if (ps.executeUpdate() != 1) throw new SQLException("No se inserto la venta");
                    try (ResultSet claves = ps.getGeneratedKeys()) {
                        if (!claves.next()) throw new SQLException("No se obtuvo el ID de la venta");
                        venta.setIdVenta(claves.getInt(1));
                    }
                }
                for (DetalleVenta d : copia) detalleDao.insertar(c, venta.getIdVenta(), d);
                c.commit();
            } catch (SQLException | RuntimeException e) {
                try { c.rollback(); } catch (SQLException rollback) { e.addSuppressed(rollback); }
                throw e;
            }
        }
        return venta;
    }

    public Optional<Venta> buscarPorId(int idVenta) throws SQLException {
        try (Connection c = conexiones.conectar();
                PreparedStatement ps = c.prepareStatement("SELECT * FROM venta WHERE id_venta = ?")) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(leer(rs)) : Optional.empty();
            }
        }
    }

    public List<Venta> listar() throws SQLException {
        List<Venta> ventas = new ArrayList<>();
        try (Connection c = conexiones.conectar();
                PreparedStatement ps = c.prepareStatement("SELECT * FROM venta ORDER BY id_venta DESC");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) ventas.add(leer(rs));
        }
        return ventas;
    }

    /** El esquema elimina los detalles asociados mediante ON DELETE CASCADE. */
    public boolean eliminar(int idVenta) throws SQLException {
        try (Connection c = conexiones.conectar();
                PreparedStatement ps = c.prepareStatement("DELETE FROM venta WHERE id_venta = ?")) {
            ps.setInt(1, idVenta);
            return ps.executeUpdate() == 1;
        }
    }

    private Venta leer(ResultSet rs) throws SQLException {
        return new Venta(rs.getInt("id_venta"), rs.getTimestamp("fecha").toLocalDateTime(),
                rs.getInt("id_cliente"), rs.getInt("id_empleado"), rs.getBigDecimal("total"));
    }
}

