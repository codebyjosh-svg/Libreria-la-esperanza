package org.esperanza.view;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.esperanza.controller.TicketVentaController;
import org.esperanza.dao.CajeroDao;
import org.esperanza.dao.DatosVentaDao;
import org.esperanza.model.DetalleVenta;
import org.esperanza.model.Venta;
import org.esperanza.service.SesionUsuario;

public final class ComprobanteVenta {

    private ComprobanteVenta() {
    }

    public static TicketVentaController mostrar(
            Venta venta,
            List<DetalleVenta> detalles,
            Stage ventana) throws IOException, SQLException {

        SesionUsuario sesion = SesionUsuario.getInstancia();

        if (!sesion.esAdmin()
                && !(sesion.esCajero()
                && sesion.getUsuarioActual().getId()
                        == venta.getIdUsuario())) {

            throw new SecurityException(
                    "No puedes abrir esta factura."
            );
        }

        DatosVentaDao datos = new DatosVentaDao();

        String cliente = datos.obtenerNombreCliente(
                venta.getCuiCliente()
        );

        String cajero = new CajeroDao().nombreCajero(
                venta.getIdUsuario()
        );

        var titulos = datos.obtenerTitulos(detalles);

        FXMLLoader loader = new FXMLLoader(
                ComprobanteVenta.class.getResource(
                        "/org/esperanza/view/TicketVenta.fxml"
                )
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

        controller.setNombreCliente(
                cliente,
                venta.getCuiCliente()
        );

        controller.setNombreCajero(
                cajero,
                venta.getIdUsuario()
        );

        controller.setDetalles(detalles, titulos);

        ventana.setScene(new Scene(root));

        ventana.setTitle(
                "Factura #" + venta.getIdVenta()
        );

        ventana.setOnCloseRequest(event -> {
            event.consume();
            controller.volver();
        });

        ventana.sizeToScene();
        ventana.centerOnScreen();

        return controller;
    }
}