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

            if (in == null) {

                throw new IllegalStateException(

                        "No se encontro " + CONFIG_FILE + " en el classpath. "

                        + "Copia db.properties.example como src/db.properties y ajusta los valores.");

            }

            config.load(in);

        } catch (IOException e) {

            throw new IllegalStateException("Error al leer " + CONFIG_FILE, e);

        }

        this.url = config.getProperty("db.url");

        this.user = config.getProperty("db.user");

        this.password = config.getProperty("db.password");
 
        if (url == null || user == null || password == null) {

            throw new IllegalStateException(

                    "Faltan propiedades (db.url, db.user, db.password) en " + CONFIG_FILE);

        }

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
 
}

 