package org.esperanza.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import org.esperanza.model.DetalleVenta;

public class TicketVentaController {

    @FXML
    private Label lblVenta;

    @FXML
    private Label lblFecha;

    @FXML
    private Label lblCliente;

    @FXML
    private Label lblCajero;

    @FXML
    private Label lblSubtotal;

    @FXML
    private Label lblDescuento;

    @FXML
    private Label lblTotal;

    @FXML
    private VBox boxProductos;

    @FXML
    private VBox contenidoTicket;


    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {

        if (lblVenta != null) {
            lblVenta.setText("Venta No. --");
        }

        if (lblFecha != null) {
            lblFecha.setText("Fecha: --");
        }

        if (lblCliente != null) {
            lblCliente.setText("Cliente: --");
        }

        if (lblCajero != null) {
            lblCajero.setText("Cajero: --");
        }

        if (lblSubtotal != null) {
            lblSubtotal.setText("Subtotal: Q0.00");
        }

        if (lblDescuento != null) {
            lblDescuento.setText("Descuento: Q0.00");
        }

        if (lblTotal != null) {
            lblTotal.setText("TOTAL: Q0.00");
        }
    }

    public void setDatosVenta(
            int idVenta,
            LocalDateTime fecha,
            long cuiCliente,
            int idUsuario,
            BigDecimal subtotal,
            BigDecimal descuento,
            BigDecimal total) {

        if (lblVenta != null) {
            lblVenta.setText(
                    "Venta No. " + idVenta
            );
        }

        if (lblFecha != null) {

            if (fecha != null) {

                lblFecha.setText(
                        "Fecha: "
                        + fecha.format(FORMATO_FECHA)
                );

            } else {

                lblFecha.setText(
                        "Fecha: --"
                );
            }
        }

        if (lblCliente != null) {

            lblCliente.setText(
                    "Cliente: "
                    + cuiCliente
            );
        }

        if (lblCajero != null) {

            lblCajero.setText(
                    "Cajero: "
                    + idUsuario
            );
        }

        if (lblSubtotal != null) {

            lblSubtotal.setText(
                    "Subtotal: Q"
                    + dinero(subtotal)
            );
        }

        if (lblDescuento != null) {

            lblDescuento.setText(
                    "Descuento: Q"
                    + dinero(descuento)
            );
        }

        if (lblTotal != null) {

            lblTotal.setText(
                    "TOTAL: Q"
                    + dinero(total)
            );
        }
    }

    public void setNombreCliente(
            String nombre,
            long cui) {

        if (lblCliente == null) {
            return;
        }

        if (nombre == null
                || nombre.trim().isEmpty()) {

            lblCliente.setText(
                    "Cliente: "
                    + cui
            );

            return;
        }

        lblCliente.setText(
                "Cliente: "
                + nombre.trim()
                + " ("
                + cui
                + ")"
        );
    }

    public void setNombreCajero(
            String nombre,
            int idUsuario) {

        if (lblCajero == null) {
            return;
        }

        if (nombre == null
                || nombre.trim().isEmpty()) {

            lblCajero.setText(
                    "Cajero: "
                    + idUsuario
            );

            return;
        }

        lblCajero.setText(
                "Cajero: "
                + nombre.trim()
                + " (ID "
                + idUsuario
                + ")"
        );
    }

    public void setDetalles(
            List<DetalleVenta> detalles) {

        setDetalles(
                detalles,
                Collections.emptyMap()
        );
    }

    public void setDetalles(
            List<DetalleVenta> detalles,
            Map<String, String> titulos) {

        if (boxProductos == null) {
            return;
        }

        boxProductos
                .getChildren()
                .clear();

        if (detalles == null
                || detalles.isEmpty()) {

            Label mensaje =
                    new Label(
                            "No hay productos en esta venta."
                    );

            boxProductos
                    .getChildren()
                    .add(mensaje);

            return;
        }

        Map<String, String> mapaTitulos =
                titulos == null
                        ? Collections.emptyMap()
                        : titulos;

        for (DetalleVenta detalle : detalles) {

            if (detalle == null) {
                continue;
            }

            String isbn =
                    detalle.getIsbn();

            if (isbn == null) {
                isbn = "";
            }

            String titulo =
                    mapaTitulos.getOrDefault(
                            isbn,
                            isbn
                    );

            if (titulo == null
                    || titulo.trim().isEmpty()) {

                titulo = isbn;
            }

            HBox fila =
                    crearFila(
                            titulo,
                            isbn,
                            detalle.getCantidad(),
                            detalle.getPrecioUnitario(),
                            detalle.getSubtotal()
                    );

            boxProductos
                    .getChildren()
                    .add(fila);
        }
    }

