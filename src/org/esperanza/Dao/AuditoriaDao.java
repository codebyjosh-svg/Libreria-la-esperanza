package org.esperanza.Dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.esperanza.util.Conexion;

/**
 * Registra sesiones y cambios realizados por cada usuario.
 */
public class AuditoriaDao {

    public long iniciarSesion(int usuarioId) {
        String sql = "INSERT INTO auditoria_uso (usuario_id, tipo, detalle, inicio) VALUES (?, 'SESION', 'Inicio de uso del sistema', NOW())";
        try (Connection c = Conexion.getInstancia().conectar();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, usuarioId);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException e) {
            System.err.println("Error al iniciar auditoría: " + e.getMessage());
        }
        return -1;
    }

    public void cerrarSesion(long auditoriaId) {
        if (auditoriaId <= 0) return;
        String sql = "UPDATE auditoria_uso SET fin = NOW(), duracion_segundos = TIMESTAMPDIFF(SECOND, inicio, NOW()) WHERE id = ? AND tipo = 'SESION' AND fin IS NULL";
        try (Connection c = Conexion.getInstancia().conectar();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, auditoriaId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al cerrar auditoría: " + e.getMessage());
        }
    }

    public void registrarCambio(int usuarioId, String tipo, String detalle) {
        String sql = "INSERT INTO auditoria_uso (usuario_id, tipo, detalle, inicio, fin, duracion_segundos) VALUES (?, ?, ?, NOW(), NOW(), 0)";
        try (Connection c = Conexion.getInstancia().conectar();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ps.setString(2, tipo);
            ps.setString(3, detalle);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al registrar cambio: " + e.getMessage());
        }
    }

    public List<ReporteUso> obtenerReporte() {
        List<ReporteUso> lista = new ArrayList<>();
        String sql = """
            SELECT u.id, u.username,
                   COALESCE(SUM(CASE WHEN a.tipo='SESION' THEN
                       CASE WHEN a.fin IS NULL THEN TIMESTAMPDIFF(SECOND, a.inicio, NOW())
                            ELSE a.duracion_segundos END
                       ELSE 0 END),0) AS segundos,
                   COALESCE(SUM(CASE WHEN a.tipo IN ('ALTA_USUARIO','DESACTIVAR_USUARIO','CAMBIO_CONTRASENA') THEN 1 ELSE 0 END),0) AS cambios,
                   MAX(a.inicio) AS ultima_actividad
            FROM usuarios u
            LEFT JOIN auditoria_uso a ON a.usuario_id = u.id
            GROUP BY u.id, u.username
            ORDER BY u.username
            """;
        try (Connection c = Conexion.getInstancia().conectar();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ReporteUso r = new ReporteUso(
                    rs.getInt("id"), rs.getString("username"),
                    rs.getLong("segundos"), rs.getInt("cambios"),
                    rs.getTimestamp("ultima_actividad")
                );
                r.setCambios(detallesCambios(rs.getInt("id")));
                lista.add(r);
            }
        } catch (SQLException e) {
            System.err.println("Error al generar reporte: " + e.getMessage());
        }
        return lista;
    }

    private String detallesCambios(int usuarioId) {
        String sql = "SELECT tipo, detalle, inicio FROM auditoria_uso WHERE usuario_id=? AND tipo IN ('ALTA_USUARIO','DESACTIVAR_USUARIO','CAMBIO_CONTRASENA') ORDER BY inicio DESC";
        StringBuilder sb = new StringBuilder();
        try (Connection c = Conexion.getInstancia().conectar();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (sb.length() > 0) sb.append("\n");
                    sb.append(rs.getTimestamp("inicio"))
                      .append(" - ").append(rs.getString("tipo"))
                      .append(": ").append(rs.getString("detalle"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al consultar cambios: " + e.getMessage());
        }
        return sb.toString();
    }

    public static class ReporteUso {
        private final int id;
        private final String username;
        private final long segundos;
        private final int cambios;
        private final Timestamp ultimaActividad;
        private String cambiosTexto = "";

        public ReporteUso(int id, String username, long segundos, int cambios, Timestamp ultimaActividad) {
            this.id = id; this.username = username; this.segundos = segundos;
            this.cambios = cambios; this.ultimaActividad = ultimaActividad;
        }
        public int getId() { return id; }
        public String getUsername() { return username; }
        public long getSegundos() { return segundos; }
        public int getCambios() { return cambios; }
        public Timestamp getUltimaActividad() { return ultimaActividad; }
        public String getTiempoTotal() {
            long s = segundos;
            long d = s / 86400; s %= 86400;
            long h = s / 3600; s %= 3600;
            long m = s / 60; s %= 60;
            if (d > 0) return String.format("%dd %02dh %02dm", d, h, m);
            return String.format("%02dh %02dm %02ds", h, m, s);
        }
        public String getCambiosTexto() { return cambiosTexto; }
        public void setCambios(String texto) { this.cambiosTexto = texto == null ? "" : texto; }
    }
}
