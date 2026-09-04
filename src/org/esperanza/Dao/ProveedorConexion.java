package org.esperanza.Dao;

import java.sql.Connection;
import java.sql.SQLException;

/** Permite usar la conexion real o una conexion de pruebas. */
@FunctionalInterface
public interface ProveedorConexion {
    Connection conectar() throws SQLException;
}

