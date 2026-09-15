import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.esperanza.dao.MovimientoInventarioDAO;
import org.esperanza.util.Conexion;

public class PruebaIngresoInventario {

    public static void main(String[] args) {

        try {
            String isbn;
            int idUsuario;

            try (Connection conexion = Conexion.getInstancia().conectar()) {

                String sqlLibro = """
                        SELECT isbn
                        FROM libros
                        WHERE activo = 1
                        LIMIT 1
                        """;

                try (PreparedStatement ps = conexion.prepareStatement(sqlLibro);
                     ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {
                        System.out.println("No hay libros activos para probar.");
                        return;
                    }

                    isbn = rs.getString("isbn");
                }

                String sqlUsuario = """
                        SELECT id
                        FROM usuarios
                        WHERE activo = 1
                        LIMIT 1
                        """;

                try (PreparedStatement ps = conexion.prepareStatement(sqlUsuario);
                     ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {
                        System.out.println("No hay usuarios activos para probar.");
                        return;
                    }

                    idUsuario = rs.getInt("id");
                }
            }

            MovimientoInventarioDAO dao = new MovimientoInventarioDAO();

            probarCantidadValida(dao, isbn, idUsuario);
            probarCantidadInvalida(dao, isbn, idUsuario);

        } catch (Exception e) {
            System.out.println("ERROR EN LA PRUEBA: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void probarCantidadValida(
            MovimientoInventarioDAO dao,
            String isbn,
            int idUsuario) throws Exception {

        int stockAntes = obtenerStock(isbn);
        int cantidadPrueba = 2;

        boolean resultado = dao.registrarIngresoInventario(
                isbn,
                idUsuario,
                cantidadPrueba,
                "Prueba T3.1.13"
        );

        int stockDespues = obtenerStock(isbn);

        System.out.println("==============================");
        System.out.println("PRUEBA T3.1.13");
        System.out.println("==============================");
        System.out.println("ISBN: " + isbn);
        System.out.println("Stock antes: " + stockAntes);
        System.out.println("Cantidad ingresada: " + cantidadPrueba);
        System.out.println("Stock después: " + stockDespues);
        System.out.println("Movimiento registrado: " + resultado);

        if (resultado && stockDespues == stockAntes + cantidadPrueba) {
            System.out.println("PRUEBA EXITOSA - T3.1.13");
        } else {
            System.out.println("PRUEBA FALLIDA - T3.1.13");
        }
    }

    private static void probarCantidadInvalida(
            MovimientoInventarioDAO dao,
            String isbn,
            int idUsuario) throws Exception {

        int stockAntes = obtenerStock(isbn);
        int movimientosAntes = contarMovimientos(isbn);
        boolean cantidadRechazada = false;

        try {
            dao.registrarIngresoInventario(
                    isbn,
                    idUsuario,
                    0,
                    "Prueba T3.1.14"
            );
        } catch (IllegalArgumentException e) {
            cantidadRechazada = true;
            System.out.println();
            System.out.println("Cantidad rechazada correctamente: " + e.getMessage());
        }

        int stockDespues = obtenerStock(isbn);
        int movimientosDespues = contarMovimientos(isbn);

        System.out.println("==============================");
        System.out.println("PRUEBA T3.1.14");
        System.out.println("==============================");
        System.out.println("ISBN: " + isbn);
        System.out.println("Cantidad utilizada: 0");
        System.out.println("Stock antes: " + stockAntes);
        System.out.println("Stock después: " + stockDespues);
        System.out.println("Movimientos antes: " + movimientosAntes);
        System.out.println("Movimientos después: " + movimientosDespues);
        System.out.println("Cantidad rechazada: " + cantidadRechazada);

        if (cantidadRechazada
                && stockAntes == stockDespues
                && movimientosAntes == movimientosDespues) {

            System.out.println("PRUEBA EXITOSA - T3.1.14");
        } else {
            System.out.println("PRUEBA FALLIDA - T3.1.14");
        }
    }

    private static int obtenerStock(String isbn) throws Exception {

        try (Connection conexion = Conexion.getInstancia().conectar()) {

            String sql = """
                    SELECT stock_actual
                    FROM libros
                    WHERE isbn = ?
                    """;

            try (PreparedStatement ps = conexion.prepareStatement(sql)) {

                ps.setString(1, isbn);

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {
                        throw new IllegalStateException("No se encontró el libro.");
                    }

                    return rs.getInt("stock_actual");
                }
            }
        }
    }

    private static int contarMovimientos(String isbn) throws Exception {

        try (Connection conexion = Conexion.getInstancia().conectar()) {

            String sql = """
                    SELECT COUNT(*) AS total
                    FROM movimientos_inventario
                    WHERE isbn = ?
                    """;

            try (PreparedStatement ps = conexion.prepareStatement(sql)) {

                ps.setString(1, isbn);

                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    return rs.getInt("total");
                }
            }
        }
    }
}