package org.esperanza.Controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import org.esperanza.Model.CarritoVenta;
import org.esperanza.Model.DetalleVenta;
import org.esperanza.Model.DescuentoVenta;
import org.esperanza.Model.Libro;

import org.esperanza.Service.NavegacionRol;

import org.esperanza.dao.LibroDAO;
import org.esperanza.dao.impl.LibroDAOImpl;

public class CarritoVentaController {

    @FXML
    private TextField txtBuscar;
    @FXML
    private TextField txtCantidad;
    @FXML
    private TextField txtNuevaCantidad;
    @FXML
    private TableView<Libro> tblDisponibles;
    @FXML
    private TableColumn<Libro, String> colDisponibleIsbn;
    @FXML
    private TableColumn<Libro, String> colDisponibleTitulo;
    @FXML
    private TableColumn<Libro, Double> colDisponiblePrecio;
    @FXML
    private TableColumn<Libro, Integer> colDisponibleStock;
    @FXML
    private TableView<DetalleVenta> tabla;
    @FXML
    private TableColumn<DetalleVenta, String> colIsbn;
    @FXML
    private TableColumn<DetalleVenta, Integer> colCantidad;
    @FXML
    private TableColumn<DetalleVenta, BigDecimal> colPrecio;
    @FXML
    private TableColumn<DetalleVenta, BigDecimal> colSubtotal;
    @FXML
    private Button btnAgregar;
    @FXML
    private Button btnActualizar;
    @FXML
    private Button btnEliminar;
    @FXML
    private Button btnVaciar;
    @FXML
    private Button btnConfirmar;
    @FXML
    private ComboBox<String> cmbTipoDescuento;
    @FXML
    private TextField txtValorDescuento;
    @FXML
    private Label lblSubtotalVenta;
    @FXML
    private Label lblDescuentoVenta;
    @FXML
    private Label lblTotal;
    @FXML
    private Label lblAvisoDescuento;
    @FXML
    private Label lblMensaje;

    private final CarritoVenta carrito
            = new CarritoVenta();

    private final LibroDAO libroDAO
            = new LibroDAOImpl();

    private final ObservableList<Libro> librosDisponibles
            = FXCollections.observableArrayList();

    private final ObservableList<DetalleVenta> detallesObservable
            = FXCollections.observableArrayList();

    private FilteredList<Libro> librosFiltrados;

    private int idUsuario;

    @FXML
    public void initialize() {

        if (!NavegacionRol.validarPermiso("VENTAS")) {
            return;
        }

        txtCantidad.setText("1");
        txtNuevaCantidad.setText("1");
        configurarTablaDisponibles();
        configurarTablaCarrito();
        configurarBusqueda();
        cargarLibros();
        configurarDescuento();
        actualizarTotales();
        btnAgregar.disableProperty().bind(
                tblDisponibles.getSelectionModel()
                        .selectedItemProperty()
                        .isNull()
        );

        btnActualizar.disableProperty().bind(
                tabla.getSelectionModel()
                        .selectedItemProperty()
                        .isNull()
        );

        btnEliminar.disableProperty().bind(
                tabla.getSelectionModel()
                        .selectedItemProperty()
                        .isNull()
        );

        btnVaciar.disableProperty().bind(
                Bindings.isEmpty(tabla.getItems())
        );

        btnConfirmar.disableProperty().bind(
                Bindings.isEmpty(tabla.getItems())
        );

    }

    private void cargarLibros() {
        try {
            librosDisponibles.setAll(
                    libroDAO.listarDisponibles()
            );
        } catch (SQLException e) {
            mostrarError(
                    "No se pudieron cargar los libros."
            );
        }
    }

