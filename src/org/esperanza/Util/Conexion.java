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

    //Constructor privado para evitar que hagan "new Conexion()" fuera de esta clase
    private Conexion() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Error Driver: " + e.getMessage());
        }

        //consumo o uso del properties empezamos creando una clase Properties
        Properties config = new Properties();
        //cargar archivo db.properties
        try (InputStream in = getClass().getResourceAsStream(CONFIG_FILE)) {
            if (in == null) {
                //excepcion si no encuentra el archivo o no existe
                throw new IllegalStateException(
                        "No se encontro " + CONFIG_FILE + " en el classpath. "
                        + "Copia db.properties.example como src/db.properties y ajusta los valores.");
            }
            //cargamos el archivo dentro de la clase propertis
            config.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Error al leer " + CONFIG_FILE, e);
        }
        this.url = config.getProperty("db.url");
        this.user = config.getProperty("db.user");
        this.password = config.getProperty("db.password");

        //comprobación de datos del properties nulos para cada atributo o datos nulos.
        if (url == null || user == null || password == null) {
            throw new IllegalStateException(
                    "Faltan propiedades (db.url, db.user, db.password) en " + CONFIG_FILE);
        }
    }

    //Método público estático para obtener la única instancia del Gestor
    public static synchronized Conexion getInstancia() {
        if (instancia == null) {
            instancia = new Conexion();
        }
        return instancia;
    }

    //Método para entregar una conexión fresca cada vez que se pida
    public Connection conectar() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

}
