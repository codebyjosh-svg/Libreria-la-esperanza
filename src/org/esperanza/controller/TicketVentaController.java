package org.esperanza.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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

    public void setDetalles(List<DetalleVenta> detalles) {
        boxProductos.getChildren().clear();

        for (DetalleVenta detalle : detalles) {
            boxProductos.getChildren().add(
                    crearFila(
                            detalle.getIsbn(),
                            detalle.getCantidad(),
                            detalle.getPrecioUnitario(),
                            detalle.getSubtotal()
                    )
            );
        }
    }

    private HBox crearFila(String isbn, int cantidad,
                           BigDecimal precio, BigDecimal subtotal) {

        Label lblIsbn = new Label(isbn);
        Label lblCantidad = new Label(String.valueOf(cantidad));
        Label lblPrecio = new Label("Q" + dinero(precio));
        Label lblSub = new Label("Q" + dinero(subtotal));

        lblIsbn.setPrefWidth(160);
        lblCantidad.setPrefWidth(55);
        lblPrecio.setPrefWidth(90);
        lblSub.setPrefWidth(105);

        lblCantidad.setAlignment(Pos.CENTER);
        lblPrecio.setAlignment(Pos.CENTER_RIGHT);
        lblSub.setAlignment(Pos.CENTER_RIGHT);

        return new HBox(5, lblIsbn, lblCantidad, lblPrecio, lblSub);
    }

    private String dinero(BigDecimal valor) {
        return valor.setScale(2).toPlainString();
    }
}