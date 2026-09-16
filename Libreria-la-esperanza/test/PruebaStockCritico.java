import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import org.esperanza.dao.impl.LibroDAOImpl;
import org.esperanza.model.Libro;

/** Prueba aislada: no modifica la base de datos de la librería. */
public class PruebaStockCritico {
    public static void main(String[] args) throws Exception {
        Class.forName("org.h2.Driver");
        String url = "jdbc:h2:mem:stock_critico;MODE=MySQL;DB_CLOSE_DELAY=-1";
        try (Connection con = DriverManager.getConnection(url);
             Statement st = con.createStatement()) {
            st.execute("CREATE TABLE libros (isbn VARCHAR(20) PRIMARY KEY, titulo VARCHAR(100), "
                    + "stock_actual INT, stock_minimo INT, activo BOOLEAN)");
            st.execute("INSERT INTO libros VALUES ('NORMAL','Normal',10,5,TRUE),"
                    + "('IGUAL','Igual',5,5,TRUE),('INFERIOR','Inferior',2,5,TRUE),"
                    + "('AGOTADO','Agotado',0,5,TRUE),('INACTIVO','Inactivo',1,5,FALSE)");
        }
        LibroDAOImpl dao = new LibroDAOImpl(() -> DriverManager.getConnection(url));
        List<Libro> libros = dao.obtenerStockCritico();
        comprobar(!contiene(libros, "NORMAL"), "T3.3.7: stock normal excluido");
        comprobar(contiene(libros, "IGUAL"), "T3.3.8: stock igual al mínimo incluido");
        comprobar(contiene(libros, "INFERIOR"), "T3.3.9: stock inferior incluido");
        comprobar(contiene(libros, "AGOTADO"), "Libro agotado incluido");
        comprobar(!contiene(libros, "INACTIVO"), "Libro inactivo excluido");
        comprobar(libros.size() == 3, "Cantidad: 3 libros críticos");
        comprobar(libros.get(0).getIsbn().equals("AGOTADO"), "Orden por menor stock");
        comprobar(libros.get(2).getStockActual() == 5 && libros.get(2).getStockMinimo() == 5,
                "DAO conserva los valores del stock");
        try (Connection con = DriverManager.getConnection(url); Statement st = con.createStatement()) {
            st.executeUpdate("UPDATE libros SET stock_actual = 10");
        }
        comprobar(dao.obtenerStockCritico().isEmpty(), "Sin libros críticos después de reabastecer");
    }
    private static boolean contiene(List<Libro> libros, String isbn) {
        return libros.stream().anyMatch(libro -> isbn.equals(libro.getIsbn()));
    }
    private static void comprobar(boolean condicion, String nombre) {
        if (!condicion) throw new AssertionError(nombre);
        System.out.println("OK: " + nombre);
    }
}
