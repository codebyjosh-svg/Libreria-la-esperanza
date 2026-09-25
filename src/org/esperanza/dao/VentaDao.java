package org.esperanza.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.esperanza.Model.DetalleVenta;
import org.esperanza.Model.DescuentoVenta;
import org.esperanza.util.Conexion;


public class VentaDao {


    public boolean registrarVenta(
            String cuiCliente,
            int idUsuario,
            List<DetalleVenta> detalles,
            DescuentoVenta descuento
    ) throws SQLException {


        Connection conexion = null;


        try {


            conexion = Conexion.getInstancia()
                    .conectar();


            conexion.setAutoCommit(false);



            BigDecimal subtotalVenta =
                    BigDecimal.ZERO;



            for(DetalleVenta detalle : detalles){

                subtotalVenta =
                        subtotalVenta.add(
                                detalle.getSubtotal()
                        );

            }



            BigDecimal montoDescuento =
                    descuento.calcularMonto(
                            subtotalVenta
                    );



            BigDecimal total =
                    subtotalVenta.subtract(
                            montoDescuento
                    );



            String sqlVenta =
                    """
                    INSERT INTO ventas
                    (
                        subtotal,
                        descuento,
                        total,
                        estado,
                        cui_cliente,
                        id_usuario
                    )
                    VALUES
                    (?, ?, ?, 'COMPLETADA', ?, ?)
                    """;



            int idVenta;



            try(
                PreparedStatement ps =
                    conexion.prepareStatement(
                        sqlVenta,
                        PreparedStatement.RETURN_GENERATED_KEYS
                    )
            ){


                ps.setBigDecimal(
                        1,
                        subtotalVenta
                );


                ps.setBigDecimal(
                        2,
                        montoDescuento
                );


                ps.setBigDecimal(
                        3,
                        total
                );


                ps.setLong(
                        4,
                        Long.parseLong(cuiCliente)
                );


                ps.setInt(
                        5,
                        idUsuario
                );


                ps.executeUpdate();



                ResultSet rs =
                        ps.getGeneratedKeys();



                if(!rs.next()){

                    throw new SQLException(
                            "No se generó el ID de venta."
                    );

                }


                idVenta =
                        rs.getInt(1);

            }




            String sqlDetalle =
                    """
                    INSERT INTO detalle_venta
                    (
                        id_venta,
                        isbn,
                        cantidad,
                        precio_unitario,
                        subtotal
                    )
                    VALUES
                    (?, ?, ?, ?, ?)
                    """;



            String sqlStock =
                    """
                    UPDATE libros
                    SET stock_actual = stock_actual - ?,
                        activo = 1
                    WHERE isbn = ?
                    """;



            try(
                PreparedStatement psDetalle =
                    conexion.prepareStatement(sqlDetalle);

                PreparedStatement psStock =
                    conexion.prepareStatement(sqlStock)
            ){



                for(DetalleVenta detalle : detalles){



                    psDetalle.setInt(
                            1,
                            idVenta
                    );


                    psDetalle.setString(
                            2,
                            detalle.getIsbn()
                    );


                    psDetalle.setInt(
                            3,
                            detalle.getCantidad()
                    );


                    psDetalle.setBigDecimal(
                            4,
                            detalle.getPrecioUnitario()
                    );


                    psDetalle.setBigDecimal(
                            5,
                            detalle.getSubtotal()
                    );


                    psDetalle.executeUpdate();



                    psStock.setInt(
                            1,
                            detalle.getCantidad()
                    );


                    psStock.setString(
                            2,
                            detalle.getIsbn()
                    );


                    psStock.executeUpdate();



                }

            }



            conexion.commit();


            return true;



        }catch(Exception e){


            if(conexion != null){

                conexion.rollback();

            }


            throw new SQLException(
                    "Error registrando venta: "
                    + e.getMessage()
            );



        }finally{


            if(conexion != null){

                conexion.close();

            }


        }

    }

}