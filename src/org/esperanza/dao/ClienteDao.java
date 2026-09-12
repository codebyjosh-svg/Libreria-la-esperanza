package org.esperanza.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.esperanza.model.Cliente;
import org.esperanza.util.Conexion;

public class ClienteDao {

    public List<Cliente> listar()
            throws SQLException {

        List<Cliente> clientes =
                new ArrayList<>();

        String sql = """
                SELECT
                    cui,
                    nombre_cliente,
                    apellido_cliente,
                    correo_electronico
                FROM clientes
                ORDER BY nombre_cliente, apellido_cliente
                """;

        try (Connection conexion =
                    Conexion.getInstancia().conectar();

             PreparedStatement ps =
                    conexion.prepareStatement(sql);

             ResultSet rs =
                    ps.executeQuery()) {

            while (rs.next()) {

                clientes.add(
                        leerCliente(rs)
                );
            }
        }

        return clientes;
    }

    public Cliente buscarPorCui(
            long cui) throws SQLException {

        String sql = """
                SELECT
                    cui,
                    nombre_cliente,
                    apellido_cliente,
                    correo_electronico
                FROM clientes
                WHERE cui = ?
                """;

        try (Connection conexion =
                    Conexion.getInstancia().conectar();

             PreparedStatement ps =
                    conexion.prepareStatement(sql)) {

            ps.setLong(
                    1,
                    cui
            );

            try (ResultSet rs =
                    ps.executeQuery()) {

                if (rs.next()) {
                    return leerCliente(rs);
                }
            }
        }

        return null;
    }

    public boolean insertar(
            Cliente cliente) throws SQLException {

        String sql = """
                INSERT INTO clientes
                (
                    cui,
                    nombre_cliente,
                    apellido_cliente,
                    correo_electronico
                )
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conexion =
                    Conexion.getInstancia().conectar();

             PreparedStatement ps =
                    conexion.prepareStatement(sql)) {

            ps.setLong(
                    1,
                    cliente.getCui()
            );

            ps.setString(
                    2,
                    cliente.getNombre()
            );

            ps.setString(
                    3,
                    cliente.getApellido()
            );

            ps.setString(
                    4,
                    cliente.getCorreo()
            );

            return ps.executeUpdate() == 1;
        }
    }

    private Cliente leerCliente(
            ResultSet rs) throws SQLException {

        return new Cliente(
                rs.getLong("cui"),
                rs.getString("nombre_cliente"),
                rs.getString("apellido_cliente"),
                rs.getString("correo_electronico")
        );
    }
}