package org.esperanza.test;

import java.sql.SQLException;
import org.esperanza.dao.StockDao;

public class PruebaStock {

    public static void main(String[] args) throws SQLException {

            StockDao stockDao = new StockDao();

            // CAMBIA ESTO POR UN ISBN REAL DE TU BASE
            String isbn = "978-0-124";

            int stock =
                    stockDao.obtenerStockActual(isbn);
            if (stock < 0) {
                throw new IllegalStateException("No se encontró el ISBN de prueba: " + isbn);
            }

            System.out.println("==============================");
            System.out.println("T2.17 - VALIDACION DE STOCK");
            System.out.println("==============================");

            System.out.println("ISBN: " + isbn);
            System.out.println("Stock actual: " + stock);

            System.out.println(
                    "Vender 1 unidad: "
                    + stockDao.hayStockSuficiente(
                            isbn,
                            1
                    )
            );

            if (stock >= 0) {

                System.out.println(
                        "Vender todo el stock: "
                        + stockDao.hayStockSuficiente(
                                isbn,
                                stock
                        )
                );

                System.out.println(
                        "Vender stock + 1: "
                        + stockDao.hayStockSuficiente(
                                isbn,
                                stock + 1
                        )
                );
            }

            System.out.println("==============================");

    }
}
