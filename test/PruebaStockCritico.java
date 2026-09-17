import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.esperanza.dao.impl.LibroDAOImpl;
import org.esperanza.model.Libro;

public class PruebaStockCritico {

    private static final List<FilaLibro> DATOS =
            new ArrayList<>();

    public static void main(String[] args) {

        DATOS.add(
                new FilaLibro(
                        "NORMAL",
                        "Normal",
                        10,
                        5,
                        true
                )
        );

        DATOS.add(
                new FilaLibro(
                        "IGUAL",
                        "Igual",
                        5,
                        5,
                        true
                )
        );

        DATOS.add(
                new FilaLibro(
                        "INFERIOR",
                        "Inferior",
                        2,
                        5,
                        true
                )
        );

        DATOS.add(
                new FilaLibro(
                        "AGOTADO",
                        "Agotado",
                        0,
                        5,
                        true
                )
        );

        DATOS.add(
                new FilaLibro(
                        "INACTIVO",
                        "Inactivo",
                        1,
                        5,
                        false
                )
        );

        LibroDAOImpl dao =
                new LibroDAOImpl(
                        PruebaStockCritico::crearConexion
                );

        List<Libro> libros =
                dao.obtenerStockCritico();

        comprobar(
                !contiene(libros, "NORMAL"),
                "T3.3.7: stock normal excluido"
        );

        comprobar(
                contiene(libros, "IGUAL"),
                "T3.3.8: stock igual al mínimo incluido"
        );

        comprobar(
                contiene(libros, "INFERIOR"),
                "T3.3.9: stock inferior incluido"
        );

        comprobar(
                contiene(libros, "AGOTADO"),
                "Libro agotado incluido"
        );

        comprobar(
                !contiene(libros, "INACTIVO"),
                "Libro inactivo excluido"
        );

        comprobar(
                libros.size() == 3,
                "Cantidad: 3 libros críticos"
        );

        comprobar(
                libros.get(0)
                        .getIsbn()
                        .equals("AGOTADO"),
                "Orden por menor stock"
        );

        comprobar(
                libros.get(2).getStockActual() == 5
                && libros.get(2).getStockMinimo() == 5,
                "DAO conserva los valores del stock"
        );

        for (FilaLibro fila : DATOS) {
            fila.stockActual = 10;
        }

        comprobar(
                dao.obtenerStockCritico().isEmpty(),
                "Sin libros críticos después de reabastecer"
        );

        System.out.println();
        System.out.println(
                "TODAS LAS PRUEBAS DE STOCK CRITICO PASARON"
        );
    }

    private static Connection crearConexion() {

        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, metodo, argumentos) -> {

                    switch (metodo.getName()) {

                        case "prepareStatement":
                            return crearPreparedStatement();

                        case "close":
                            return null;

                        case "isClosed":
                            return false;

                        default:
                            throw new UnsupportedOperationException(
                                    "Método Connection no simulado: "
                                    + metodo.getName()
                            );
                    }
                }
        );
    }

    private static PreparedStatement crearPreparedStatement() {

        return (PreparedStatement) Proxy.newProxyInstance(
                PreparedStatement.class.getClassLoader(),
                new Class<?>[]{PreparedStatement.class},
                (proxy, metodo, argumentos) -> {

                    switch (metodo.getName()) {

                        case "executeQuery":
                            return crearResultSet();

                        case "close":
                            return null;

                        default:
                            throw new UnsupportedOperationException(
                                    "Método PreparedStatement no simulado: "
                                    + metodo.getName()
                            );
                    }
                }
        );
    }

    private static ResultSet crearResultSet() {

        List<FilaLibro> criticos =
                DATOS.stream()
                        .filter(
                                libro ->
                                        libro.activo
                                        && libro.stockActual
                                        <= libro.stockMinimo
                        )
                       .sorted(
        Comparator
                .comparingInt(
                        (FilaLibro libro) ->
                                libro.stockActual
                )
                .thenComparing(
                        (FilaLibro libro) ->
                                libro.titulo
                )
)
                        .toList();

        int[] posicion = {-1};

        return (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class<?>[]{ResultSet.class},
                (proxy, metodo, argumentos) -> {

                    switch (metodo.getName()) {

                        case "next":

                            posicion[0]++;

                            return posicion[0]
                                    < criticos.size();

                        case "getString":

                            FilaLibro filaTexto =
                                    criticos.get(
                                            posicion[0]
                                    );

                            String columnaTexto =
                                    (String) argumentos[0];

                            if ("isbn".equals(columnaTexto)) {
                                return filaTexto.isbn;
                            }

                            if ("titulo".equals(columnaTexto)) {
                                return filaTexto.titulo;
                            }

                            return null;

                        case "getInt":

                            FilaLibro filaNumero =
                                    criticos.get(
                                            posicion[0]
                                    );

                            String columnaNumero =
                                    (String) argumentos[0];

                            if ("stock_actual"
                                    .equals(columnaNumero)) {

                                return filaNumero.stockActual;
                            }

                            if ("stock_minimo"
                                    .equals(columnaNumero)) {

                                return filaNumero.stockMinimo;
                            }

                            return 0;

                        case "close":
                            return null;

                        default:
                            throw new UnsupportedOperationException(
                                    "Método ResultSet no simulado: "
                                    + metodo.getName()
                            );
                    }
                }
        );
    }

    private static boolean contiene(
            List<Libro> libros,
            String isbn) {

        return libros.stream()
                .anyMatch(
                        libro ->
                                isbn.equals(
                                        libro.getIsbn()
                                )
                );
    }

    private static void comprobar(
            boolean condicion,
            String nombre) {

        if (!condicion) {
            throw new AssertionError(nombre);
        }

        System.out.println(
                "OK: " + nombre
        );
    }

    private static class FilaLibro {

        private final String isbn;
        private final String titulo;
        private int stockActual;
        private final int stockMinimo;
        private final boolean activo;

        private FilaLibro(
                String isbn,
                String titulo,
                int stockActual,
                int stockMinimo,
                boolean activo) {

            this.isbn = isbn;
            this.titulo = titulo;
            this.stockActual = stockActual;
            this.stockMinimo = stockMinimo;
            this.activo = activo;
        }
    }
}