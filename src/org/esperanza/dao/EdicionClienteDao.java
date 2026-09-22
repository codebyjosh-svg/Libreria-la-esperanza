package org.esperanza.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import org.esperanza.model.Cliente;
import org.esperanza.service.SesionUsuario;
import org.esperanza.util.Conexion;

public class EdicionClienteDao {

    public void validarAcceso() {
        SesionUsuario s = SesionUsuario.getInstancia();

        if (!s.esCajero() && !s.esAdmin()) {
            throw new SecurityException(
                    "No tienes permiso para editar clientes."
            );
        }
    }

    public List<Cliente> listar() throws SQLException {
        validarAcceso();
        return new ClienteDao().listar();
    }

    public void actualizar(
            long cui,
            String nombre,
            String apellido,
            String correo
    ) throws SQLException {

        validarAcceso();

        nombre = Objects.toString(nombre, "").trim();
        apellido = Objects.toString(apellido, "").trim();
        correo = Objects.toString(correo, "").trim();

        if (cui <= 0 || nombre.isBlank() || apellido.isBlank()) {
            throw new IllegalArgumentException(
                    "Nombre y apellido son obligatorios."
            );
        }

        if (!correo.isEmpty()
                && !correo.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
            throw new IllegalArgumentException(
                    "Escribe un correo válido o deja el campo vacío."
            );
        }

        String sql =
                "UPDATE clientes SET nombre_cliente = ?, "
                + "apellido_cliente = ?, correo_electronico = ? "
                + "WHERE cui = ?";

        try (
                Connection c = Conexion.getInstancia().conectar();
                PreparedStatement ps = c.prepareStatement(sql)
        ) {
            ps.setString(1, nombre);
            ps.setString(2, apellido);
            ps.setString(3, correo);
            ps.setLong(4, cui);

            if (ps.executeUpdate() != 1) {
                throw new SQLException(
                        "No se pudo actualizar el cliente. Actualiza la lista."
                );
            }
        }
    }
}