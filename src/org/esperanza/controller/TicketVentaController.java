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
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.esperanza.model.DetalleVenta;
import org.esperanza.service.NavegacionRol;

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

    private Runnable alVolver;

    @FXML
    public void initialize() {

        if (lblVenta != null) {
            lblVenta.setText("Factura No. --");
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
            lblDescuento.setText("Descuento aplicado: -Q0.00");
        }

        if (lblTotal != null) {
            lblTotal.setText("TOTAL: Q0.00");
        }
    }

    public void setAlVolver(Runnable accion) {
        this.alVolver = accion;
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
            lblVenta.setText("Factura No. " + idVenta);
        }

        if (lblFecha != null) {
            lblFecha.setText(
                    "Fecha: "
                    + (fecha == null
                            ? "--"
                            : fecha.format(FORMATO_FECHA))
            );
        }

        if (lblCliente != null) {
            lblCliente.setText(
                    "Cliente: " + cuiCliente
            );
        }

        if (lblCajero != null) {
            lblCajero.setText(
                    "Cajero: " + idUsuario
            );
        }

        if (lblSubtotal != null) {
            lblSubtotal.setText(
                    "Subtotal: Q" + dinero(subtotal)
            );
        }

        if (lblDescuento != null) {
            lblDescuento.setText(
                    "Descuento aplicado: -Q"
                    + dinero(descuento)
            );
        }

        if (lblTotal != null) {
            lblTotal.setText(
                    "TOTAL: Q" + dinero(total)
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
                    "Cliente: " + cui
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
                    "Cajero: " + idUsuario
            );

            return;
        }

        lblCajero.setText(
                "Cajero: " + nombre.trim()
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

            Label producto = celda(
                    titulo + "\n" + isbn,
                    180,
                    Pos.CENTER_LEFT
            );

            Label cantidad = celda(
                    String.valueOf(
                            detalle.getCantidad()
                    ),
                    50,
                    Pos.CENTER
            );

            Label precio = celda(
                    "Q"
                    + dinero(
                            detalle.getPrecioUnitario()
                    ),
                    80,
                    Pos.CENTER_RIGHT
            );

            Label subtotal = celda(
                    "Q"
                    + dinero(
                            detalle.getSubtotal()
                    ),
                    100,
                    Pos.CENTER_RIGHT
            );

            HBox fila =
                    new HBox(
                            5,
                            producto,
                            cantidad,
                            precio,
                            subtotal
                    );

            fila.setAlignment(
                    Pos.CENTER_LEFT
            );

            boxProductos
                    .getChildren()
                    .add(fila);
        }
    }

    private Label celda(
            String texto,
            double ancho,
            Pos alineacion) {

        Label label =
                new Label(
                        texto == null
                                ? ""
                                : texto
                );

        label.setMinWidth(ancho);
        label.setPrefWidth(ancho);
        label.setMaxWidth(ancho);

        label.setWrapText(true);
        label.setAlignment(alineacion);

        return label;
    }

    @FXML
    public void imprimir() {

        mostrarMensaje(
                Alert.AlertType.INFORMATION,
                "Factura impresa correctamente"
        );
    }

    @FXML
    public void volver() {

        Window window =
                obtenerVentana();

        if (!(window instanceof Stage ventana)) {
            return;
        }

        ventana.setOnCloseRequest(null);

        if (alVolver != null) {

            alVolver.run();
            return;
        }

        if (ventana.getOwner() != null) {

            ventana.close();
            return;
        }

        NavegacionRol
                .abrirDashboardSegunRol(
                        ventana
                );
    }

    private Window obtenerVentana() {

        if (contenidoTicket != null
                && contenidoTicket.getScene() != null) {

            return contenidoTicket
                    .getScene()
                    .getWindow();
        }

        if (lblVenta != null
                && lblVenta.getScene() != null) {

            return lblVenta
                    .getScene()
                    .getWindow();
        }

        return null;
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

        alert.setContentText(
                mensaje == null
                || mensaje.trim().isEmpty()
                        ? "Ocurrió un error."
                        : mensaje
        );

        Window ventana =
                obtenerVentana();

        if (ventana != null) {
            alert.initOwner(ventana);
        }

        alert.showAndWait();
    }

    private String dinero(
            BigDecimal valor) {

        return (valor == null
                ? BigDecimal.ZERO
                : valor)
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                )
                .toPlainString();
    }
}