    private void configurarTablaDisponibles() {
        colDisponibleIsbn.setCellValueFactory(
                dato -> new ReadOnlyObjectWrapper<>(
                        dato.getValue().getIsbn()
                )
        );

        colDisponibleTitulo.setCellValueFactory(
                dato -> new ReadOnlyObjectWrapper<>(
                        dato.getValue().getTitulo()
                )
        );

        colDisponiblePrecio.setCellValueFactory(
                dato -> new ReadOnlyObjectWrapper<>(
                        dato.getValue().getPrecio()
                )
        );

        colDisponibleStock.setCellValueFactory(
                dato -> new ReadOnlyObjectWrapper<>(
                        dato.getValue().getStockActual()
                )
        );

        librosFiltrados
                = new FilteredList<>(
                        librosDisponibles,
                        p -> true
                );

        tblDisponibles.setItems(
                librosFiltrados
        );

    }

    private void configurarTablaCarrito() {

        colIsbn.setCellValueFactory(
                dato -> new ReadOnlyObjectWrapper<>(
                        dato.getValue().getIsbn()
                )
        );

        colCantidad.setCellValueFactory(
                dato -> new ReadOnlyObjectWrapper<>(
                        dato.getValue().getCantidad()
                )
        );

        colPrecio.setCellValueFactory(
                dato -> new ReadOnlyObjectWrapper<>(
                        dato.getValue().getPrecioUnitario()
                )
        );

        colSubtotal.setCellValueFactory(
                dato -> new ReadOnlyObjectWrapper<>(
                        dato.getValue().getSubtotal()
                )
        );

        tabla.setItems(
                detallesObservable
        );

    }

    private void configurarBusqueda() {

        txtBuscar.textProperty()
                .addListener(
                        (obs, viejo, texto) -> {

                            librosFiltrados.setPredicate(
                                    libro -> {

                                        if (texto == null || texto.isBlank()) {
                                            return true;
                                        }

                                        String filtro
                                        = texto.toLowerCase();

                                        return libro.getIsbn()
                                                .toLowerCase()
                                                .contains(filtro)
                                        || libro.getTitulo()
                                                .toLowerCase()
                                                .contains(filtro);

                                    });

                        });

    }

    private void configurarDescuento() {

        cmbTipoDescuento.getItems()
                .addAll(
                        "SIN DESCUENTO",
                        "PORCENTAJE",
                        "MONTO FIJO"
                );

        cmbTipoDescuento.setValue(
                "SIN DESCUENTO"
        );

    }

    @FXML
private void volver(){

    Stage stage =
            (Stage) btnConfirmar
                    .getScene()
                    .getWindow();


    NavegacionRol
            .abrirDashboardSegunRol(stage);

}
    @FXML
    private void agregarLibro() {

        Libro libro
                = tblDisponibles
                        .getSelectionModel()
                        .getSelectedItem();

        if (libro == null) {
            return;
        }

        int cantidad;

        try {

            cantidad
                    = Integer.parseInt(
                            txtCantidad.getText()
                    );

        } catch (Exception e) {

            mostrarError(
                    "Cantidad inválida."
            );

            return;
        }

        if (cantidad <= 0) {

            mostrarError(
                    "La cantidad debe ser mayor a cero."
            );

            return;
        }

        if (cantidad > libro.getStockActual()) {

            mostrarError(
                    "No hay suficiente stock."
            );

            return;

        }

        DetalleVenta detalle
                = new DetalleVenta();

        detalle.setIsbn(
                libro.getIsbn()
        );

        detalle.setCantidad(
                cantidad
        );

        detalle.setPrecioUnitario(
                BigDecimal.valueOf(
                        libro.getPrecio()
                )
        );

        detallesObservable.add(
                detalle
        );

        actualizarTotales();
        lblMensaje.setText(
                "Producto agregado correctamente."
        );

    }

    @FXML
    private void actualizarCantidad() {
        DetalleVenta seleccionado
                = tabla.getSelectionModel()
                        .getSelectedItem();
        if (seleccionado == null) {
            return;
        }

        try {

            int cantidad
                    = Integer.parseInt(
                            txtNuevaCantidad.getText()
                    );

            seleccionado.setCantidad(
                    cantidad
            );

            tabla.refresh();

            actualizarTotales();

        } catch (Exception e) {

            mostrarError(
                    "Cantidad inválida.");
        }
    }

