package org.esperanza.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import org.esperanza.Service.NavegacionRol;
import org.esperanza.dao.VentaDao;
import org.esperanza.model.CarritoVenta;
import org.esperanza.model.DetalleVenta;
import org.esperanza.model.Venta;
import org.esperanza.view.ComprobanteVenta;

public class CarritoVentaController {

    @FXML private TextField txtIsbn;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtCantidad;
    @FXML private TextField txtNuevaCantidad;
    @FXML private TextField txtCuiCliente;

    @FXML private TableView<DetalleVenta> tabla;
    @FXML private TableColumn<DetalleVenta, String> colIsbn;
    @FXML private TableColumn<DetalleVenta, Integer> colCantidad;
    @FXML private TableColumn<DetalleVenta, BigDecimal> colPrecio;
    @FXML private TableColumn<DetalleVenta, BigDecimal> colSubtotal;

    @FXML private Button btnActualizar;
    @FXML private Button btnEliminar;
    @FXML private Button btnVaciar;
    @FXML private Button btnConfirmar;

    @FXML private Label lblTotal;
    @FXML private Label lblMensaje;

    private final CarritoVenta carrito = new CarritoVenta();
    private final VentaDao ventaDao = new VentaDao();

    private int idUsuario = 5;

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

        tabla.setPlaceholder(
                new Label("Agrega libros para iniciar la venta.")
        );

        btnActualizar.disableProperty().bind(
                tabla.getSelectionModel().selectedItemProperty().isNull()
        );

        btnEliminar.disableProperty().bind(
                tabla.getSelectionModel().selectedItemProperty().isNull()
        );

        btnVaciar.disableProperty().bind(
                Bindings.isEmpty(tabla.getItems())
        );

        btnConfirmar.disableProperty().bind(
                Bindings.isEmpty(tabla.getItems())
        );

        tabla.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, anterior, seleccionado) -> {
                    if (seleccionado != null) {
                        txtNuevaCantidad.setText(
                                String.valueOf(seleccionado.getCantidad())
                        );
                    }
                });

        txtCantidad.setText("1");
        lblTotal.setText("Total: Q0.00");

        configurarCierre();
    }

    private void configurarCierre() {
        Platform.runLater(() -> {
            Stage stage = (Stage) tabla
                    .getScene()
                    .getWindow();

            stage.setOnCloseRequest(event -> {
                event.consume();
                regresarDashboard(stage);
            });
        });
    }

    private void regresarDashboard(Stage stage) {
        stage.setOnCloseRequest(null);

        NavegacionRol.abrirDashboardSegunRol(
                stage
        );
    }

    @FXML
    private void agregarProducto() {
        ejecutar(() -> {
            String isbn = txtIsbn.getText().trim();

            if (isbn.isEmpty()) {
                throw new IllegalArgumentException(
                        "El ISBN es obligatorio"
                );
            }

            int cantidad = enteroPositivo(
                    txtCantidad.getText(),
                    "La cantidad"
            );

            BigDecimal precio;

            try {
                precio = new BigDecimal(
                        txtPrecio.getText()
                                .trim()
                                .replace(',', '.')
                );
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Escribe un precio válido"
                );
            }

            if (precio.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(
                        "El precio debe ser mayor que cero"
                );
            }

            carrito.agregarProducto(
                    isbn,
                    cantidad,
                    precio
            );

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
            DetalleVenta detalle = tabla
                    .getSelectionModel()
                    .getSelectedItem();

            if (detalle == null) {
                throw new IllegalArgumentException(
                        "Selecciona un libro"
                );
            }

            int cantidad = enteroPositivo(
                    txtNuevaCantidad.getText(),
                    "La cantidad"
            );

            carrito.cambiarCantidad(
                    detalle.getIsbn(),
                    cantidad
            );

            refrescar(detalle.getIsbn());

        }, "Cantidad actualizada.");
    }

    @FXML
    private void eliminarProducto() {
        ejecutar(() -> {
            DetalleVenta detalle = tabla
                    .getSelectionModel()
                    .getSelectedItem();

            if (detalle == null) {
                throw new IllegalArgumentException(
                        "Selecciona un libro"
                );
            }

            carrito.quitarProducto(
                    detalle.getIsbn()
            );

            refrescar(null);

        }, "Libro eliminado.");
    }

    @FXML
    private void vaciarCarrito() {
        ejecutar(() -> {
            carrito.vaciar();
            refrescar(null);
            txtNuevaCantidad.clear();
        }, "Carrito vacío.");
    }

    @FXML
    private void confirmarVenta() {
        try {
            String cuiTexto = txtCuiCliente
                    .getText()
                    .trim();

            if (cuiTexto.isEmpty()) {
                throw new IllegalArgumentException(
                        "Ingresa el CUI del cliente"
                );
            }

            long cuiCliente = Long.parseLong(cuiTexto);

            if (cuiCliente <= 0) {
                throw new IllegalArgumentException(
                        "El CUI del cliente no es válido"
                );
            }

            List<DetalleVenta> detalles =
                    carrito.getDetalles();

            Venta venta = carrito.confirmarVenta(
                    cuiCliente,
                    idUsuario,
                    ventaDao
            );

            Stage stage = (Stage) btnConfirmar
                    .getScene()
                    .getWindow();

            ComprobanteVenta.mostrar(
                    venta,
                    detalles,
                    stage
            );

        } catch (NumberFormatException e) {
            mostrarError(
                    "Ingresa un CUI válido."
            );

        } catch (IllegalArgumentException
                | SQLException
                | IOException e) {

            mostrarError(
                    e.getMessage()
            );
        }
    }

    private void refrescar(String isbnSeleccionado) {
        tabla.getItems().setAll(
                carrito.getDetalles()
        );

        lblTotal.setText(
                "Total: Q"
                + carrito.getTotal().toPlainString()
        );

        if (isbnSeleccionado == null) {
            return;
        }

        for (DetalleVenta detalle : tabla.getItems()) {
            if (detalle.getIsbn()
                    .equals(isbnSeleccionado)) {

                tabla.getSelectionModel()
                        .select(detalle);

                break;
            }
        }
    }

    private int enteroPositivo(
            String texto,
            String nombre) {

        try {
            int valor = Integer.parseInt(
                    texto.trim()
            );

            if (valor > 0) {
                return valor;
            }

        } catch (NumberFormatException ignored) {
        }

        throw new IllegalArgumentException(
                nombre
                + " debe ser un entero mayor que cero"
        );
    }

    private void ejecutar(
            Runnable accion,
            String mensaje) {

        try {
            accion.run();

            lblMensaje.setStyle(
                    "-fx-text-fill: #166534;"
            );

            lblMensaje.setText(
                    mensaje
            );

        } catch (IllegalArgumentException
                | ArithmeticException e) {

            mostrarError(
                    e instanceof ArithmeticException
                            ? "Usa precios de hasta dos decimales."
                            : e.getMessage()
            );
        }
    }

    private void mostrarError(String mensaje) {
        lblMensaje.setStyle(
                "-fx-text-fill: #b91c1c;"
        );

        lblMensaje.setText(
                mensaje == null
                        ? "Ocurrió un error."
                        : mensaje
        );
    }

    public void setIdUsuario(int idUsuario) {
        if (idUsuario <= 0) {
            throw new IllegalArgumentException(
                    "El usuario no es válido"
            );
        }

        this.idUsuario = idUsuario;
    }
}