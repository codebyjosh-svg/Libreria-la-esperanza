package org.esperanza.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.esperanza.model.DetalleVenta;
import org.esperanza.util.Conexion;

public class DatosVentaDao {

    public String obtenerNombreCliente(long cui) throws SQLException {
        String sql = """
                SELECT nombre_cliente, apellido_cliente
                FROM clientes
                WHERE cui = ?
                """;

        try (Connection cn = Conexion.getInstancia().conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setLong(1, cui);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nombre_cliente") + " " + rs.getString("apellido_cliente");
                }
            }
        }

        return "Cliente no encontrado";
    }

    public Map<String, String> obtenerTitulos(List<DetalleVenta> detalles) throws SQLException {
        Map<String, String> titulos = new LinkedHashMap<>();
        String sql = "SELECT titulo FROM libros WHERE isbn = ?";

        try (Connection cn = Conexion.getInstancia().conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            for (DetalleVenta detalle : detalles) {
                ps.setString(1, detalle.getIsbn());

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        titulos.put(detalle.getIsbn(), rs.getString("titulo"));
                    } else {
                        titulos.put(detalle.getIsbn(), "Libro no encontrado");
                    }
                }
            }
        }

        return titulos;
    }
}