package org.esperanza.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Conexión a MySQL (singleton).
 * Lee src/db.properties del classpath.
 * Autor: mi nombre
 */
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
            System.err.println("Error Driver MySQL: " + e.getMessage());
        }

        Properties config = new Properties();
        try (InputStream in = getClass().getResourceAsStream(CONFIG_FILE)) {
            if (in == null) {
                throw new IllegalStateException(
                        "No se encontro " + CONFIG_FILE + " en el classpath. "
                        + "Asegurate de tener src/db.properties y haz Clean and Build.");
            }
            config.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Error al leer " + CONFIG_FILE, e);
        }

        this.url = trim(config.getProperty("db.url"));
        this.user = trim(config.getProperty("db.user"));
        this.password = trim(config.getProperty("db.password"));

        if (url == null || url.isEmpty() || user == null || user.isEmpty()) {
            throw new IllegalStateException(
                    "Faltan db.url o db.user en " + CONFIG_FILE);
        }

        System.out.println("[Conexion] URL=" + url + " | user=" + user
                + " | password=" + (password == null || password.isEmpty() ? "(vacia)" : "***"));
    }

    private static String trim(String v) {
        return v == null ? null : v.trim();
    }

    public static synchronized Conexion getInstancia() {
        if (instancia == null) {
            instancia = new Conexion();
        }
        return instancia;
    }

    public Connection conectar() throws SQLException {
        try {
            return DriverManager.getConnection(url, user, password == null ? "" : password);
        } catch (SQLException e) {
            System.err.println("[Conexion] Error al conectar: " + e.getMessage());
            System.err.println("[Conexion] Revisa src/db.properties (usuario y contraseña de MySQL).");
            throw e;
        }
    }
}
