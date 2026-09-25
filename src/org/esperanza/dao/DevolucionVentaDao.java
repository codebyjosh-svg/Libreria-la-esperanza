package org.esperanza.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import org.esperanza.Model.DetalleVenta;
import org.esperanza.Model.EstadoVenta;
import org.esperanza.Model.Usuario;
import org.esperanza.Model.Venta;
import org.esperanza.Service.SesionUsuario;
import org.esperanza.util.Conexion;

/** Devoluciones totales: estado, existencias y auditoría se confirman juntos. */
public class DevolucionVentaDao {
    private final ProveedorConexion conexiones;

    public DevolucionVentaDao() {
        this(() -> Conexion.getInstancia().conectar());
    }

    public DevolucionVentaDao(ProveedorConexion conexiones) {
        this.conexiones = Objects.requireNonNull(conexiones);
    }

    private int usuarioAutorizado() {
        SesionUsuario sesion = SesionUsuario.getInstancia();
        Usuario usuario = sesion.getUsuarioActual();
        if (!sesion.haySesionActiva() || !sesion.tienePermiso("DEVOLUCIONES")
                || usuario == null || !usuario.isActivo() || usuario.getId() <= 0) {
            throw new SecurityException("Tu sesión no tiene permiso para gestionar devoluciones.");
        }
        return usuario.getId();
    }

    public static String validarMotivo(String motivo) {
        String limpio = motivo == null ? "" : motivo.strip();
        if (limpio.length() < 5 || limpio.length() > 500) {
            throw new IllegalArgumentException("El motivo debe tener entre 5 y 500 caracteres.");
        }
        return limpio;
    }

    private static void validarId(int idVenta) {
        if (idVenta <= 0) throw new IllegalArgumentException("Selecciona una venta válida.");
    }

