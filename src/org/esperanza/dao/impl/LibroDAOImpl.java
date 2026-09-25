package org.esperanza.dao.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.esperanza.dao.LibroDAO;
import org.esperanza.Model.Libro;
import org.esperanza.util.Conexion;


public class LibroDAOImpl implements LibroDAO {


    @Override
    public List<Libro> listarDisponibles() throws SQLException {

        List<Libro> lista = new ArrayList<>();

        String sql =
                "SELECT * FROM libros "
                + "WHERE stock_actual > 0 "
                + "AND activo = 1";


        try (
            Connection conn = Conexion.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {


            while (rs.next()) {

                Libro libro = new Libro();


                libro.setIsbn(
                        rs.getString("isbn")
                );


                libro.setTitulo(
                        rs.getString("titulo")
                );


                libro.setPrecio(
                        rs.getDouble("precio")
                );


                libro.setStockActual(
                        rs.getInt("stock_actual")
                );


                libro.setStockMinimo(
                        rs.getInt("stock_minimo")
                );


                libro.setFechaPublicacion(
                        rs.getDate("fecha_publicacion")
                );


                libro.setIdCategoria(
                        rs.getInt("id_categoria")
                );


                libro.setNitEditorial(
                        rs.getString("nit_editorial")
                );


                libro.setIdProveedor(
                        rs.getInt("id_proveedor")
                );


                libro.setActivo(
                        rs.getBoolean("activo")
                );


                lista.add(libro);

            }

        }


        return lista;
    }



    @Override
    public List<Libro> listarTodos() {


        List<Libro> lista = new ArrayList<>();

        String sql =
                "SELECT * FROM libros";


        try (
            Connection conn = Conexion.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {


            while(rs.next()) {

                Libro libro = new Libro();


                libro.setIsbn(
                        rs.getString("isbn")
                );

                libro.setTitulo(
                        rs.getString("titulo")
                );

                libro.setPrecio(
                        rs.getDouble("precio")
                );

                libro.setStockActual(
                        rs.getInt("stock_actual")
                );


                lista.add(libro);

            }


        } catch(SQLException e){

            e.printStackTrace();

        }


        return lista;
    }



    @Override
    public Libro buscarLibro(String isbn) {


        String sql =
                "SELECT * FROM libros WHERE isbn = ?";


        try(
            Connection conn = Conexion.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ){


            stmt.setString(1, isbn);


            ResultSet rs =
                    stmt.executeQuery();


            if(rs.next()){


                Libro libro = new Libro();


                libro.setIsbn(
                        rs.getString("isbn")
                );


                libro.setTitulo(
                        rs.getString("titulo")
                );


                libro.setPrecio(
                        rs.getDouble("precio")
                );


                libro.setStockActual(
                        rs.getInt("stock_actual")
                );


                return libro;

            }


        }catch(SQLException e){

            e.printStackTrace();

        }


        return null;
    }



    @Override
    public Libro buscarPorIsbn(String isbn) {

        return buscarLibro(isbn);

    }



    @Override
    public List<Libro> buscarPorTitulo(String titulo) {

        return new ArrayList<>();

    }



    @Override
    public List<Libro> buscarPorAutor(String autor) {

        return new ArrayList<>();

    }



    @Override
    public List<Libro> obtenerStockCritico() {


        List<Libro> lista = new ArrayList<>();


        String sql =
                "SELECT * FROM libros "
                + "WHERE stock_actual <= stock_minimo";


        try(
            Connection conn = Conexion.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ){


            while(rs.next()){


                Libro libro = new Libro();


                libro.setIsbn(
                        rs.getString("isbn")
                );


                libro.setTitulo(
                        rs.getString("titulo")
                );


                libro.setPrecio(
                        rs.getDouble("precio")
                );


                libro.setStockActual(
                        rs.getInt("stock_actual")
                );


                lista.add(libro);

            }


        }catch(SQLException e){

            e.printStackTrace();

        }


        return lista;
    }



    @Override
    public boolean insertar(Libro libro) {

        return false;

    }



    @Override
    public boolean actualizar(Libro libro) {

        return false;

    }



    @Override
    public boolean eliminar(String isbn) {

        return false;

    }



    @Override
    public boolean actualizarPrecio(
            String isbn,
            BigDecimal nuevoPrecio) {


        String sql =
                "UPDATE libros "
                + "SET precio = ? "
                + "WHERE isbn = ?";


        try(
            Connection conn = Conexion.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ){


            stmt.setBigDecimal(
                    1,
                    nuevoPrecio
            );


            stmt.setString(
                    2,
                    isbn
            );


            return stmt.executeUpdate() > 0;


        }catch(SQLException e){

            e.printStackTrace();

        }


        return false;
    }

}