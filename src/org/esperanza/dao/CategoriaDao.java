package org.esperanza.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.esperanza.model.Categoria;
import org.esperanza.util.Conexion;

public class CategoriaDao {

    public List<Categoria> listarCategorias() throws SQLException {

        List<Categoria> lista = new ArrayList<>();

        String sql = """
                SELECT id_categoria, nombre, descripcion
                FROM categorias
                ORDER BY nombre
                """;

        try (
                Connection conexion =
                        Conexion.getInstancia().conectar();

                PreparedStatement ps =
                        conexion.prepareStatement(sql);

                ResultSet rs =
                        ps.executeQuery()
        ) {

            while (rs.next()) {

                Categoria categoria = new Categoria();

                categoria.setIdCategoria(
                        rs.getInt("id_categoria")
                );

                categoria.setNombre(
                        rs.getString("nombre")
                );

                categoria.setDescripcion(
                        rs.getString("descripcion")
                );

                lista.add(categoria);
            }
        }

        return lista;
    }

    public void insertarCategoria(
            Categoria categoria) throws SQLException {

        String sql = """
                INSERT INTO categorias
                (nombre, descripcion)
                VALUES (?, ?)
                """;

        try (
                Connection conexion =
                        Conexion.getInstancia().conectar();

                PreparedStatement ps =
                        conexion.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    categoria.getNombre()
            );

            ps.setString(
                    2,
                    categoria.getDescripcion()
            );

            ps.executeUpdate();
        }
    }

    public void actualizarCategoria(
            Categoria categoria) throws SQLException {

        String sql = """
                UPDATE categorias
                SET nombre = ?, descripcion = ?
                WHERE id_categoria = ?
                """;

        try (
                Connection conexion =
                        Conexion.getInstancia().conectar();

                PreparedStatement ps =
                        conexion.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    categoria.getNombre()
            );

            ps.setString(
                    2,
                    categoria.getDescripcion()
            );

            ps.setInt(
                    3,
                    categoria.getIdCategoria()
            );

            ps.executeUpdate();
        }
    }

    public boolean existeNombre(
            String nombre,
            Integer idCategoriaExcluir) throws SQLException {

        String sql;

        if (idCategoriaExcluir == null) {

            sql = """
                    SELECT COUNT(*)
                    FROM categorias
                    WHERE LOWER(nombre) = LOWER(?)
                    """;

        } else {

            sql = """
                    SELECT COUNT(*)
                    FROM categorias
                    WHERE LOWER(nombre) = LOWER(?)
                    AND id_categoria <> ?
                    """;
        }

        try (
                Connection conexion =
                        Conexion.getInstancia().conectar();

                PreparedStatement ps =
                        conexion.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    nombre.trim()
            );

            if (idCategoriaExcluir != null) {
                ps.setInt(
                        2,
                        idCategoriaExcluir
                );
            }

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }
}