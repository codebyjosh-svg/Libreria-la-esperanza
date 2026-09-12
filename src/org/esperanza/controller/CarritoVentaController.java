package org.esperanza.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.esperanza.Service.NavegacionRol;
import org.esperanza.dao.ClienteDao;
import org.esperanza.dao.LibroDAO;
import org.esperanza.dao.VentaDao;
import org.esperanza.dao.impl.LibroDAOImpl;
import org.esperanza.model.CarritoVenta;
import org.esperanza.model.Cliente;
import org.esperanza.model.DetalleVenta;
import org.esperanza.model.Libro;
import org.esperanza.model.Venta;
import org.esperanza.view.ComprobanteVenta;

public class CarritoVentaController {

    @FXML private TextField txtBuscarCliente;
    @FXML private TableView<Cliente> tblClientes;
    @FXML private TableColumn<Cliente, Long> colClienteCui;
    @FXML private TableColumn<Cliente, String> colClienteNombre;
    @FXML private TableColumn<Cliente, String> colClienteApellido;
    @FXML private TableColumn<Cliente, String> colClienteCorreo;
    @FXML private Label lblClienteSeleccionado;

    @FXML private VBox panelNuevoCliente;
    @FXML private TextField txtNuevoCui;
    @FXML private TextField txtNuevoNombre;
    @FXML private TextField txtNuevoApellido;
    @FXML private TextField txtNuevoCorreo;

    @FXML private TextField txtBuscar;
    @FXML private TextField txtCantidad;
    @FXML private TextField txtNuevaCantidad;

    @FXML private TableView<Libro> tblDisponibles;
    @FXML private TableColumn<Libro, String> colDisponibleIsbn;
    @FXML private TableColumn<Libro, String> colDisponibleTitulo;
    @FXML private TableColumn<Libro, Double> colDisponiblePrecio;
    @FXML private TableColumn<Libro, Integer> colDisponibleStock;

    @FXML private TableView<DetalleVenta> tabla;
    @FXML private TableColumn<DetalleVenta, String> colIsbn;
    @FXML private TableColumn<DetalleVenta, Integer> colCantidad;
    @FXML private TableColumn<DetalleVenta, BigDecimal> colPrecio;
    @FXML private TableColumn<DetalleVenta, BigDecimal> colSubtotal;

    @FXML private Button btnAgregar;
    @FXML private Button btnActualizar;
    @FXML private Button btnEliminar;
    @FXML private Button btnVaciar;
    @FXML private Button btnConfirmar;

    @FXML private Label lblTotal;
    @FXML private Label lblMensaje;

    private final CarritoVenta carrito =
            new CarritoVenta();

    private final VentaDao ventaDao =
            new VentaDao();

    private final LibroDAO libroDAO =
            new LibroDAOImpl();

    private final ClienteDao clienteDao =
            new ClienteDao();

    private final ObservableList<Libro> librosDisponibles =
            FXCollections.observableArrayList();

    private final ObservableList<Cliente> clientes =
            FXCollections.observableArrayList();

    private FilteredList<Libro> librosFiltrados;
    private FilteredList<Cliente> clientesFiltrados;

    private int idUsuario = 5;

    @FXML
    public void initialize() {

        configurarTablaClientes();
        configurarTablaDisponibles();
        configurarTablaCarrito();

        cargarClientes();
        cargarLibrosDisponibles();

        configurarBusquedaClientes();
        configurarBusquedaLibros();

        txtCantidad.setText("1");
        txtNuevaCantidad.setText("1");

        lblTotal.setText(
                "Total: Q0.00"
        );

        lblClienteSeleccionado.setText(
                "Cliente seleccionado: ninguno"
        );

        btnAgregar.disableProperty().bind(
                tblDisponibles
                        .getSelectionModel()
                        .selectedItemProperty()
                        .isNull()
        );

        btnActualizar.disableProperty().bind(
                tabla
                        .getSelectionModel()
                        .selectedItemProperty()
                        .isNull()
        );

        btnEliminar.disableProperty().bind(
                tabla
                        .getSelectionModel()
                        .selectedItemProperty()
                        .isNull()
        );

        btnVaciar.disableProperty().bind(
                Bindings.isEmpty(
                        tabla.getItems()
                )
        );

        btnConfirmar.disableProperty().bind(
                Bindings.isEmpty(
                        tabla.getItems()
                )
        );

        tblClientes
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (obs, anterior, cliente) -> {

                            if (cliente == null) {

                                lblClienteSeleccionado.setText(
                                        "Cliente seleccionado: ninguno"
                                );

                            } else {

                                lblClienteSeleccionado.setText(
                                        "Cliente seleccionado: "
                                        + cliente.getNombreCompleto()
                                        + " - CUI: "
                                        + cliente.getCui()
                                );
                            }
                        }
                );

