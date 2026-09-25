package org.esperanza.Controller;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.esperanza.dao.LibroDAO;
import org.esperanza.dao.impl.LibroDAOImpl;
import org.esperanza.Model.Libro;
import org.esperanza.Service.NavegacionRol;
import org.esperanza.Service.SesionUsuario;

public class LibroController {

    @FXML
    private TextField txtIsbn;

    @FXML
    private TextField txtTitulo;

    @FXML
    private TextField txtPrecio;

    @FXML
    private TextField txtIdCategoria;

    @FXML
    private TextField txtNitEditorial;

    @FXML
    private TextField txtIdProveedor;

    @FXML
    private TextField txtStockActual;

    @FXML
    private TextField txtStockMinimo;

    @FXML
    private DatePicker dpFecha;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnLimpiar;

    @FXML
    private Button btnDesactivar;

    @FXML
    private TableView<Libro> tbLibros;

    @FXML
    private TableColumn<Libro, String> colIsbn;

    @FXML
    private TableColumn<Libro, String> colTitulo;

    @FXML
    private TableColumn<Libro, Double> colPrecio;

    @FXML
    private TableColumn<Libro, Integer> colStock;

    @FXML
    private TableColumn<Libro, Boolean> colActivo;

    private final LibroDAO libroDao
            = new LibroDAOImpl();

    private boolean modoEdicion = false;

    @FXML
    public void initialize() {

        if (!NavegacionRol.validarPermiso(
                "GESTION_INVENTARIO")) {
            return;
        }

        SesionUsuario sesion
                = SesionUsuario.getInstancia();

        if (!sesion.esAdmin()
                && !sesion.esBodega()) {

            throw new SecurityException(
                    "No tienes permiso para administrar libros."
            );
        }

        btnDesactivar.setVisible(
                sesion.esAdmin()
        );

        btnDesactivar.setManaged(
                sesion.esAdmin()
        );

        btnDesactivar.setDisable(
                true
        );

        configurarTabla();
        limpiarFormulario();
        cargarTabla();

        Platform.runLater(
                this::configurarCierre
        );
    }

    private void configurarTabla() {

        colIsbn.setCellValueFactory(
                new PropertyValueFactory<>(
                        "isbn"
                )
        );

        colTitulo.setCellValueFactory(
                new PropertyValueFactory<>(
                        "titulo"
                )
        );

        colPrecio.setCellValueFactory(
                new PropertyValueFactory<>(
                        "precio"
                )
        );

        colStock.setCellValueFactory(
                new PropertyValueFactory<>(
                        "stockActual"
                )
        );

        colActivo.setCellValueFactory(
                new PropertyValueFactory<>(
                        "activo"
                )
        );

        colPrecio.setCellFactory(
                columna -> new TableCell<>() {

            @Override
            protected void updateItem(
                    Double precio,
                    boolean vacio) {

                super.updateItem(
                        precio,
                        vacio
                );

                if (vacio
                        || precio == null) {

                    setText(null);

                } else {

                    setText(
                            String.format(
                                    Locale.US,
                                    "Q%.2f",
                                    precio
                            )
                    );
                }
            }
        });

        colActivo.setCellFactory(
                columna -> new TableCell<>() {

            @Override
            protected void updateItem(
                    Boolean activo,
                    boolean vacio) {

                super.updateItem(
                        activo,
                        vacio
                );

                if (vacio
                        || activo == null) {

                    setText(null);

                } else {

                    setText(
                            activo
                                    ? "Activo"
                                    : "Inactivo"
                    );
                }
            }
        });
    }

    private void cargarTabla() {

        List<Libro> libros
                = libroDao.listarTodos();

        tbLibros.setItems(
                FXCollections
                        .observableArrayList(
                                libros
                        )
        );
    }

    @FXML
    private void guardarLibro(
            ActionEvent event) {

        if (!NavegacionRol.validarPermiso(
                "GESTION_INVENTARIO")) {
            return;
        }

        SesionUsuario sesion
                = SesionUsuario.getInstancia();

        if (!sesion.esAdmin()
                && !sesion.esBodega()) {

            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Acceso denegado",
                    "No tienes permiso para guardar libros."
            );

            return;
        }

        if (txtIsbn.getText().isBlank()
                || txtTitulo.getText().isBlank()
                || txtPrecio.getText().isBlank()
                || txtIdCategoria.getText().isBlank()
                || txtNitEditorial.getText().isBlank()
                || txtIdProveedor.getText().isBlank()) {

            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Datos incompletos",
                    "Completa ISBN, título, precio, categoría, "
                    + "NIT de editorial y proveedor."
            );