    private HBox crearFila(
            String titulo,
            String isbn,
            int cantidad,
            BigDecimal precio,
            BigDecimal subtotal) {

        if (titulo == null) {
            titulo = "";
        }

        if (isbn == null) {
            isbn = "";
        }

        Label libro =
                new Label(
                        titulo
                        + "\n"
                        + isbn
                );

        Label cant =
                new Label(
                        String.valueOf(cantidad)
                );

        Label precioLabel =
                new Label(
                        "Q"
                        + dinero(precio)
                );

        Label subtotalLabel =
                new Label(
                        "Q"
                        + dinero(subtotal)
                );


        libro.setPrefWidth(180);
        libro.setMinWidth(180);
        libro.setWrapText(true);

        cant.setPrefWidth(50);
        cant.setMinWidth(50);

        precioLabel.setPrefWidth(80);
        precioLabel.setMinWidth(80);

        subtotalLabel.setPrefWidth(100);
        subtotalLabel.setMinWidth(100);


        cant.setAlignment(
                Pos.CENTER
        );

        precioLabel.setAlignment(
                Pos.CENTER_RIGHT
        );

        subtotalLabel.setAlignment(
                Pos.CENTER_RIGHT
        );

        HBox fila =
                new HBox(
                        5,
                        libro,
                        cant,
                        precioLabel,
                        subtotalLabel
                );

        fila.setAlignment(
                Pos.CENTER_LEFT
        );

        return fila;
    }

    @FXML
    private void imprimir() {

        if (contenidoTicket == null) {

            mostrarMensaje(
                    Alert.AlertType.ERROR,
                    "No se encontró el contenido del comprobante."
            );

            return;
        }

        PrinterJob printerJob =
                PrinterJob.createPrinterJob();

        if (printerJob == null) {

            mostrarMensaje(
                    Alert.AlertType.ERROR,
                    "No se encontró ninguna impresora disponible."
            );

            return;
        }

        Window ventana =
                obtenerVentana();

        boolean continuar =
                printerJob.showPrintDialog(
                        ventana
                );

        if (!continuar) {
            return;
        }

        try {

            boolean impreso =
                    printerJob.printPage(
                            contenidoTicket
                    );

            if (impreso) {

                printerJob.endJob();

                mostrarMensaje(
                        Alert.AlertType.INFORMATION,
                        "Comprobante impreso correctamente."
                );

            } else {

                mostrarMensaje(
                        Alert.AlertType.ERROR,
                        "No fue posible imprimir el comprobante."
                );
            }

        } catch (Exception e) {

            mostrarMensaje(
                    Alert.AlertType.ERROR,
                    "Ocurrió un error al imprimir el comprobante."
            );

            e.printStackTrace();
        }
    }

    private Window obtenerVentana() {

        if (contenidoTicket == null) {
            return null;
        }

        if (contenidoTicket.getScene() == null) {
            return null;
        }

        return contenidoTicket
                .getScene()
                .getWindow();
    }

    private void mostrarMensaje(
            Alert.AlertType tipo,
            String mensaje) {

        Alert alert =
                new Alert(tipo);

        alert.setTitle(
                "Comprobante de venta"
        );

        alert.setHeaderText(null);

        if (mensaje == null
                || mensaje.trim().isEmpty()) {

            alert.setContentText(
                    "Ocurrió un error."
            );

        } else {

            alert.setContentText(
                    mensaje
            );
        }

        Window ventana =
                obtenerVentana();

        if (ventana != null) {

            alert.initOwner(
                    ventana
            );
        }

        alert.showAndWait();
    }

    private String dinero(
            BigDecimal valor) {

        if (valor == null) {

            valor =
                    BigDecimal.ZERO;
        }

        return valor
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                )
                .toPlainString();
    }
}