    @FXML
    private void eliminarLibro() {

        DetalleVenta seleccionado
                = tabla.getSelectionModel()
                        .getSelectedItem();

        if (seleccionado != null) {

            detallesObservable.remove(
                    seleccionado);

            actualizarTotales();
        }
    }

    @FXML
    private void vaciarCarrito() {

        detallesObservable.clear();

        actualizarTotales();

        lblMensaje.setText(
                "Carrito vacío.");
    }

    private void actualizarTotales() {

        BigDecimal subtotal
                = BigDecimal.ZERO;

        for (DetalleVenta detalle
                : detallesObservable) {

            subtotal
                    = subtotal.add(
                            detalle.getSubtotal());
        }

        BigDecimal descuento
                = calcularDescuento(
                        subtotal);

        BigDecimal total
                = subtotal.subtract(
                        descuento
                );

        lblSubtotalVenta.setText(
                "Subtotal: Q"
                + subtotal
        );

        lblDescuentoVenta.setText(
                "Descuento: -Q"
                + descuento
        );

        lblTotal.setText(
                "Total: Q"
                + total
        );

    }

    private BigDecimal calcularDescuento(
            BigDecimal subtotal) {

        if (cmbTipoDescuento.getValue() == null) {

            return BigDecimal.ZERO;

        }

        String tipo
                = cmbTipoDescuento.getValue();

        try {

            BigDecimal valor
                    = new BigDecimal(
                            txtValorDescuento.getText()
                    );

            if (tipo.equals("PORCENTAJE")) {

                return subtotal
                        .multiply(valor)
                        .divide(
                                new BigDecimal("100")
                        );

            }

            if (tipo.equals("MONTO FIJO")) {

                return valor;

            }

        } catch (Exception e) {

            return BigDecimal.ZERO;

        }

        return BigDecimal.ZERO;

    }

    @FXML
    private void confirmarVenta() {

        if (detallesObservable.isEmpty()) {

            mostrarError(
                    "El carrito está vacío."
            );

            return;

        }

        try {

            FXMLLoader loader
                    = new FXMLLoader(
                            getClass()
                                    .getResource(
                                            "/org/esperanza/view/ConfirmarVenta.fxml"
                                    )
                    );

            Parent root
                    = loader.load();

            ConfirmarVentaController controller
                    = loader.getController();

            BigDecimal subtotal
                    = detallesObservable.stream()
                            .map(DetalleVenta::getSubtotal)
                            .reduce(
                                    BigDecimal.ZERO,
                                    BigDecimal::add
                            );

            BigDecimal descuento
                    = calcularDescuento(
                            subtotal
                    );

            BigDecimal total
                    = subtotal.subtract(
                            descuento
                    );

          DescuentoVenta descuentoVenta =
        DescuentoVenta.sinDescuento();
          
            controller.cargarDatos(
        "",
        idUsuario,
        detallesObservable,
        total,
        descuentoVenta
);

            Stage stage
                    = (Stage) btnConfirmar
                            .getScene()
                            .getWindow();

            stage.setScene(
                    new Scene(root)
            );

            stage.show();

        } catch (IOException e) {

            mostrarError(
                    "No se pudo abrir confirmación de venta."
            );

            e.printStackTrace();

        }

    }

    public void setIdUsuario(int idUsuario) {

        this.idUsuario = idUsuario;

    }

    private void mostrarError(String mensaje) {

        Alert alerta
                = new Alert(
                        Alert.AlertType.ERROR
                );

        alerta.setTitle(
                "Error"
        );

        alerta.setHeaderText(
                null
        );

        alerta.setContentText(
                mensaje
        );

        alerta.showAndWait();
    }
}
