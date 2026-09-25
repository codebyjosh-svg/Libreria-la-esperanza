package org.esperanza.Controller;

import java.sql.SQLException;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import org.esperanza.dao.MovimientoInventarioDAO;
import org.esperanza.dao.MovimientoInventarioDAO.LibroDisponible;
import org.esperanza.Service.NavegacionRol;
import org.esperanza.Service.Pantallas;
import org.esperanza.Service.SesionUsuario;

public class IngresoInventarioController {

    @FXML
    private ComboBox<LibroDisponible> cmbLibro;

    @FXML
    private Label lblIsbn;

    @FXML
    private Label lblStockActual;

    @FXML
    private TextField txtCantidad;

    @FXML
    private TextArea txtObservacion;

    @FXML
    private Button btnCancelar;

    private final MovimientoInventarioDAO movimientoDAO =
            new MovimientoInventarioDAO();

    private int idUsuarioActual;

    @FXML
    public void initialize() {

        if (!NavegacionRol.validarPermiso(
                "ENTRADAS_SALIDAS")) {
            return;
        }

        txtCantidad.setTextFormatter(
                new TextFormatter<String>(
                        cambio ->
                                cambio
                                        .getControlNewText()
                                        .matches("\\d*")
                                        ? cambio
                                        : null
                )
        );

        cmbLibro.setOnAction(
                event -> mostrarDatosLibro()
        );

        btnCancelar.setText(
                "Volver al Dashboard"
        );

        btnCancelar.setPrefWidth(
                160
        );

        btnCancelar.setOnAction(
                event -> onCancelar()
        );

        cargarLibros();
    }

    public void setIdUsuarioActual(
            int idUsuarioActual) {

        this.idUsuarioActual =
                idUsuarioActual;
    }

    private void cargarLibros() {

        try {

            cmbLibro
                    .getItems()
                    .setAll(
                            movimientoDAO
                                    .listarLibrosDisponibles()
                    );

        } catch (SQLException ex) {

            mostrarError(
                    "No se pudieron cargar los libros.\n"
                    + ex.getMessage()
            );
        }
    }

    private void mostrarDatosLibro() {

        LibroDisponible libro =
                cmbLibro.getValue();

        if (libro == null) {

            lblIsbn.setText("-");
            lblStockActual.setText("0");

            return;
        }

        lblIsbn.setText(
                libro.getIsbn()
        );

        lblStockActual.setText(
                String.valueOf(
                        libro.getStockActual()
                )
        );
    }

    public int obtenerCantidadIngresada() {

        String texto =
                txtCantidad.getText();

        if (texto == null
                || texto.isBlank()) {

            throw new IllegalArgumentException(
                    "Debe ingresar una cantidad."
            );
        }

        int cantidad;

        try {

            cantidad =
                    Integer.parseInt(
                            texto
                    );

        } catch (NumberFormatException ex) {

            throw new IllegalArgumentException(
                    "La cantidad ingresada no es válida."
            );
        }

        if (cantidad <= 0) {

            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que 0."
            );
        }

        return cantidad;
    }

    @FXML
    private void onRegistrarClick() {

        if (!NavegacionRol.validarPermiso(
                "ENTRADAS_SALIDAS")) {
            return;
        }

        try {

            SesionUsuario sesion =
                    SesionUsuario.getInstancia();

            if (!sesion.tienePermiso(
                    "ENTRADAS_SALIDAS")) {

                mostrarError(
                        "No tienes permiso para registrar ingresos."
                );

                return;
            }

            if (sesion.getUsuarioActual() == null) {

                throw new IllegalArgumentException(
                        "No se pudo identificar al usuario."
                );
            }

            idUsuarioActual =
                    sesion
                            .getUsuarioActual()
                            .getId();

            if (idUsuarioActual <= 0) {

                throw new IllegalArgumentException(
                        "No se pudo identificar al usuario."
                );
            }

            LibroDisponible libro =
                    cmbLibro.getValue();

            if (libro == null) {

                throw new IllegalArgumentException(
                        "Debe seleccionar un libro."
                );
            }

            int cantidad =
                    obtenerCantidadIngresada();

            String observacion =
                    txtObservacion.getText() == null
                            ? ""
                            : txtObservacion
                                    .getText()
                                    .trim();

            boolean registrado =
                    movimientoDAO
                            .registrarIngresoInventario(
                                    libro.getIsbn(),
                                    idUsuarioActual,
                                    cantidad,
                                    observacion
                            );

            if (registrado) {

                mostrarConfirmacion(
                        "Ingreso registrado correctamente.\n"
                        + "Libro: "
                        + libro.getTitulo()
                        + "\nCantidad ingresada: "
                        + cantidad
                );

                limpiarFormulario();
                cargarLibros();

            } else {

                mostrarError(
                        "No se pudo registrar el ingreso."
                );
            }

        } catch (IllegalArgumentException ex) {

            mostrarError(
                    ex.getMessage()
            );

        } catch (SQLException ex) {

            mostrarError(
                    "No se pudo registrar el ingreso.\n"
                    + ex.getMessage()
            );
        }
    }

    @FXML
    private void onCancelar() {

        Pantallas.volver(
                cmbLibro
        );
    }

    private void limpiarFormulario() {

        cmbLibro
                .getSelectionModel()
                .clearSelection();

        txtCantidad.clear();
        txtObservacion.clear();

        lblIsbn.setText("-");
        lblStockActual.setText("0");
    }

    private void mostrarConfirmacion(
            String mensaje) {

        Alert alerta =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        if (cmbLibro.getScene() != null
                && cmbLibro
                        .getScene()
                        .getWindow() != null) {

            alerta.initOwner(
                    cmbLibro
                            .getScene()
                            .getWindow()
            );
        }

        alerta.setTitle(
                "Ingreso de Inventario"
        );

        alerta.setHeaderText(
                "Operación exitosa"
        );

        alerta.setContentText(
                mensaje
        );

        alerta.showAndWait();
    }

    private void mostrarError(
            String mensaje) {

        Alert alerta =
                new Alert(
                        Alert.AlertType.ERROR
                );

        if (cmbLibro.getScene() != null
                && cmbLibro
                        .getScene()
                        .getWindow() != null) {

            alerta.initOwner(
                    cmbLibro
                            .getScene()
                            .getWindow()
            );
        }

        alerta.setTitle(
                "Ingreso de Inventario"
        );

        alerta.setHeaderText(
                "No se pudo completar la operación"
        );

        alerta.setContentText(
                mensaje
        );

        alerta.showAndWait();
    }
}