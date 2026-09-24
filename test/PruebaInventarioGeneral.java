import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.esperanza.dao.LibroDAO;
import org.esperanza.dao.impl.LibroDAOImpl;
import org.esperanza.model.Libro;
import org.esperanza.util.Conexion;

public class PruebaInventarioGeneral {

    public static void main(String[] args) {

        System.out.println("========================================");
        System.out.println("T4.I.5 - PRUEBAS DE INVENTARIO");
        System.out.println("========================================");

        try {

            LibroDAO libroDAO = new LibroDAOImpl();

            boolean pruebaListado =
                    probarListadoInventario(libroDAO);

            boolean pruebaStock =
                    probarValoresStock(libroDAO);

            boolean pruebaCriticos =
                    probarStockCritico(libroDAO);

            boolean pruebaSql =
                    compararCantidadContraSql(libroDAO);

            System.out.println();
            System.out.println("========================================");
            System.out.println("RESULTADO FINAL");
            System.out.println("========================================");

            if (pruebaListado
                    && pruebaStock
                    && pruebaCriticos
                    && pruebaSql) {

                System.out.println(
                        "PRUEBA EXITOSA - T4.I.5"
                );

                System.out.println(
                        "Las pruebas generales de inventario pasaron."
                );

            } else {

                System.out.println(
                        "PRUEBA FALLIDA - T4.I.5"
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

    private static boolean probarListadoInventario(
            LibroDAO libroDAO) {

        System.out.println();
        System.out.println("----------------------------------------");
        System.out.println("PRUEBA 1 - CARGA DE INVENTARIO");
        System.out.println("----------------------------------------");

        List<Libro> libros =
                libroDAO.listarTodos();

        if (libros == null) {

            System.out.println(
                    "ERROR: el DAO devolvio null."
            );

            return false;
        }

        System.out.println(
                "Libros cargados: "
                + libros.size()
        );

        if (libros.isEmpty()) {

            System.out.println(
                    "ERROR: no se cargaron libros."
            );

            return false;
        }

        System.out.println(
                "LISTADO DE INVENTARIO CORRECTO"
        );

        return true;
    }

    private static boolean probarValoresStock(
            LibroDAO libroDAO) {

        System.out.println();
        System.out.println("----------------------------------------");
        System.out.println("PRUEBA 2 - VALORES DE STOCK");
        System.out.println("----------------------------------------");

        List<Libro> libros =
                libroDAO.listarTodos();

        boolean correcto = true;

        for (Libro libro : libros) {

            if (libro.getStockActual() < 0) {

                System.out.println(
                        "ERROR - Stock negativo: "
                        + libro.getTitulo()
                );

                correcto = false;
            }

            if (libro.getStockMinimo() < 0) {

                System.out.println(
                        "ERROR - Stock minimo negativo: "
                        + libro.getTitulo()
                );

                correcto = false;
            }

            System.out.println(
                    libro.getIsbn()
                    + " | "
                    + libro.getTitulo()
                    + " | Stock: "
                    + libro.getStockActual()
                    + " | Minimo: "
                    + libro.getStockMinimo()
            );
        }

        System.out.println(
                correcto
                        ? "VALORES DE STOCK CORRECTOS"
                        : "VALORES DE STOCK INCORRECTOS"
        );

        return correcto;
    }

    private static boolean probarStockCritico(
            LibroDAO libroDAO) {

        System.out.println();
        System.out.println("----------------------------------------");
        System.out.println("PRUEBA 3 - STOCK CRITICO");
        System.out.println("----------------------------------------");

        List<Libro> criticos =
                libroDAO.obtenerStockCritico();

        if (criticos == null) {

            System.out.println(
                    "ERROR: stock critico devolvio null."
            );

            return false;
        }

        boolean correcto = true;

        for (Libro libro : criticos) {

            boolean esCritico =
                    libro.getStockActual()
                    <= libro.getStockMinimo();

            System.out.println(
                    libro.getTitulo()
                    + " | Actual: "
                    + libro.getStockActual()
                    + " | Minimo: "
                    + libro.getStockMinimo()
                    + " | "
                    + (esCritico
                            ? "CORRECTO"
                            : "ERROR")
            );

            if (!esCritico) {
                correcto = false;
            }
        }

        if (criticos.isEmpty()) {

            System.out.println(
                    "No hay libros con stock critico actualmente."
            );
        }

        System.out.println(
                correcto
                        ? "STOCK CRITICO CORRECTO"
                        : "STOCK CRITICO INCORRECTO"
        );

        return correcto;
    }

    private static boolean compararCantidadContraSql(
            LibroDAO libroDAO) throws Exception {

        System.out.println();
        System.out.println("----------------------------------------");
        System.out.println("PRUEBA 4 - DAO CONTRA SQL");
        System.out.println("----------------------------------------");

        int cantidadDao =
                libroDAO.listarTodos().size();

        String sql =
                "SELECT COUNT(*) AS total FROM libros";

        int cantidadSql;

        try (
                Connection conexion =
                        Conexion.getInstancia().conectar();

                PreparedStatement ps =
                        conexion.prepareStatement(sql);

                ResultSet rs =
                        ps.executeQuery()
        ) {

            rs.next();

            cantidadSql =
                    rs.getInt("total");
        }

        System.out.println(
                "Cantidad DAO: "
                + cantidadDao
        );

        System.out.println(
                "Cantidad SQL: "
                + cantidadSql
        );

        boolean correcto =
                cantidadDao == cantidadSql;

        System.out.println(
                correcto
                        ? "DAO Y SQL COINCIDEN"
                        : "DAO Y SQL NO COINCIDEN"
        );

        return correcto;
    }
}