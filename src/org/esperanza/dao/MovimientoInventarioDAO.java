package org.esperanza.dao;

import java.sql.Connection;
import java.sql.SQLException;
import org.esperanza.util.Conexion;

public class MovimientoInventarioDAO {

    public MovimientoInventarioDAO() {
    }

    public Connection obtenerConexion() throws SQLException {
        return Conexion.getInstancia().conectar();
    }
}