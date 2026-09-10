package org.esperanza.view;

import java.io.IOException;
import java.util.List;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.esperanza.controller.TicketVentaController;
import org.esperanza.model.DetalleVenta;
import org.esperanza.model.Venta;

public class ComprobanteVenta {

    public static void mostrar(
            Venta venta,
            List<DetalleVenta> detalles) throws IOException {

        FXMLLoader loader = new FXMLLoader(
                ComprobanteVenta.class.getResource(
                        "/org/esperanza/view/TicketVenta.fxml"
                )
        );

        Parent root = loader.load();

        TicketVentaController controller =
                loader.getController();

        controller.setDatosVenta(
                venta.getIdVenta(),
                venta.getFechaVenta(),
                venta.getCuiCliente(),
                venta.getIdUsuario(),
                venta.getSubtotal(),
                venta.getDescuento(),
                venta.getTotal()
        );

        controller.setDetalles(detalles);

        Stage stage = new Stage();

        stage.setTitle(
                "Comprobante de venta #" + venta.getIdVenta()
        );

        stage.setScene(
                new Scene(root)
        );

        stage.setResizable(false);
        stage.show();
    }

    private ComprobanteVenta() {
    }
}