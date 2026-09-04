package org.esperanza.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class Conexion {

    private static Conexion instancia;

    private static final String CONFIG_FILE =
            "/db.properties";

    private final String url;
    private final String user;
    private final String password;

    private Conexion() {

        try {

            Class.forName("com.mysql.cj.jdbc.Driver");

        } catch (ClassNotFoundException e) {

            System.err.println(
                    "Error al cargar Driver MySQL: "
                    + e.getMessage()
            );
        }

        Properties config = new Properties();

        try (
            InputStream in =
                    Conexion.class.getResourceAsStream(
                            CONFIG_FILE
                    )
        ) {

            if (in == null) {

                throw new IllegalStateException(
                        "No se encontró "
                        + CONFIG_FILE
                        + " en el proyecto."
                );
            }

            config.load(in);

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Error al leer "
                    + CONFIG_FILE,
                    e
            );
        }

        url = config.getProperty("db.url");
        user = config.getProperty("db.user");
        password = config.getProperty("db.password");

        if (url == null
                || user == null
                || password == null) {

            throw new IllegalStateException(
                    "Faltan datos en db.properties"
            );
        }
    }

    public static synchronized Conexion getInstancia() {

        if (instancia == null) {
            instancia = new Conexion();
        }

        return instancia;
    }

    public Connection conectar()
            throws SQLException {

        return DriverManager.getConnection(
                url,
                user,
                password
        );
    }
}