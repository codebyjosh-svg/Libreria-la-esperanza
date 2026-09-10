package org.esperanza.controller;

import java.math.BigDecimal;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.esperanza.model.CarritoVenta;
import org.esperanza.model.DetalleVenta;

public class CarritoVentaController {

    @FXML private TextField txtIsbn;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtCantidad;
    @FXML private TextField txtNuevaCantidad;

    @FXML private TableView<DetalleVenta> tabla;
    @FXML private TableColumn<DetalleVenta, String> colIsbn;
    @FXML private TableColumn<DetalleVenta, Integer> colCantidad;
    @FXML private TableColumn<DetalleVenta, BigDecimal> colPrecio;
    @FXML private TableColumn<DetalleVenta, BigDecimal> colSubtotal;

    @FXML private Button btnActualizar;
    @FXML private Button btnEliminar;
    @FXML private Button btnVaciar;

    @FXML private Label lblTotal;
    @FXML private Label lblMensaje;

    private final CarritoVenta carrito = new CarritoVenta();

    @FXML
    public void initialize() {
        colIsbn.setCellValueFactory(c ->
                new ReadOnlyObjectWrapper<>(c.getValue().getIsbn()));

        colCantidad.setCellValueFactory(c ->
                new ReadOnlyObjectWrapper<>(c.getValue().getCantidad()));

        colPrecio.setCellValueFactory(c ->
                new ReadOnlyObjectWrapper<>(c.getValue().getPrecioUnitario()));

        colSubtotal.setCellValueFactory(c ->
                new ReadOnlyObjectWrapper<>(c.getValue().getSubtotal()));

        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        tabla.setPlaceholder(new Label("Agrega libros para iniciar la venta."));

        btnActualizar.disableProperty().bind(
                tabla.getSelectionModel().selectedItemProperty().isNull()
        );

        btnEliminar.disableProperty().bind(
                tabla.getSelectionModel().selectedItemProperty().isNull()
        );

        btnVaciar.disableProperty().bind(
                Bindings.isEmpty(tabla.getItems())
        );

        tabla.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, seleccionado) -> {
                    if (seleccionado != null) {
                        txtNuevaCantidad.setText(
                                String.valueOf(seleccionado.getCantidad())
                        );
                    }
                }
        );
    }

    @FXML
    private void agregarProducto() {
        ejecutar(() -> {
            String isbn = txtIsbn.getText().trim();

            if (isbn.isEmpty()) {
                throw new IllegalArgumentException("El ISBN es obligatorio");
            }

            int cantidad = enteroPositivo(
                    txtCantidad.getText(),
                    "La cantidad"
            );

            BigDecimal precio;

            try {
                precio = new BigDecimal(
                        txtPrecio.getText().trim().replace(',', '.')
                );
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Escribe un precio válido");
            }

            if (precio.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("El precio debe ser mayor que cero");
            }

            carrito.agregarProducto(isbn, cantidad, precio);
            refrescar(isbn);

            txtIsbn.clear();
            txtPrecio.clear();
            txtCantidad.setText("1");
            txtIsbn.requestFocus();
        }, "Libro agregado al carrito.");
    }

    @FXML
    private void actualizarCantidad() {
        ejecutar(() -> {
            DetalleVenta detalle = tabla.getSelectionModel().getSelectedItem();

            if (detalle == null) {
                throw new IllegalArgumentException("Selecciona un libro");
            }

            int cantidad = enteroPositivo(
                    txtNuevaCantidad.getText(),
                    "La cantidad"
            );

            carrito.cambiarCantidad(detalle.getIsbn(), cantidad);
            refrescar(detalle.getIsbn());
        }, "Cantidad actualizada.");
    }

    @FXML
    private void eliminarProducto() {
        ejecutar(() -> {
            DetalleVenta detalle = tabla.getSelectionModel().getSelectedItem();

            if (detalle == null) {
                throw new IllegalArgumentException("Selecciona un libro");
            }

            carrito.quitarProducto(detalle.getIsbn());
            refrescar(null);
        }, "Libro eliminado.");
    }

    @FXML
    private void vaciarCarrito() {
        ejecutar(() -> {
            carrito.vaciar();
            refrescar(null);
        }, "Carrito vacío.");
    }

    private void refrescar(String isbn) {
        tabla.getItems().setAll(carrito.getDetalles());
        lblTotal.setText("Total: Q" + carrito.getTotal().toPlainString());

        if (isbn == null) return;

        for (DetalleVenta detalle : tabla.getItems()) {
            if (detalle.getIsbn().equals(isbn)) {
                tabla.getSelectionModel().select(detalle);
                break;
            }
        }
    }

    private int enteroPositivo(String texto, String nombre) {
        try {
            int valor = Integer.parseInt(texto.trim());
            if (valor > 0) return valor;
        } catch (NumberFormatException ignored) {
        }

        throw new IllegalArgumentException(
                nombre + " debe ser un entero mayor que cero"
        );
    }

    private void ejecutar(Runnable accion, String confirmacion) {
        try {
            accion.run();
            lblMensaje.setStyle("-fx-text-fill: #166534;");
            lblMensaje.setText(confirmacion);
        } catch (IllegalArgumentException | ArithmeticException e) {
            lblMensaje.setStyle("-fx-text-fill: #b91c1c;");
            lblMensaje.setText(
                    e instanceof ArithmeticException
                            ? "Usa precios de hasta dos decimales."
                            : e.getMessage()
            );
        }
    }
}