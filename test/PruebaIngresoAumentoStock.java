import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.esperanza.dao.MovimientoInventarioDAO;
import org.esperanza.util.Conexion;

public class PruebaIngresoAumentoStock {

    public static void main(String[] args) {

        System.out.println("========================================");
        System.out.println("T4.I.8 - PRUEBA INGRESO Y AUMENTO STOCK");
        System.out.println("========================================");

        String isbn = null;
        int idUsuario = 0;
        int stockAntes = 0;

        int cantidadPrueba = 2;

        String observacion =
                "PRUEBA_T4_I_8_INGRESO_STOCK";

        try {

            // 1. Buscar libro activo
            isbn = obtenerIsbnActivo();

            // 2. Buscar usuario activo
            idUsuario = obtenerUsuarioActivo();

            // 3. Leer stock antes
            stockAntes = obtenerStock(isbn);

            System.out.println();
            System.out.println("ISBN: " + isbn);
            System.out.println("Stock antes: " + stockAntes);
            System.out.println(
                    "Cantidad a ingresar: "
                    + cantidadPrueba
            );

            // 4. Ejecutar ingreso real mediante DAO
            MovimientoInventarioDAO dao =
                    new MovimientoInventarioDAO();

            boolean registrado =
                    dao.registrarIngresoInventario(
                            isbn,
                            idUsuario,
                            cantidadPrueba,
                            observacion
                    );

            // 5. Leer stock después
            int stockDespues =
                    obtenerStock(isbn);

            System.out.println(
                    "Movimiento registrado: "
                    + registrado
            );

            System.out.println(
                    "Stock despues: "
                    + stockDespues
            );

            int stockEsperado =
                    stockAntes + cantidadPrueba;

            System.out.println(
                    "Stock esperado: "
                    + stockEsperado
            );

            boolean aumentoCorrecto =
                    registrado
                    && stockDespues
                    == stockEsperado;

            System.out.println();

            if (aumentoCorrecto) {

                System.out.println(
                        "AUMENTO DE STOCK CORRECTO"
                );

            } else {

                System.out.println(
                        "AUMENTO DE STOCK INCORRECTO"
                );
            }

            // 6. Restaurar la base después de la prueba
            restaurarDatos(
                    isbn,
                    stockAntes,
                    observacion
            );

            int stockRestaurado =
                    obtenerStock(isbn);

            System.out.println();
            System.out.println(
                    "Stock restaurado: "
                    + stockRestaurado
            );

            boolean restauracionCorrecta =
                    stockRestaurado == stockAntes;

            if (aumentoCorrecto
                    && restauracionCorrecta) {

                System.out.println();
                System.out.println(
                        "========================================"
                );

                System.out.println(
                        "PRUEBA EXITOSA - T4.I.8"
                );

                System.out.println(
                        "El ingreso aumento el stock "
                        + "y los datos fueron restaurados."
                );

                System.out.println(
                        "========================================"
                );

            } else {

                System.out.println();
                System.out.println(
                        "PRUEBA FALLIDA - T4.I.8"
                );
            }

        } catch (Exception ex) {

            System.out.println();
            System.out.println(
                    "ERROR DURANTE LA PRUEBA: "
                    + ex.getMessage()
            );

            // Intento de restauración en caso de error
            if (isbn != null) {

                try {

                    restaurarDatos(
                            isbn,
                            stockAntes,
                            observacion
                    );

                } catch (Exception restauracionEx) {

                    System.out.println(
                            "No se pudo restaurar "
                            + "automáticamente la prueba."
                    );
                }
            }

            ex.printStackTrace();
        }
    }

    private static String obtenerIsbnActivo()
            throws Exception {

        String sql = """
                SELECT isbn
                FROM libros
                WHERE activo = TRUE
                ORDER BY isbn
                LIMIT 1
                """;

        try (
                Connection conexion =
                        Conexion.getInstancia().conectar();

                PreparedStatement ps =
                        conexion.prepareStatement(sql);

                ResultSet rs =
                        ps.executeQuery()
        ) {

            if (!rs.next()) {

                throw new IllegalStateException(
                        "No hay libros activos."
                );
            }

            return rs.getString("isbn");
        }
    }

    private static int obtenerUsuarioActivo()
            throws Exception {

        String sql = """
                SELECT id
                FROM usuarios
                WHERE activo = TRUE
                ORDER BY id
                LIMIT 1
                """;

        try (
                Connection conexion =
                        Conexion.getInstancia().conectar();

                PreparedStatement ps =
                        conexion.prepareStatement(sql);

                ResultSet rs =
                        ps.executeQuery()
        ) {

            if (!rs.next()) {

                throw new IllegalStateException(
                        "No hay usuarios activos."
                );
            }

            return rs.getInt("id");
        }
    }

    private static int obtenerStock(
            String isbn) throws Exception {

        String sql = """
                SELECT stock_actual
                FROM libros
                WHERE isbn = ?
                """;

        try (
                Connection conexion =
                        Conexion.getInstancia().conectar();

                PreparedStatement ps =
                        conexion.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    isbn
            );

            try (ResultSet rs =
                    ps.executeQuery()) {

                if (!rs.next()) {

                    throw new IllegalStateException(
                            "No se encontro el libro."
                    );
                }

                return rs.getInt(
                        "stock_actual"
                );
            }
        }
    }

    private static void restaurarDatos(
            String isbn,
            int stockOriginal,
            String observacion)
            throws Exception {

        try (
                Connection conexion =
                        Conexion.getInstancia().conectar()
        ) {

            boolean autoCommitOriginal =
                    conexion.getAutoCommit();

            conexion.setAutoCommit(false);

            try {

                String sqlStock = """
                        UPDATE libros
                        SET stock_actual = ?
                        WHERE isbn = ?
                        """;

                try (
                        PreparedStatement ps =
                                conexion.prepareStatement(
                                        sqlStock
                                )
                ) {

                    ps.setInt(
                            1,
                            stockOriginal
                    );

                    ps.setString(
                            2,
                            isbn
                    );

                    ps.executeUpdate();
                }

                String sqlMovimiento = """
                        DELETE FROM movimientos_inventario
                        WHERE isbn = ?
                          AND observacion = ?
                          AND tipo_movimiento = 'INGRESO'
                        """;

                try (
                        PreparedStatement ps =
                                conexion.prepareStatement(
                                        sqlMovimiento
                                )
                ) {

                    ps.setString(
                            1,
                            isbn
                    );

                    ps.setString(
                            2,
                            observacion
                    );

                    ps.executeUpdate();
                }

                conexion.commit();

            } catch (Exception ex) {

                conexion.rollback();
                throw ex;

            } finally {

                conexion.setAutoCommit(
                        autoCommitOriginal
                );
            }
        }
    }
}