package org.esperanza.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.esperanza.Model.Categoria;
import org.esperanza.util.Conexion;

public class CategoriaDao {

    public List<Categoria> listarCategorias()
            throws SQLException {

        List<Categoria> lista = new ArrayList<>();

        String sql = """
                SELECT id_categoria, nombre_categoria
                FROM categorias
                ORDER BY nombre_categoria
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
                        rs.getString("nombre_categoria")
                );

                lista.add(categoria);
            }
        }

        return lista;
    }

    public void insertarCategoria(
            Categoria categoria)
            throws SQLException {

        String sql = """
                INSERT INTO categorias
                (nombre_categoria)
                VALUES (?)
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

            ps.executeUpdate();
        }
    }

    public void actualizarCategoria(
            Categoria categoria)
            throws SQLException {

        String sql = """
                UPDATE categorias
                SET nombre_categoria = ?
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

            ps.setInt(
                    2,
                    categoria.getIdCategoria()
            );

            ps.executeUpdate();
        }
    }

    public boolean existeNombre(
            String nombre,
            Integer idCategoriaExcluir)
            throws SQLException {

        String sql;

        if (idCategoriaExcluir == null) {

            sql = """
                    SELECT COUNT(*)
                    FROM categorias
                    WHERE LOWER(nombre_categoria) = LOWER(?)
                    """;

        } else {

            sql = """
                    SELECT COUNT(*)
                    FROM categorias
                    WHERE LOWER(nombre_categoria) = LOWER(?)
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