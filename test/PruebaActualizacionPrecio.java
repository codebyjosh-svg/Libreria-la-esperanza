import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.esperanza.dao.LibroDAO;
import org.esperanza.dao.impl.LibroDAOImpl;
import org.esperanza.util.Conexion;

public class PruebaActualizacionPrecio {

    public static void main(String[] args) {

        System.out.println("========================================");
        System.out.println("T4.4.15 - PRUEBA MODIFICACION DE PRECIO");
        System.out.println("========================================");

        LibroDAO libroDAO = new LibroDAOImpl();

        String isbn = null;
        String titulo = null;
        BigDecimal precioOriginal = null;

        try {

            // 1. Buscar un libro activo para la prueba
            String sqlBuscar = """
                    SELECT
                        isbn,
                        titulo,
                        precio
                    FROM libros
                    WHERE activo = TRUE
                    ORDER BY isbn
                    LIMIT 1
                    """;

            try (
                    Connection conexion =
                            Conexion.getInstancia().conectar();

                    PreparedStatement ps =
                            conexion.prepareStatement(sqlBuscar);

                    ResultSet rs =
                            ps.executeQuery()
            ) {

                if (!rs.next()) {

                    System.out.println(
                            "PRUEBA CANCELADA: no hay libros activos."
                    );

                    return;
                }

                isbn = rs.getString("isbn");
                titulo = rs.getString("titulo");
                precioOriginal = rs.getBigDecimal("precio");
            }

            BigDecimal precioPrueba =
                    precioOriginal.add(
                            new BigDecimal("1.00")
                    );

            System.out.println();
            System.out.println("Libro seleccionado:");
            System.out.println("ISBN: " + isbn);
            System.out.println("Titulo: " + titulo);
            System.out.println(
                    "Precio original: Q"
                    + precioOriginal
            );

            System.out.println(
                    "Precio de prueba: Q"
                    + precioPrueba
            );

            // 2. Actualizar mediante DAO
            boolean actualizado =
                    libroDAO.actualizarPrecio(
                            isbn,
                            precioPrueba
                    );

            if (!actualizado) {

                System.out.println();
                System.out.println(
                        "PRUEBA FALLIDA: "
                        + "el DAO no pudo actualizar el precio."
                );

                return;
            }

            // 3. Verificar directamente contra SQL
            BigDecimal precioGuardado =
                    consultarPrecio(isbn);

            System.out.println();
            System.out.println(
                    "Precio guardado en SQL: Q"
                    + precioGuardado
            );

            boolean modificacionCorrecta =
                    precioGuardado != null
                    && precioGuardado.compareTo(
                            precioPrueba
                    ) == 0;

            if (modificacionCorrecta) {

                System.out.println(
                        "MODIFICACION CORRECTA"
                );

            } else {

                System.out.println(
                        "MODIFICACION INCORRECTA"
                );
            }

            // 4. Restaurar precio original
            boolean restaurado =
                    libroDAO.actualizarPrecio(
                            isbn,
                            precioOriginal
                    );

            if (!restaurado) {

                System.out.println();
                System.out.println(
                        "ADVERTENCIA: "
                        + "no se pudo restaurar el precio original."
                );

                return;
            }

            // 5. Comprobar restauración
            BigDecimal precioRestaurado =
                    consultarPrecio(isbn);

            boolean restauracionCorrecta =
                    precioRestaurado != null
                    && precioRestaurado.compareTo(
                            precioOriginal
                    ) == 0;

            System.out.println();
            System.out.println(
                    "Precio restaurado: Q"
                    + precioRestaurado
            );

            if (modificacionCorrecta
                    && restauracionCorrecta) {

                System.out.println();
                System.out.println(
                        "========================================"
                );

                System.out.println(
                        "PRUEBA EXITOSA - T4.4.15"
                );

                System.out.println(
                        "El precio fue modificado, "
                        + "verificado y restaurado correctamente."
                );

                System.out.println(
                        "========================================"
                );

            } else {

                System.out.println();
                System.out.println(
                        "PRUEBA FALLIDA - T4.4.15"
                );
            }

        } catch (Exception ex) {

            System.out.println();
            System.out.println(
                    "ERROR DURANTE LA PRUEBA: "
                    + ex.getMessage()
            );

            // Intentar restaurar si ocurrió un error
            if (isbn != null
                    && precioOriginal != null) {

                libroDAO.actualizarPrecio(
                        isbn,
                        precioOriginal
                );
            }

            ex.printStackTrace();
        }
    }

    private static BigDecimal consultarPrecio(
            String isbn) throws Exception {

        String sql = """
                SELECT precio
                FROM libros
                WHERE isbn = ?
                """;

        try (
                Connection conexion =
                        Conexion.getInstancia().conectar();

                PreparedStatement ps =
                        conexion.prepareStatement(sql)
        ) {

            ps.setString(1, isbn);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getBigDecimal("precio");
                }
            }
        }

        return null;
    }
}