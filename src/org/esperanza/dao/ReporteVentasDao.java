package org.esperanza.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.esperanza.model.FiltroFecha;
import org.esperanza.model.Venta;
import org.esperanza.util.Conexion;

public class ReporteVentasDao {

    public List<Venta> obtenerVentasPorFiltro(FiltroFecha filtro) throws SQLException {
        List<Venta> lista = new ArrayList<>();
        String sql = "";

        switch (filtro) {
            case DIA:
                sql = "SELECT * FROM ventas WHERE DATE(fecha) = CURDATE()";
                break;
            case SEMANA:
                sql = "SELECT * FROM ventas WHERE YEARWEEK(fecha, 1) = YEARWEEK(CURDATE(), 1)";
                break;
            case MES:
                sql = "SELECT * FROM ventas WHERE MONTH(fecha) = MONTH(CURDATE()) AND YEAR(fecha) = YEAR(CURDATE())";
                break;
        }

        try (Connection conexion = Conexion.getInstancia().conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Venta venta = new Venta();
                venta.setIdVenta(rs.getInt("id_venta"));
                venta.setTotal(rs.getDouble("total"));
                lista.add(venta);
            }
        }
        return lista;
    }
}