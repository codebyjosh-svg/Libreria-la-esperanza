import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.esperanza.model.LibroMasVendido;
import org.esperanza.model.StockValorizado;
import org.esperanza.dao.ReporteInventarioDao;
import org.esperanza.util.Conexion;

public class PruebaReporteInventario {

    public static void main(String[] args) {

        System.out.println("========================================");
        System.out.println("T4.3.8 - PRUEBA REPORTES CONTRA SQL");
        System.out.println("========================================");

        try {

            ReporteInventarioDao dao =
                    new ReporteInventarioDao();

            boolean rankingCorrecto =
                    probarRankingContraSql(dao);

            boolean stockCorrecto =
                    probarStockContraSql(dao);

            System.out.println();
            System.out.println("========================================");
            System.out.println("RESULTADO FINAL");
            System.out.println("========================================");

            if (rankingCorrecto && stockCorrecto) {

                System.out.println(
                        "PRUEBA EXITOSA - T4.3.8"
                );

                System.out.println(
                        "Los resultados del DAO coinciden con SQL."
                );

            } else {

                System.out.println(
                        "PRUEBA FALLIDA - T4.3.8"
                );
            }

        } catch (Exception ex) {

            System.out.println(
                    "ERROR DURANTE LA PRUEBA: "
                    + ex.getMessage()
            );

            ex.printStackTrace();
        }
    }

    private static boolean probarRankingContraSql(
            ReporteInventarioDao dao) throws Exception {

        System.out.println();
        System.out.println("----------------------------------------");
        System.out.println("PRUEBA 1 - LIBROS MAS VENDIDOS");
        System.out.println("----------------------------------------");

        List<LibroMasVendido> datosDao =
                dao.listarLibrosMasVendidos();

        String sql = """
                SELECT
                    l.isbn,
                    l.titulo,
                    SUM(dv.cantidad) AS cantidad_vendida,
                    COALESCE(SUM(dv.subtotal), 0) AS total_vendido
                FROM detalle_venta dv
                INNER JOIN ventas v
                    ON v.id_venta = dv.id_venta
                INNER JOIN libros l
                    ON l.isbn = dv.isbn
                WHERE v.estado = 'COMPLETADA'
                GROUP BY
                    l.isbn,
                    l.titulo
                ORDER BY
                    cantidad_vendida DESC,
                    total_vendido DESC,
                    l.titulo ASC
                """;

        int posicion = 0;
        boolean correcto = true;

        try (
                Connection conexion =
                        Conexion.getInstancia().conectar();

                PreparedStatement ps =
                        conexion.prepareStatement(sql);

                ResultSet rs =
                        ps.executeQuery()
        ) {

            while (rs.next()) {

                if (posicion >= datosDao.size()) {

                    System.out.println(
                            "ERROR: SQL devuelve más filas que el DAO."
                    );

                    return false;
                }

                LibroMasVendido daoLibro =
                        datosDao.get(posicion);

                String sqlIsbn =
                        rs.getString("isbn");

                String sqlTitulo =
                        rs.getString("titulo");

                int sqlCantidad =
                        rs.getInt("cantidad_vendida");

                BigDecimal sqlTotal =
                        rs.getBigDecimal("total_vendido");

                boolean filaCorrecta =
                        sqlIsbn.equals(daoLibro.getIsbn())
                        && sqlTitulo.equals(daoLibro.getTitulo())
                        && sqlCantidad
                                == daoLibro.getCantidadVendida()
                        && compararDecimal(
                                sqlTotal,
                                daoLibro.getTotalVendido()
                        );

                System.out.println(
                        (posicion + 1)
                        + ". "
                        + sqlTitulo
                        + " | SQL: "
                        + sqlCantidad
                        + " unidades - Q"
                        + sqlTotal
                        + " | DAO: "
                        + daoLibro.getCantidadVendida()
                        + " unidades - Q"
                        + daoLibro.getTotalVendido()
                        + " | "
                        + (filaCorrecta
                                ? "CORRECTO"
                                : "ERROR")
                );

                if (!filaCorrecta) {
                    correcto = false;
                }

                posicion++;
            }
        }

        if (posicion != datosDao.size()) {

            System.out.println(
                    "ERROR: El DAO devuelve una cantidad "
                    + "diferente de filas."
            );

            correcto = false;
        }

        System.out.println(
                correcto
                        ? "RANKING CORRECTO"
                        : "RANKING INCORRECTO"
        );

        return correcto;
    }

    private static boolean probarStockContraSql(
            ReporteInventarioDao dao) throws Exception {

        System.out.println();
        System.out.println("----------------------------------------");
        System.out.println("PRUEBA 2 - STOCK VALORIZADO");
        System.out.println("----------------------------------------");

        List<StockValorizado> datosDao =
                dao.listarStockValorizado();

        String sql = """
                SELECT
                    isbn,
                    titulo,
                    stock_actual,
                    precio,
                    CAST(
                        stock_actual * precio
                        AS DECIMAL(14,2)
                    ) AS valor_inventario
                FROM libros
                WHERE activo = TRUE
                ORDER BY
                    valor_inventario DESC,
                    titulo ASC
                """;

        int posicion = 0;
        boolean correcto = true;

        try (
                Connection conexion =
                        Conexion.getInstancia().conectar();

                PreparedStatement ps =
                        conexion.prepareStatement(sql);

                ResultSet rs =
                        ps.executeQuery()
        ) {

            while (rs.next()) {

                if (posicion >= datosDao.size()) {

                    System.out.println(
                            "ERROR: SQL devuelve más filas que el DAO."
                    );

                    return false;
                }

                StockValorizado daoLibro =
                        datosDao.get(posicion);

                String sqlIsbn =
                        rs.getString("isbn");

                String sqlTitulo =
                        rs.getString("titulo");

                int sqlStock =
                        rs.getInt("stock_actual");

                BigDecimal sqlPrecio =
                        rs.getBigDecimal("precio");

                BigDecimal sqlValor =
                        rs.getBigDecimal("valor_inventario");

                boolean filaCorrecta =
                        sqlIsbn.equals(daoLibro.getIsbn())
                        && sqlTitulo.equals(daoLibro.getTitulo())
                        && sqlStock
                                == daoLibro.getStockActual()
                        && compararDecimal(
                                sqlPrecio,
                                daoLibro.getPrecio()
                        )
                        && compararDecimal(
                                sqlValor,
                                daoLibro.getValorInventario()
                        );

                System.out.println(
                        (posicion + 1)
                        + ". "
                        + sqlTitulo
                        + " | SQL: Q"
                        + sqlValor
                        + " | DAO: Q"
                        + daoLibro.getValorInventario()
                        + " | "
                        + (filaCorrecta
                                ? "CORRECTO"
                                : "ERROR")
                );

                if (!filaCorrecta) {
                    correcto = false;
                }

                posicion++;
            }
        }

        if (posicion != datosDao.size()) {

            System.out.println(
                    "ERROR: El DAO devuelve una cantidad "
                    + "diferente de filas."
            );

            correcto = false;
        }

        System.out.println(
                correcto
                        ? "STOCK VALORIZADO CORRECTO"
                        : "STOCK VALORIZADO INCORRECTO"
        );

        return correcto;
    }

    private static boolean compararDecimal(
            BigDecimal valor1,
            BigDecimal valor2) {

        if (valor1 == null && valor2 == null) {
            return true;
        }

        if (valor1 == null || valor2 == null) {
            return false;
        }

        return valor1.compareTo(valor2) == 0;
    }
}