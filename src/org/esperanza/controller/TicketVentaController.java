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
import javafx.print.PageLayout;
import javafx.print.PrinterJob;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;

import org.esperanza.model.DetalleVenta;

public class TicketVentaController {

    @FXML private Label lblVenta;
    @FXML private Label lblFecha;
    @FXML private Label lblCliente;
    @FXML private Label lblCajero;
    @FXML private Label lblSubtotal;
    @FXML private Label lblDescuento;
    @FXML private Label lblTotal;

    @FXML private VBox boxProductos;
    @FXML private VBox contenidoTicket;

    public void setDatosVenta(int idVenta, LocalDateTime fecha, long cuiCliente,
                              int idUsuario, BigDecimal subtotal,
                              BigDecimal descuento, BigDecimal total) {

        lblVenta.setText("Venta No. " + idVenta);
        lblFecha.setText("Fecha: " + fecha.format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        lblCliente.setText("Cliente: " + cuiCliente);
        lblCajero.setText("Cajero: " + idUsuario);
        lblSubtotal.setText("Subtotal: Q" + dinero(subtotal));
        lblDescuento.setText("Descuento: Q" + dinero(descuento));
        lblTotal.setText("TOTAL: Q" + dinero(total));
    }

    public void setNombreCliente(String nombre, long cui) {
        lblCliente.setText("Cliente: " + nombre + " (" + cui + ")");
    }

    public void setDetalles(List<DetalleVenta> detalles) {
        setDetalles(detalles, Collections.emptyMap());
    }

    public void setDetalles(List<DetalleVenta> detalles,
                            Map<String, String> titulos) {

        boxProductos.getChildren().clear();

        for (DetalleVenta detalle : detalles) {
            String titulo = titulos.getOrDefault(
                    detalle.getIsbn(),
                    detalle.getIsbn()
            );

            boxProductos.getChildren().add(
                    crearFila(
                            titulo,
                            detalle.getIsbn(),
                            detalle.getCantidad(),
                            detalle.getPrecioUnitario(),
                            detalle.getSubtotal()
                    )
            );
        }
    }

    private HBox crearFila(String titulo, String isbn, int cantidad,
                           BigDecimal precio, BigDecimal subtotal) {

        Label libro = new Label(titulo + "\n" + isbn);
        Label cant = new Label(String.valueOf(cantidad));
        Label precioLabel = new Label("Q" + dinero(precio));
        Label subtotalLabel = new Label("Q" + dinero(subtotal));

        libro.setPrefWidth(180);
        libro.setWrapText(true);
        cant.setPrefWidth(50);
        precioLabel.setPrefWidth(80);
        subtotalLabel.setPrefWidth(100);

        cant.setAlignment(Pos.CENTER);
        precioLabel.setAlignment(Pos.CENTER_RIGHT);
        subtotalLabel.setAlignment(Pos.CENTER_RIGHT);

        return new HBox(5, libro, cant, precioLabel, subtotalLabel);
    }

    @FXML
    private void imprimir() {
        PrinterJob job = PrinterJob.createPrinterJob();

        if (job == null) {
            mostrarMensaje(
                    Alert.AlertType.ERROR,
                    "No se encontró una impresora disponible."
            );
            return;
        }

        boolean continuar = job.showPrintDialog(
                contenidoTicket.getScene().getWindow()
        );

        if (!continuar) return;

        PageLayout pagina = job.getJobSettings().getPageLayout();

        double ancho = contenidoTicket.getBoundsInParent().getWidth();
        double alto = contenidoTicket.getBoundsInParent().getHeight();

        double escalaX = pagina.getPrintableWidth() / ancho;
        double escalaY = pagina.getPrintableHeight() / alto;
        double escala = Math.min(1, Math.min(escalaX, escalaY));

        Scale scale = new Scale(escala, escala);
        contenidoTicket.getTransforms().add(scale);

        boolean impreso = job.printPage(
                pagina,
                contenidoTicket
        );

        contenidoTicket.getTransforms().remove(scale);

        if (impreso) {
            job.endJob();

            mostrarMensaje(
                    Alert.AlertType.INFORMATION,
                    "Comprobante enviado a impresión."
            );
        } else {
            job.cancelJob();

            mostrarMensaje(
                    Alert.AlertType.ERROR,
                    "No se pudo imprimir el comprobante."
            );
        }
    }

    private void mostrarMensaje(Alert.AlertType tipo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle("Impresión");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private String dinero(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}