            return;
        }

        try {

            double precio
                    = Double.parseDouble(
                            txtPrecio
                                    .getText()
                                    .trim()
                    );

            if (!Double.isFinite(precio)
                    || precio <= 0) {

                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Precio inválido",
                        "El precio debe ser mayor que cero."
                );

                return;
            }

            int idCategoria
                    = Integer.parseInt(
                            txtIdCategoria
                                    .getText()
                                    .trim()
                    );

            int idProveedor
                    = Integer.parseInt(
                            txtIdProveedor
                                    .getText()
                                    .trim()
                    );

            if (idCategoria <= 0
                    || idProveedor <= 0) {

                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Datos inválidos",
                        "Los ID de categoría y proveedor "
                        + "deben ser mayores que cero."
                );

                return;
            }

            int stockActual
                    = leerStock(
                            txtStockActual
                    );

            int stockMinimo
                    = leerStock(
                            txtStockMinimo
                    );

            if (stockActual < 0
                    || stockMinimo < 0) {

                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Stock inválido",
                        "El stock no puede ser negativo."
                );

                return;
            }

            LocalDate fecha
                    = dpFecha.getValue();

            if (fecha == null) {
                fecha = LocalDate.now();
            }

            Libro libro
                    = new Libro();

            libro.setIsbn(
                    txtIsbn
                            .getText()
                            .trim()
            );

            libro.setTitulo(
                    txtTitulo
                            .getText()
                            .trim()
            );

            libro.setPrecio(
                    precio
            );

            libro.setFechaPublicacion(
                    Date.valueOf(
                            fecha
                    )
            );

            libro.setIdCategoria(
                    idCategoria
            );

            libro.setNitEditorial(
                    txtNitEditorial
                            .getText()
                            .trim()
            );

            libro.setIdProveedor(
                    idProveedor
            );

            libro.setStockActual(
                    stockActual
            );

            libro.setStockMinimo(
                    stockMinimo
            );

            boolean guardado;

            if (modoEdicion) {

                guardado
                        = libroDao.actualizar(
                                libro
                        );

            } else {

                guardado
                        = libroDao.insertar(
                                libro
                        );
            }

            if (!guardado) {

                mostrarAlerta(
                        Alert.AlertType.ERROR,
                        "No se pudo guardar",
                        "Revisa que el ISBN no esté duplicado "
                        + "y que la categoría, editorial y proveedor "
                        + "existan en la base de datos."
                );

                return;
            }

            mostrarAlerta(
                    Alert.AlertType.INFORMATION,
                    "Libros",
                    modoEdicion
                            ? "Libro actualizado correctamente."
                            : "Libro registrado correctamente."
            );

            limpiarFormulario();
            cargarTabla();

        } catch (NumberFormatException ex) {

            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Valores inválidos",
                    "Escribe un precio numérico y números enteros "
                    + "en categoría, proveedor y stock."
            );
        }
    }

    private int leerStock(
            TextField campo) {

        String texto
                = campo
                        .getText()
                        .trim();

        if (texto.isEmpty()) {
            return 0;
        }

        return Integer.parseInt(
                texto
        );
    }

    @FXML
    private void seleccionarLibro(
            MouseEvent event) {

        Libro seleccionado
                = tbLibros
                        .getSelectionModel()
                        .getSelectedItem();

        if (seleccionado == null) {
            return;
        }

        modoEdicion = true;

        txtIsbn.setText(
                seleccionado.getIsbn()
        );

        txtIsbn.setDisable(
                true
        );

        txtTitulo.setText(
                seleccionado.getTitulo()
        );

        txtPrecio.setText(
                String.valueOf(
                        seleccionado.getPrecio()
                )
        );

        txtIdCategoria.setText(
                String.valueOf(
                        seleccionado.getIdCategoria()
                )
        );

        txtNitEditorial.setText(
                seleccionado.getNitEditorial()
        );

        txtIdProveedor.setText(
                String.valueOf(
                        seleccionado.getIdProveedor()
                )
        );

        txtStockActual.setText(
                String.valueOf(
                        seleccionado.getStockActual()
                )
        );

        /*
         * El stock existente no se modifica
         * desde Gestión de Libros.
         */
        txtStockActual.setDisable(
                true
        );

        txtStockMinimo.setText(
                String.valueOf(
                        seleccionado.getStockMinimo()
                )
        );

     if (seleccionado
             .getFechaPublicacion() != null){
         
         dpFecha.setValue(
                        new java.sql.Date(
                                    seleccionado
                                                .getFechaPublicacion()
                                                .getTime()
                        ).toLocalDate()
         );
         
     }else{
         
         dpFecha.setValue(
         null
         ); 
     }

        btnGuardar.setText(
                "Guardar cambios"
        );

        btnDesactivar.setDisable(
                !SesionUsuario
                        .getInstancia()
                        .esAdmin()
                || !seleccionado.isActivo()
        );
    }

    @FXML
    private void desactivarLibro(
            ActionEvent event) {

        if (!NavegacionRol.validarPermiso(
                "GESTION_INVENTARIO")) {
            return;
        }

        if (!SesionUsuario
                .getInstancia()
                .esAdmin()) {

            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Acceso denegado",
                    "Solo el administrador puede desactivar libros."
            );

            return;
        }

        Libro seleccionado
                = tbLibros
                        .getSelectionModel()
                        .getSelectedItem();

        if (!modoEdicion
                || seleccionado == null) {

            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Selecciona un libro",
                    "Selecciona el libro que deseas desactivar."
            );

            return;
        }

        if (!seleccionado.isActivo()) {

            mostrarAlerta(
                    Alert.AlertType.INFORMATION,
                    "Libros",
                    "Este libro ya está inactivo."
            );

            return;
        }

        boolean desactivado
                = libroDao.eliminar(
                        seleccionado.getIsbn()
                );

        if (desactivado) {

            mostrarAlerta(
                    Alert.AlertType.INFORMATION,
                    "Libros",
                    "Libro desactivado correctamente."
            );

            limpiarFormulario();
            cargarTabla();

        } else {

            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error",
                    "No se pudo desactivar el libro."
            );
        }
    }

    @FXML
    private void limpiarFormulario() {

        modoEdicion = false;

        tbLibros
                .getSelectionModel()
                .clearSelection();

        txtIsbn.setDisable(
                false
        );

        txtStockActual.setDisable(
                false
        );

        txtIsbn.clear();
        txtTitulo.clear();
        txtPrecio.clear();
        txtIdCategoria.clear();
        txtNitEditorial.clear();
        txtIdProveedor.clear();

        txtStockActual.setText(
                "0"
        );

        txtStockMinimo.setText(
                "0"
        );

        dpFecha.setValue(
                null
        );

        btnGuardar.setText(
                "Guardar"
        );

        btnDesactivar.setDisable(
                true
        );
    }

    @FXML
    private void onVolverDashboard() {

        if (tbLibros.getScene() == null
                || tbLibros
                        .getScene()
                        .getWindow() == null) {

            return;
        }

        Stage ventana
                = (Stage) tbLibros
                        .getScene()
                        .getWindow();

        ventana.setOnCloseRequest(
                null
        );

        Window propietario
                = ventana.getOwner();

        if (propietario != null) {

            ventana.close();

            if (propietario instanceof Stage) {

                Stage principal
                        = (Stage) propietario;

                principal.toFront();
                principal.requestFocus();
            }

            return;
        }

        if (!SesionUsuario
                .getInstancia()
                .haySesionActiva()) {

            ventana.close();
            return;
        }

        NavegacionRol
                .abrirDashboardSegunRol(
                        ventana
                );
    }

    private void configurarCierre() {

        if (tbLibros.getScene() == null
                || tbLibros
                        .getScene()
                        .getWindow() == null) {

            return;
        }

        Stage ventana
                = (Stage) tbLibros
                        .getScene()
                        .getWindow();

        ventana.setOnCloseRequest(
                event -> {

                    event.consume();
                    onVolverDashboard();
                }
        );
    }

    private void mostrarAlerta(
            Alert.AlertType tipo,
            String titulo,
            String mensaje) {

        Alert alerta
                = new Alert(
                        tipo
                );

        alerta.setTitle(
                titulo
        );

        alerta.setHeaderText(
                null
        );

        alerta.setContentText(
                mensaje
        );

        if (tbLibros.getScene() != null
                && tbLibros
                        .getScene()
                        .getWindow() != null) {

            alerta.initOwner(
                    tbLibros
                            .getScene()
                            .getWindow()
            );
        }

        alerta.showAndWait();
    }
}