        tabla
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (obs, anterior, seleccionado) -> {

                            if (seleccionado != null) {

                                txtNuevaCantidad.setText(
                                        String.valueOf(
                                                seleccionado.getCantidad()
                                        )
                                );
                            }
                        }
                );

        configurarCierre();
    }

    private void configurarTablaClientes() {

        colClienteCui.setCellValueFactory(
                c -> new ReadOnlyObjectWrapper<>(
                        c.getValue().getCui()
                )
        );

        colClienteNombre.setCellValueFactory(
                c -> new ReadOnlyObjectWrapper<>(
                        c.getValue().getNombre()
                )
        );

        colClienteApellido.setCellValueFactory(
                c -> new ReadOnlyObjectWrapper<>(
                        c.getValue().getApellido()
                )
        );

        colClienteCorreo.setCellValueFactory(
                c -> new ReadOnlyObjectWrapper<>(
                        c.getValue().getCorreo()
                )
        );

        tblClientes.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        tblClientes.setPlaceholder(
                new Label(
                        "No hay clientes registrados."
                )
        );
    }

    private void cargarClientes() {

        try {

            clientes.setAll(
                    clienteDao.listar()
            );

            clientesFiltrados =
                    new FilteredList<>(
                            clientes,
                            cliente -> true
                    );

            tblClientes.setItems(
                    clientesFiltrados
            );

        } catch (SQLException e) {

            mostrarError(
                    "No se pudieron cargar los clientes: "
                    + e.getMessage()
            );
        }
    }

    private void configurarBusquedaClientes() {

        txtBuscarCliente
                .textProperty()
                .addListener(
                        (obs, anterior, nuevo) -> {

                            if (clientesFiltrados == null) {
                                return;
                            }

                            String criterio =
                                    nuevo == null
                                            ? ""
                                            : nuevo
                                                    .trim()
                                                    .toLowerCase();

                            clientesFiltrados.setPredicate(
                                    cliente -> {

                                        if (criterio.isEmpty()) {
                                            return true;
                                        }

                                        String cui =
                                                String.valueOf(
                                                        cliente.getCui()
                                                );

                                        String nombre =
                                                cliente.getNombre() == null
                                                        ? ""
                                                        : cliente
                                                                .getNombre()
                                                                .toLowerCase();

                                        String apellido =
                                                cliente.getApellido() == null
                                                        ? ""
                                                        : cliente
                                                                .getApellido()
                                                                .toLowerCase();

                                        String correo =
                                                cliente.getCorreo() == null
                                                        ? ""
                                                        : cliente
                                                                .getCorreo()
                                                                .toLowerCase();

                                        return cui.contains(criterio)
                                                || nombre.contains(criterio)
                                                || apellido.contains(criterio)
                                                || correo.contains(criterio);
                                    }
                            );
                        }
                );
    }

    @FXML
    private void mostrarNuevoCliente() {

        panelNuevoCliente.setVisible(
                true
        );

        panelNuevoCliente.setManaged(
                true
        );

        txtNuevoCui.requestFocus();
    }

    @FXML
    private void cancelarNuevoCliente() {

        panelNuevoCliente.setVisible(
                false
        );

        panelNuevoCliente.setManaged(
                false
        );

        limpiarFormularioCliente();
    }

    @FXML
    private void guardarNuevoCliente() {

        try {

            String cuiTexto =
                    txtNuevoCui
                            .getText()
                            .trim();

            if (!cuiTexto.matches("\\d{13}")) {

                throw new IllegalArgumentException(
                        "El CUI debe contener 13 dígitos."
                );
            }

            long cui =
                    Long.parseLong(
                            cuiTexto
                    );

            String nombre =
                    txtNuevoNombre
                            .getText()
                            .trim();

            String apellido =
                    txtNuevoApellido
                            .getText()
                            .trim();

            String correo =
                    txtNuevoCorreo
                            .getText()
                            .trim();

            if (nombre.isEmpty()) {

                throw new IllegalArgumentException(
                        "El nombre es obligatorio."
                );
            }

            if (apellido.isEmpty()) {

                throw new IllegalArgumentException(
                        "El apellido es obligatorio."
                );
            }

            if (!correo.isEmpty()
                    && !correo.matches(
                            "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"
                    )) {

                throw new IllegalArgumentException(
                        "El correo electrónico no es válido."
                );
            }

            if (clienteDao.buscarPorCui(cui) != null) {

                throw new IllegalArgumentException(
                        "Ya existe un cliente con ese CUI."
                );
            }

            Cliente nuevoCliente =
                    new Cliente(
                            cui,
                            nombre,
                            apellido,
                            correo
                    );

            if (!clienteDao.insertar(
                    nuevoCliente
            )) {

                throw new SQLException(
                        "No se pudo registrar el cliente."
                );
            }

            txtBuscarCliente.clear();

            cargarClientes();

            seleccionarCliente(
                    cui
            );

            cancelarNuevoCliente();

            lblMensaje.setStyle(
                    "-fx-text-fill: #166534;"
            );

            lblMensaje.setText(
                    "Cliente registrado y seleccionado."
            );

        } catch (NumberFormatException e) {

            mostrarError(
                    "El CUI no es válido."
            );

        } catch (IllegalArgumentException
                | SQLException e) {

            mostrarError(
                    e.getMessage()
            );
        }
    }

    private void seleccionarCliente(
            long cui) {

        for (Cliente cliente
                : tblClientes.getItems()) {

            if (cliente.getCui() == cui) {

                tblClientes
                        .getSelectionModel()
                        .select(cliente);

                tblClientes.scrollTo(
                        cliente
                );

                break;
            }
        }
    }

    private void limpiarFormularioCliente() {

        txtNuevoCui.clear();
        txtNuevoNombre.clear();
        txtNuevoApellido.clear();
        txtNuevoCorreo.clear();
    }

    private void configurarTablaDisponibles() {

        colDisponibleIsbn.setCellValueFactory(
                c -> new ReadOnlyObjectWrapper<>(
                        c.getValue().getIsbn()
                )
        );

        colDisponibleTitulo.setCellValueFactory(
                c -> new ReadOnlyObjectWrapper<>(
                        c.getValue().getTitulo()
                )
        );

        colDisponiblePrecio.setCellValueFactory(
                c -> new ReadOnlyObjectWrapper<>(
                        c.getValue().getPrecio()
                )
        );

        colDisponibleStock.setCellValueFactory(
                c -> new ReadOnlyObjectWrapper<>(
                        c.getValue().getStockActual()
                )
        );

        tblDisponibles.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        tblDisponibles.setPlaceholder(
                new Label(
                        "No hay libros disponibles."
                )
        );

        tblDisponibles.setOnMouseClicked(
                event -> {

                    if (event.getClickCount() == 2
                            && tblDisponibles
                                    .getSelectionModel()
                                    .getSelectedItem() != null) {

                        agregarProducto();
                    }
                }
        );
    }

    private void configurarTablaCarrito() {

        colIsbn.setCellValueFactory(
                c -> new ReadOnlyObjectWrapper<>(
                        c.getValue().getIsbn()
                )
        );

        colCantidad.setCellValueFactory(
                c -> new ReadOnlyObjectWrapper<>(
                        c.getValue().getCantidad()
                )
        );

        colPrecio.setCellValueFactory(
                c -> new ReadOnlyObjectWrapper<>(
                        c.getValue().getPrecioUnitario()
                )
        );

        colSubtotal.setCellValueFactory(
                c -> new ReadOnlyObjectWrapper<>(
                        c.getValue().getSubtotal()
                )
        );

        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        tabla.setPlaceholder(
                new Label(
                        "Agrega libros para iniciar la venta."
                )
        );
    }

    private void cargarLibrosDisponibles() {

        librosDisponibles.clear();

        List<Libro> libros =
                libroDAO.listarTodos();

        if (libros != null) {

            for (Libro libro : libros) {

                if (libro.getStockActual() > 0) {

                    librosDisponibles.add(
                            libro
                    );
                }
            }
        }

        librosFiltrados =
                new FilteredList<>(
                        librosDisponibles,
                        libro -> true
                );

        tblDisponibles.setItems(
                librosFiltrados
        );
    }

    private void configurarBusquedaLibros() {

        txtBuscar
                .textProperty()
                .addListener(
                        (obs, anterior, nuevo) -> {

                            if (librosFiltrados == null) {
                                return;
                            }

                            String criterio =
                                    nuevo == null
                                            ? ""
                                            : nuevo
                                                    .trim()
                                                    .toLowerCase();

                            librosFiltrados.setPredicate(
                                    libro -> {

                                        if (criterio.isEmpty()) {
                                            return true;
                                        }

                                        String isbn =
                                                libro.getIsbn() == null
                                                        ? ""
                                                        : libro
                                                                .getIsbn()
                                                                .toLowerCase();

                                        String titulo =
                                                libro.getTitulo() == null
                                                        ? ""
                                                        : libro
                                                                .getTitulo()
                                                                .toLowerCase();

                                        String autor =
                                                libro.getNombreAutor() == null
                                                        ? ""
                                                        : libro
                                                                .getNombreAutor()
                                                                .toLowerCase();

                                        return isbn.contains(criterio)
                                                || titulo.contains(criterio)
                                                || autor.contains(criterio);
                                    }
                            );
                        }
                );
    }

    @FXML
    private void agregarProducto() {

        ejecutar(() -> {

            Libro libro =
                    tblDisponibles
                            .getSelectionModel()
                            .getSelectedItem();

            if (libro == null) {

                throw new IllegalArgumentException(
                        "Selecciona un libro de la lista."
                );
            }

            int cantidad =
                    enteroPositivo(
                            txtCantidad.getText(),
                            "La cantidad"
                    );

            int cantidadActual =
                    obtenerCantidadEnCarrito(
                            libro.getIsbn()
                    );

            if (cantidadActual + cantidad
                    > libro.getStockActual()) {

                throw new IllegalArgumentException(
                        "No hay suficiente stock. Disponible: "
                        + libro.getStockActual()
                );
            }

            BigDecimal precio =
                    BigDecimal.valueOf(
                            libro.getPrecio()
                    );

            carrito.agregarProducto(
                    libro.getIsbn(),
                    cantidad,
                    precio
            );

            refrescar(
                    libro.getIsbn()
            );

            txtCantidad.setText(
                    "1"
            );

        }, "Libro agregado al carrito.");
    }

    private int obtenerCantidadEnCarrito(
            String isbn) {

        for (DetalleVenta detalle
                : carrito.getDetalles()) {

            if (detalle
                    .getIsbn()
                    .equals(isbn)) {

                return detalle.getCantidad();
            }
        }

        return 0;
    }

    @FXML
    private void actualizarCantidad() {

        ejecutar(() -> {

            DetalleVenta detalle =
                    tabla
                            .getSelectionModel()
                            .getSelectedItem();

            if (detalle == null) {

                throw new IllegalArgumentException(
                        "Selecciona un libro del carrito."
                );
            }

            int cantidad =
                    enteroPositivo(
                            txtNuevaCantidad.getText(),
                            "La cantidad"
                    );

            Libro libro =
                    buscarLibroDisponible(
                            detalle.getIsbn()
                    );

            if (libro != null
                    && cantidad
                    > libro.getStockActual()) {

                throw new IllegalArgumentException(
                        "No hay suficiente stock. Disponible: "
                        + libro.getStockActual()
                );
            }

            carrito.cambiarCantidad(
                    detalle.getIsbn(),
                    cantidad
            );

            refrescar(
                    detalle.getIsbn()
            );

        }, "Cantidad actualizada.");
    }

    private Libro buscarLibroDisponible(
            String isbn) {

        for (Libro libro
                : librosDisponibles) {

            if (libro
                    .getIsbn()
                    .equals(isbn)) {

                return libro;
            }
        }

        return null;
    }

    @FXML
    private void eliminarProducto() {

        ejecutar(() -> {

            DetalleVenta detalle =
                    tabla
                            .getSelectionModel()
                            .getSelectedItem();

            if (detalle == null) {

                throw new IllegalArgumentException(
                        "Selecciona un libro."
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
            txtNuevaCantidad.setText("1");

        }, "Carrito vacío.");
    }

    @FXML
    private void confirmarVenta() {

        try {

            Cliente cliente =
                    tblClientes
                            .getSelectionModel()
                            .getSelectedItem();

            if (cliente == null) {

                throw new IllegalArgumentException(
                        "Debe seleccionar un cliente para la venta."
                );
            }

            List<DetalleVenta> detalles =
                    carrito.getDetalles();

            Venta venta =
                    carrito.confirmarVenta(
                            cliente.getCui(),
                            idUsuario,
                            ventaDao
                    );

            Stage stage =
                    (Stage) btnConfirmar
                            .getScene()
                            .getWindow();

            ComprobanteVenta.mostrar(
                    venta,
                    detalles,
                    stage
            );

            refrescar(null);

            cargarLibrosDisponibles();

            tblClientes
                    .getSelectionModel()
                    .clearSelection();

            txtBuscarCliente.clear();
            txtBuscar.clear();

            lblClienteSeleccionado.setText(
                    "Cliente seleccionado: ninguno"
            );

            txtCantidad.setText("1");
            txtNuevaCantidad.setText("1");

        } catch (IllegalArgumentException
                | SQLException
                | IOException e) {

            mostrarError(
                    e.getMessage()
            );
        }
    }

    private void refrescar(
            String isbnSeleccionado) {

        tabla.getItems().setAll(
                carrito.getDetalles()
        );

        lblTotal.setText(
                "Total: Q"
                + carrito
                        .getTotal()
                        .toPlainString()
        );

        if (isbnSeleccionado == null) {
            return;
        }

        for (DetalleVenta detalle
                : tabla.getItems()) {

            if (detalle
                    .getIsbn()
                    .equals(isbnSeleccionado)) {

                tabla
                        .getSelectionModel()
                        .select(detalle);

                break;
            }
        }
    }

    private int enteroPositivo(
            String texto,
            String nombre) {

        try {

            int valor =
                    Integer.parseInt(
                            texto.trim()
                    );

            if (valor > 0) {
                return valor;
            }

        } catch (NumberFormatException ignored) {
        }

        throw new IllegalArgumentException(
                nombre
                + " debe ser un entero mayor que cero."
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
                    e.getMessage()
            );
        }
    }

    private void mostrarError(
            String mensaje) {

        lblMensaje.setStyle(
                "-fx-text-fill: #b91c1c;"
        );

        lblMensaje.setText(
                mensaje == null
                        ? "Ocurrió un error."
                        : mensaje
        );
    }

    private void configurarCierre() {

        Platform.runLater(() -> {

            Stage stage =
                    (Stage) tabla
                            .getScene()
                            .getWindow();

            stage.setOnCloseRequest(
                    event -> {

                        event.consume();

                        regresarDashboard(
                                stage
                        );
                    }
            );
        });
    }

    private void regresarDashboard(
            Stage stage) {

        stage.setOnCloseRequest(
                null
        );

        NavegacionRol
                .abrirDashboardSegunRol(
                        stage
                );
    }

    public void setIdUsuario(
            int idUsuario) {

        if (idUsuario <= 0) {

            throw new IllegalArgumentException(
                    "El usuario no es válido."
            );
        }

        this.idUsuario =
                idUsuario;
    }
}