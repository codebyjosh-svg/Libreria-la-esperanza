package org.esperanza.dao;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

import org.esperanza.service.SesionUsuario;
import org.esperanza.util.Conexion;

public class CatalogoDao {

    public record Campo(
            String nombre,
            int tipo,
            int longitud,
            boolean obligatorio,
            boolean generado,
            String defecto) {
    }

    private final String tabla;

    public CatalogoDao(String tabla) {
        if (!Set.of(
                "clientes",
                "categorias",
                "proveedores",
                "autores",
                "editoriales"
        ).contains(tabla)) {
            throw new IllegalArgumentException(
                    "Catálogo no permitido."
            );
        }

        this.tabla = tabla;
        validarAcceso();
    }

    private void validarAcceso() {
        SesionUsuario sesion = SesionUsuario.getInstancia();

        boolean permitido = sesion.esAdmin()
                || (tabla.equals("clientes") && sesion.esCajero())
                || (tabla.equals("categorias") && sesion.esBodega());

        if (!permitido) {
            throw new SecurityException(
                    "No tienes permiso para este catálogo."
            );
        }
    }

    private String identificador(String nombre) {
        return "`" + nombre.replace("`", "``") + "`";
    }

    public List<Campo> campos() throws SQLException {
        validarAcceso();

        List<Campo> campos = new ArrayList<>();

        try (Connection conexion = Conexion.getInstancia().conectar();
             ResultSet rs = conexion.getMetaData().getColumns(
                     conexion.getCatalog(),
                     null,
                     tabla,
                     null)) {

            while (rs.next()) {
                boolean generado =
                        "YES".equals(rs.getString("IS_AUTOINCREMENT"))
                        || "YES".equals(
                                rs.getString("IS_GENERATEDCOLUMN")
                        );

                campos.add(new Campo(
                        rs.getString("COLUMN_NAME"),
                        rs.getInt("DATA_TYPE"),
                        rs.getInt("COLUMN_SIZE"),
                        rs.getInt("NULLABLE")
                                == DatabaseMetaData.columnNoNulls,
                        generado,
                        rs.getString("COLUMN_DEF")
                ));
            }
        }

        if (campos.isEmpty()) {
            throw new SQLException(
                    "No se encontró la tabla " + tabla
                    + ". Revisa la base de datos seleccionada."
            );
        }

        return campos;
    }

    public List<Map<String, String>> listar() throws SQLException {
        validarAcceso();

        List<Map<String, String>> filas = new ArrayList<>();

        String sql = "SELECT * FROM " + identificador(tabla);

        try (Connection conexion = Conexion.getInstancia().conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int cantidadColumnas = rs.getMetaData().getColumnCount();

            while (rs.next()) {
                Map<String, String> fila = new LinkedHashMap<>();

                for (int i = 1; i <= cantidadColumnas; i++) {
                    fila.put(
                            rs.getMetaData().getColumnName(i),
                            Objects.toString(rs.getString(i), "")
                    );
                }

                filas.add(fila);
            }
        }

        return filas;
    }

    public void insertar(Map<String, String> valores)
            throws SQLException {

        validarAcceso();

        List<Campo> usados = new ArrayList<>();
        List<Object> parametros = new ArrayList<>();

        for (Campo campo : campos()) {
            if (campo.generado()) {
                continue;
            }

            String valor = valores
                    .getOrDefault(campo.nombre(), "")
                    .trim();

            if (valor.isEmpty()) {
                if (campo.obligatorio() && campo.defecto() == null) {
                    throw new IllegalArgumentException(
                            "Completa el campo "
                            + campo.nombre().replace('_', ' ')
                            + "."
                    );
                }

                continue;
            }

            if ((campo.nombre().contains("correo")
                    || campo.nombre().contains("email"))
                    && !valor.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {

                throw new IllegalArgumentException(
                        "Escribe un correo válido."
                );
            }

            Object convertido;

            try {
                convertido = switch (campo.tipo()) {
                    case Types.INTEGER,
                         Types.BIGINT,
                         Types.SMALLINT,
                         Types.TINYINT -> Long.valueOf(valor);

                    case Types.DECIMAL,
                         Types.NUMERIC,
                         Types.DOUBLE,
                         Types.FLOAT,
                         Types.REAL -> new BigDecimal(valor);

                    case Types.DATE -> java.sql.Date.valueOf(valor);

                    case Types.TIMESTAMP -> Timestamp.valueOf(valor);

                    case Types.BOOLEAN, Types.BIT -> {
                        if (!Set.of("0", "1", "true", "false")
                                .contains(valor.toLowerCase())) {
                            throw new IllegalArgumentException();
                        }

                        yield valor.equals("1")
                                || valor.equalsIgnoreCase("true");
                    }

                    default -> {
                        if (campo.longitud() > 0
                                && valor.length() > campo.longitud()) {
                            throw new IllegalArgumentException();
                        }

                        yield valor;
                    }
                };
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException(
                        "Revisa el valor de "
                        + campo.nombre().replace('_', ' ')
                        + "."
                );
            }

            if (campo.nombre().equalsIgnoreCase("cui")
                    && (!valor.matches("[0-9]{13}")
                    || Long.parseLong(valor) <= 0)) {

                throw new IllegalArgumentException(
                        "El CUI debe contener 13 dígitos."
                );
            }

            usados.add(campo);
            parametros.add(convertido);
        }

        if (usados.isEmpty()) {
            throw new IllegalArgumentException(
                    "Completa los datos del registro."
            );
        }

        String columnas = String.join(
                ",",
                usados.stream()
                        .map(campo -> identificador(campo.nombre()))
                        .toList()
        );

        String marcas = String.join(
                ",",
                Collections.nCopies(usados.size(), "?")
        );

        String sql = "INSERT INTO " + identificador(tabla)
                + " (" + columnas + ") VALUES (" + marcas + ")";

        try (Connection conexion = Conexion.getInstancia().conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }

            if (ps.executeUpdate() != 1) {
                throw new SQLException(
                        "No se guardó el registro."
                );
            }
        }
    }
}