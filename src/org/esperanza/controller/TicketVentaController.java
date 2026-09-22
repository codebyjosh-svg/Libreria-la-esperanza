package org.esperanza.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.esperanza.model.DetalleVenta;
import org.esperanza.service.NavegacionRol;

public class TicketVentaController {

    @FXML private Label lblVenta;
    @FXML private Label lblFecha;
    @FXML private Label lblCliente;
    @FXML private Label lblCajero;
    @FXML private Label lblSubtotal;
    @FXML private Label lblTotal;

    @FXML private VBox boxProductos;

    private Runnable alVolver;

    public void setAlVolver(Runnable accion) {
        alVolver = accion;
    }

    public void setDatosVenta(
            int id,
            LocalDateTime fecha,
            long cui,
            int usuario,
            BigDecimal subtotal,
            BigDecimal descuento,
            BigDecimal total) {

        lblVenta.setText("Factura No. " + id);

        lblFecha.setText(
                "Fecha: "
                + (fecha == null
                        ? "--"
                        : fecha.format(
                                DateTimeFormatter.ofPattern(
                                        "dd/MM/yyyy HH:mm"
                                )
                        ))
        );

        lblCliente.setText("Cliente: " + cui);
        lblCajero.setText("Cajero: " + usuario);

        lblSubtotal.setText("Subtotal: Q" + dinero(subtotal));
        lblTotal.setText("TOTAL: Q" + dinero(total));
    }

    public void setNombreCliente(String nombre, long cui) {
        lblCliente.setText(
                "Cliente: " + nombre + " (" + cui + ")"
        );
    }

    public void setNombreCajero(String nombre, int id) {
        lblCajero.setText("Cajero: " + nombre);
    }

    public void setDetalles(List<DetalleVenta> detalles) {
        setDetalles(detalles, Map.of());
    }

    public void setDetalles(
            List<DetalleVenta> detalles,
            Map<String, String> titulos) {

        boxProductos.getChildren().clear();

        for (DetalleVenta detalle : detalles) {
            String isbn = detalle.getIsbn();

            Label producto = celda(
                    titulos.getOrDefault(isbn, isbn)
                    + "\n" + isbn,
                    280,
                    Pos.CENTER_LEFT
            );

            Label cantidad = celda(
                    String.valueOf(detalle.getCantidad()),
                    50,
                    Pos.CENTER
            );

            Label precio = celda(
                    "Q" + dinero(detalle.getPrecioUnitario()),
                    100,
                    Pos.CENTER_RIGHT
            );

            Label subtotal = celda(
                    "Q" + dinero(detalle.getSubtotal()),
                    110,
                    Pos.CENTER_RIGHT
            );

            boxProductos.getChildren().add(
                    new HBox(
                            8,
                            producto,
                            cantidad,
                            precio,
                            subtotal
                    )
            );
        }
    }

    private Label celda(
            String texto,
            double ancho,
            Pos alineacion) {

        Label label = new Label(texto);

        label.setMinWidth(ancho);
        label.setPrefWidth(ancho);
        label.setMaxWidth(ancho);

        label.setWrapText(true);
        label.setAlignment(alineacion);

        return label;
    }

    private String dinero(BigDecimal valor) {
        return (valor == null ? BigDecimal.ZERO : valor)
                .setScale(2, RoundingMode.HALF_UP)
                .toPlainString();
    }

    @FXML
    public void imprimir() {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);

        alerta.initOwner(
                lblVenta.getScene().getWindow()
        );

        alerta.setTitle("Factura");
        alerta.setHeaderText(null);

        alerta.setContentText(
                "Factura impresa correctamente"
        );

        alerta.showAndWait();
    }

    @FXML
    public void volver() {
        Stage ventana = (Stage) lblVenta
                .getScene()
                .getWindow();

        ventana.setOnCloseRequest(null);

        if (alVolver != null) {
            alVolver.run();

        } else if (ventana.getOwner() != null) {
            ventana.close();

        } else {
            NavegacionRol.abrirDashboardSegunRol(ventana);
        }
    }
}