    /** El filtro por ID se aplica dentro de los últimos 30 días. Sin límite silencioso. */
    public List<Venta> listarRecientes(Integer idVenta) throws SQLException {
        usuarioAutorizado();
        if (idVenta != null) validarId(idVenta);
        String sql = """
                SELECT id_venta, fecha_venta, subtotal, descuento, total,
                       cui_cliente, id_usuario, estado
                FROM ventas
                WHERE fecha_venta >= DATE_SUB(NOW(), INTERVAL 30 DAY)
                  AND fecha_venta <= NOW()
                """ + (idVenta == null ? "" : " AND id_venta = ?")
                + " ORDER BY fecha_venta DESC, id_venta DESC";
        List<Venta> ventas = new ArrayList<>();
        try (Connection con = conexiones.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (idVenta != null) ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Venta venta = new Venta(rs.getInt("id_venta"),
                            rs.getTimestamp("fecha_venta").toLocalDateTime(),
                            rs.getBigDecimal("subtotal"), rs.getBigDecimal("descuento"),
                            rs.getBigDecimal("total"), rs.getLong("cui_cliente"), rs.getInt("id_usuario"));
                    venta.setEstado(EstadoVenta.desdeBaseDatos(rs.getString("estado")));
                    ventas.add(venta);
                }
            }
        }
        return ventas;
    }

    public List<DetalleVenta> listarDetalles(int idVenta) throws SQLException {
        usuarioAutorizado();
        validarId(idVenta);
        return new DetalleVentaDao(conexiones).listarPorVenta(idVenta);
    }

    /** No acepta un usuario del formulario: la identidad de auditoría procede de la sesión. */
    public void devolver(int idVenta, String motivo) throws SQLException {
        int usuarioId = usuarioAutorizado();
        validarId(idVenta);
        String motivoLimpio = validarMotivo(motivo);
        try (Connection con = conexiones.conectar()) {
            verificarTablasTransaccionales(con);
            con.setAutoCommit(false);
            try {
                bloquearVentaCompletada(con, idVenta);
                Map<String, Integer> cantidades = leerCantidades(con, idVenta);
                // PK(id_venta) protege también frente a un estado modificado fuera de este DAO.
                try (PreparedStatement ps = con.prepareStatement("""
                        INSERT INTO devoluciones_venta (id_venta, motivo, id_usuario)
                        VALUES (?, ?, ?)
                        """)) {
                    ps.setInt(1, idVenta);
                    ps.setString(2, motivoLimpio);
                    ps.setInt(3, usuarioId);
                    exigirUnaFila(ps.executeUpdate(), "No se pudo registrar la auditoría de devolución.");
                }
                // El orden estable reduce bloqueos cruzados cuando dos ventas comparten libros.
                for (Map.Entry<String, Integer> item : cantidades.entrySet()) {
                    restaurarStock(con, item.getKey(), item.getValue());
                    try (PreparedStatement ps = con.prepareStatement("""
                            INSERT INTO movimientos_devolucion_venta (id_venta, isbn, cantidad)
                            VALUES (?, ?, ?)
                            """)) {
                        ps.setInt(1, idVenta);
                        ps.setString(2, item.getKey());
                        ps.setInt(3, item.getValue());
                        exigirUnaFila(ps.executeUpdate(), "No se pudo registrar el movimiento de devolución.");
                    }
                }
                try (PreparedStatement ps = con.prepareStatement("""
                        UPDATE ventas SET estado = 'DEVUELTA'
                        WHERE id_venta = ? AND estado = 'COMPLETADA'
                        """)) {
                    ps.setInt(1, idVenta);
                    exigirUnaFila(ps.executeUpdate(), "La venta cambió de estado. Actualiza la consulta.");
                }
                con.commit();
            } catch (SQLException | RuntimeException ex) {
                try { con.rollback(); } catch (SQLException rollback) { ex.addSuppressed(rollback); }
                throw ex;
            }
        }
    }

    private void bloquearVentaCompletada(Connection con, int idVenta) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT estado FROM ventas WHERE id_venta = ? FOR UPDATE")) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("La venta no existe.");
                EstadoVenta estado = EstadoVenta.desdeBaseDatos(rs.getString("estado"));
                if (!estado.permiteDevolucion()) {
                    throw new IllegalStateException(estado == EstadoVenta.DEVUELTA
                            ? "Esta venta ya fue devuelta. No se puede devolver dos veces."
                            : "Solo se pueden devolver ventas en estado COMPLETADA.");
                }
            }
        }
    }

    private Map<String, Integer> leerCantidades(Connection con, int idVenta) throws SQLException {
        Map<String, Integer> cantidades = new TreeMap<>();
        try (PreparedStatement ps = con.prepareStatement("""
                SELECT isbn, cantidad FROM detalle_venta
                WHERE id_venta = ? ORDER BY isbn, id_detalle FOR UPDATE
                """)) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String isbn = rs.getString("isbn");
                    int cantidad = rs.getInt("cantidad");
                    if (isbn == null || isbn.isBlank() || cantidad <= 0) {
                        throw new SQLException("La venta contiene un detalle inválido; revisa sus datos.");
                    }
                    try {
                        cantidades.merge(isbn, cantidad, Math::addExact);
                    } catch (ArithmeticException ex) {
                        throw new SQLException("La cantidad a devolver supera el límite permitido.", ex);
                    }
                }
            }
        }
        if (cantidades.isEmpty()) throw new IllegalStateException("La venta no tiene productos para devolver.");
        return cantidades;
    }

    private void restaurarStock(Connection con, String isbn, int cantidad) throws SQLException {
        // No se filtra activo: una devolución también repone libros desactivados después de la venta.
        try (PreparedStatement ps = con.prepareStatement("""
                UPDATE libros SET stock_actual = stock_actual + ?
                WHERE isbn = ? AND stock_actual >= 0 AND stock_actual <= ?
                """)) {
            ps.setInt(1, cantidad);
            ps.setString(2, isbn);
            ps.setInt(3, Integer.MAX_VALUE - cantidad);
            exigirUnaFila(ps.executeUpdate(),
                    "No se pudo restaurar el stock del ISBN " + isbn + ". Revisa existencia y límite de stock.");
        }
    }

    private static void exigirUnaFila(int filas, String mensaje) throws SQLException {
        if (filas != 1) throw new SQLException(mensaje);
    }

    private void verificarTablasTransaccionales(Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("""
                SELECT TABLE_NAME, ENGINE FROM information_schema.TABLES
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME IN ('ventas', 'detalle_venta', 'libros',
                                     'devoluciones_venta', 'movimientos_devolucion_venta')
                """); ResultSet rs = ps.executeQuery()) {
            int tablas = 0;
            while (rs.next()) {
                tablas++;
                if (!"InnoDB".equalsIgnoreCase(rs.getString("ENGINE"))) {
                    throw new SQLException("Las cinco tablas de devoluciones deben usar InnoDB.");
                }
            }
            if (tablas != 5) throw new SQLException(
                    "Faltan tablas de devoluciones. Ejecuta sql/T4_2_5_devoluciones.sql.");
        }
    }
}
