package org.esperanza.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class Conexion {
    private static final Conexion INSTANCIA = new Conexion();
    private final Properties propiedades = new Properties();

    private Conexion() {
        cargarPropiedades();
    }

    public static Conexion getInstancia() {
        return INSTANCIA;
    }

    private void cargarPropiedades() {
        try (InputStream entrada = Conexion.class.getResourceAsStream("/db.properties")) {
            if (entrada != null) {
                propiedades.load(entrada);
            }
        } catch (IOException e) {
            System.err.println("No se pudo leer db.properties: " + e.getMessage());
        }
    }

    public Connection conectar() throws SQLException {
        String url = System.getProperty("db.url", propiedades.getProperty("db.url"));
        String user = System.getProperty("db.user", propiedades.getProperty("db.user"));
        String password = System.getProperty("db.password", propiedades.getProperty("db.password", ""));

        if (url == null || user == null) {
            throw new SQLException("Falta configurar db.properties (db.url y db.user).");
        }
        return DriverManager.getConnection(url, user, password);
    }
}
