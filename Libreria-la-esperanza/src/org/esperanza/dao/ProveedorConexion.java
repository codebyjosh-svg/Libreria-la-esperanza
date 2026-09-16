package org.esperanza.dao;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface ProveedorConexion {

    Connection conectar() throws SQLException;
}