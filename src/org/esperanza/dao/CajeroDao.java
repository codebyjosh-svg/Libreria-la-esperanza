package org.esperanza.dao;

import java.sql.*;
import java.util.*;

import org.esperanza.model.Libro;
import org.esperanza.model.Usuario;
import org.esperanza.model.Venta;
import org.esperanza.service.SesionUsuario;
import org.esperanza.util.Conexion;

public class CajeroDao {

    private SesionUsuario sesion() {
        SesionUsuario s = SesionUsuario.getInstancia();

        if (!s.esCajero() && !s.esAdmin()) {
            throw new SecurityException(
                    "No tienes acceso a este módulo."
            );
        }

        return s;
    }

    public List<Venta> ventas() throws SQLException {
        return consultarVentas(null);
    }

    public Venta buscarVenta(int id) throws SQLException {
        List<Venta> ventas = consultarVentas(id);

        if (ventas.isEmpty()) {
            throw new SQLException(
                    "La venta no existe o no pertenece a tu usuario."
            );
        }

        return ventas.get(0);
    }

    private List<Venta> consultarVentas(Integer id)
            throws SQLException {

        SesionUsuario s = sesion();

        String sql =
                "SELECT id_venta, fecha_venta, subtotal, descuento, total, "
                + "cui_cliente, id_usuario FROM ventas WHERE 1=1"
                + (s.esCajero() ? " AND id_usuario = ?" : "")
                + (id != null ? " AND id_venta = ?" : "")
                + " ORDER BY id_venta DESC";

        List<Venta> resultado = new ArrayList<>();

        try (Connection c = Conexion.getInstancia().conectar();
             PreparedStatement ps = c.prepareStatement(sql)) {

            int parametro = 1;

            if (s.esCajero()) {
                ps.setInt(
                        parametro++,
                        s.getUsuarioActual().getId()
                );
            }

            if (id != null) {
                ps.setInt(parametro, id);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(new Venta(
                            rs.getInt("id_venta"),
                            rs.getTimestamp("fecha_venta")
                                    .toLocalDateTime(),
                            rs.getBigDecimal("subtotal"),
                            rs.getBigDecimal("descuento"),
                            rs.getBigDecimal("total"),
                            rs.getLong("cui_cliente"),
                            rs.getInt("id_usuario")
                    ));
                }
            }
        }

        return resultado;
    }

    public List<Libro> stock(String busqueda)
            throws SQLException {

        sesion();

        List<Libro> libros = new ArrayList<>();

        String sql =
                "SELECT isbn, titulo, precio, stock_actual FROM libros "
                + "WHERE activo = TRUE "
                + "AND (isbn LIKE ? OR titulo LIKE ?) "
                + "ORDER BY titulo";

        try (Connection c = Conexion.getInstancia().conectar();
             PreparedStatement ps = c.prepareStatement(sql)) {

            String filtro = "%" + busqueda.trim() + "%";

            ps.setString(1, filtro);
            ps.setString(2, filtro);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Libro libro = new Libro();

                    libro.setIsbn(rs.getString("isbn"));
                    libro.setTitulo(rs.getString("titulo"));
                    libro.setPrecio(rs.getDouble("precio"));
                    libro.setStockActual(rs.getInt("stock_actual"));

                    libros.add(libro);
                }
            }
        }

        return libros;
    }

    public List<Usuario> usuarios() throws SQLException {
        sesion();

        List<Usuario> usuarios = new ArrayList<>();

        String sql =
                "SELECT id, username, rol, nombre, apellido, correo, activo "
                + "FROM usuarios ORDER BY nombre, apellido";

        try (Connection c = Conexion.getInstancia().conectar();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                usuarios.add(new Usuario(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("rol"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("correo"),
                        rs.getBoolean("activo")
                ));
            }
        }

        return usuarios;
    }

    public void editarUsuario(
            int id,
            String username,
            String nombre,
            String apellido,
            String correo) throws SQLException {

        SesionUsuario s = sesion();

        if (!username.matches("[A-Za-z0-9._-]{4,20}")
                || nombre.isBlank()
                || apellido.isBlank()
                || !correo.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {

            throw new IllegalArgumentException(
                    "Revisa usuario (4 a 20 caracteres), "
                    + "nombre, apellido y correo."
            );
        }

        try (Connection c = Conexion.getInstancia().conectar()) {

            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT id FROM usuarios "
                    + "WHERE username = ? AND id <> ?")) {

                ps.setString(1, username);
                ps.setInt(2, id);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        throw new IllegalArgumentException(
                                "Ese usuario ya existe."
                        );
                    }
                }
            }

            // Conserva rol, contraseña y estado.
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE usuarios "
                    + "SET username = ?, nombre = ?, "
                    + "apellido = ?, correo = ? WHERE id = ?")) {

                ps.setString(1, username);
                ps.setString(2, nombre);
                ps.setString(3, apellido);
                ps.setString(4, correo);
                ps.setInt(5, id);

                if (ps.executeUpdate() != 1) {
                    throw new SQLException(
                            "No se pudo actualizar el usuario."
                    );
                }
            }
        }

        if (s.getUsuarioActual().getId() == id) {
            Usuario actual = s.getUsuarioActual();

            actual.setUsrname(username);
            actual.setNombre(nombre);
            actual.setApellido(apellido);
            actual.setCorreo(correo);
        }
    }

    public String nombreCajero(int id) throws SQLException {
        sesion();

        try (Connection c = Conexion.getInstancia().conectar();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT nombre, apellido, username "
                     + "FROM usuarios WHERE id = ?")) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String nombre = (
                            Objects.toString(
                                    rs.getString("nombre"), ""
                            )
                            + " "
                            + Objects.toString(
                                    rs.getString("apellido"), ""
                            )
                    ).trim();

                    return nombre.isBlank()
                            ? rs.getString("username")
                            : nombre;
                }
            }
        }

        return "Usuario " + id;
    }
}