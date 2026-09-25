package org.esperanza.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Conexion {

    private static Conexion instancia;

    private static final String CONFIG_FILE = "/db.properties";

    private final String url;
    private final String user;
    private final String password;

    private Conexion() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Error Driver: " + e.getMessage());
        }

        Properties config = new Properties();
        try (InputStream in = getClass().getResourceAsStream(CONFIG_FILE)) {
            if (in == null && System.getenv("ESPERANZA_DB_URL") == null) {
                throw new IllegalStateException(
                        "No se encontro " + CONFIG_FILE + " en el classpath. "
                        + "Copia db.properties.example como src/db.properties y ajusta los valores.");
            }
            if (in != null) {
                config.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error al leer " + CONFIG_FILE, e);
        }
        this.url = configuracion(config, "ESPERANZA_DB_URL", "db.url");
        this.user = configuracion(config, "ESPERANZA_DB_USER", "db.user");
        this.password = configuracion(config, "ESPERANZA_DB_PASSWORD", "db.password");

        if (url == null || user == null || password == null) {
            throw new IllegalStateException(
                    "Faltan propiedades (db.url, db.user, db.password) en " + CONFIG_FILE);
        }
    }

    private static String configuracion(Properties config, String variable, String propiedad) {
        String valor = System.getenv(variable);
        return valor != null ? valor : config.getProperty(propiedad);
    }

    public static synchronized Conexion getInstancia() {
        if (instancia == null) {
            instancia = new Conexion();
        }
        return instancia;
    }

    public Connection conectar() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public static Connection getConnection() throws SQLException {
        return getInstancia().conectar();
    }
}
