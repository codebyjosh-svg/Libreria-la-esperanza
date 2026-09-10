package org.esperanza.view;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.esperanza.controller.TicketVentaController;
import org.esperanza.dao.DatosVentaDao;
import org.esperanza.model.DetalleVenta;
import org.esperanza.model.Venta;

public class ComprobanteVenta {

    public static void mostrar(Venta venta, List<DetalleVenta> detalles)
            throws IOException, SQLException {

        DatosVentaDao datosDao = new DatosVentaDao();
        String cliente = datosDao.obtenerNombreCliente(venta.getCuiCliente());
        Map<String, String> titulos = datosDao.obtenerTitulos(detalles);

        FXMLLoader loader = new FXMLLoader(
                ComprobanteVenta.class.getResource("/org/esperanza/view/TicketVenta.fxml")
        );

        Parent root = loader.load();
        TicketVentaController controller = loader.getController();

        controller.setDatosVenta(
                venta.getIdVenta(),
                venta.getFechaVenta(),
                venta.getCuiCliente(),
                venta.getIdUsuario(),
                venta.getSubtotal(),
                venta.getDescuento(),
                venta.getTotal()
        );

        controller.setNombreCliente(cliente, venta.getCuiCliente());
        controller.setDetalles(detalles, titulos);

        Stage stage = new Stage();
        stage.setTitle("Comprobante de venta #" + venta.getIdVenta());
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.show();
    }

    private ComprobanteVenta() {
    }
}