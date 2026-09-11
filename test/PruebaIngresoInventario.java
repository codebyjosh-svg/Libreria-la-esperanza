import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.esperanza.dao.MovimientoInventarioDAO;
import org.esperanza.util.Conexion;

public class PruebaIngresoInventario {

    public static void main(String[] args) {

        try {
            String isbn;
            int stockAntes;
            int idUsuario;

            try (Connection conexion =
                    Conexion.getInstancia().conectar()) {

                String sqlLibro = """
                        SELECT isbn, stock_actual
                        FROM libros
                        WHERE activo = 1
                        LIMIT 1
                        """;

                try (PreparedStatement ps =
                        conexion.prepareStatement(sqlLibro);
                     ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {
                        System.out.println(
                                "No hay libros activos para probar."
                        );
                        return;
                    }

                    isbn = rs.getString("isbn");
                    stockAntes = rs.getInt("stock_actual");
                }

                String sqlUsuario = """
                        SELECT id
                        FROM usuarios
                        WHERE activo = 1
                        LIMIT 1
                        """;

                try (PreparedStatement ps =
                        conexion.prepareStatement(sqlUsuario);
                     ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {
                        System.out.println(
                                "No hay usuarios activos para probar."
                        );
                        return;
                    }

                    idUsuario = rs.getInt("id");
                }
            }

            MovimientoInventarioDAO dao =
                    new MovimientoInventarioDAO();

            int cantidadPrueba = 2;

            boolean resultado =
                    dao.registrarIngresoInventario(
                            isbn,
                            idUsuario,
                            cantidadPrueba,
                            "Prueba T3.1.13"
                    );

            int stockDespues;

            try (Connection conexion =
                    Conexion.getInstancia().conectar()) {

                String sql = """
                        SELECT stock_actual
                        FROM libros
                        WHERE isbn = ?
                        """;

                try (PreparedStatement ps =
                        conexion.prepareStatement(sql)) {

                    ps.setString(1, isbn);

                    try (ResultSet rs = ps.executeQuery()) {

                        rs.next();

                        stockDespues =
                                rs.getInt("stock_actual");
                    }
                }
            }

            System.out.println("==============================");
            System.out.println("PRUEBA T3.1.13");
            System.out.println("==============================");
            System.out.println("ISBN: " + isbn);
            System.out.println("Stock antes: " + stockAntes);
            System.out.println(
                    "Cantidad ingresada: " + cantidadPrueba
            );
            System.out.println(
                    "Stock después: " + stockDespues
            );
            System.out.println(
                    "Movimiento registrado: " + resultado
            );

            if (resultado
                    && stockDespues
                    == stockAntes + cantidadPrueba) {

                System.out.println(
                        "PRUEBA EXITOSA - T3.1.13"
                );

            } else {

                System.out.println(
                        "PRUEBA FALLIDA - T3.1.13"
                );
            }

        } catch (Exception e) {

            System.out.println(
                    "ERROR EN LA PRUEBA: "
                    + e.getMessage()
            );

            e.printStackTrace();
        }
    }
}