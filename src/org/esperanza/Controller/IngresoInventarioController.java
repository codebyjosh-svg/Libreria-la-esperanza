package org.esperanza.controller;

import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import org.esperanza.dao.MovimientoInventarioDAO;
import org.esperanza.dao.MovimientoInventarioDAO.LibroDisponible;

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

    private final MovimientoInventarioDAO movimientoDAO =
            new MovimientoInventarioDAO();

    private int idUsuarioActual;

    @FXML
    public void initialize() {

        txtCantidad.setTextFormatter(
                new TextFormatter<String>(cambio ->
                        cambio.getControlNewText().matches("\\d*")
                                ? cambio
                                : null
                )
        );

        cmbLibro.setOnAction(event -> mostrarDatosLibro());

        cargarLibros();
    }

    public void setIdUsuarioActual(int idUsuarioActual) {
        this.idUsuarioActual = idUsuarioActual;
    }

    private void cargarLibros() {

        try {

            cmbLibro.getItems().setAll(
                    movimientoDAO.listarLibrosDisponibles()
            );

        } catch (SQLException e) {

            mostrarError(
                    "No se pudieron cargar los libros.\n"
                    + e.getMessage()
            );
        }
    }

    private void mostrarDatosLibro() {

        LibroDisponible libro = cmbLibro.getValue();

        if (libro == null) {
            lblIsbn.setText("-");
            lblStockActual.setText("0");
            return;
        }

        lblIsbn.setText(libro.getIsbn());

        lblStockActual.setText(
                String.valueOf(libro.getStockActual())
        );
    }

    public int obtenerCantidadIngresada() {

        String texto = txtCantidad.getText();

        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(
                    "Debe ingresar una cantidad."
            );
        }

        int cantidad;

        try {
            cantidad = Integer.parseInt(texto);
        } catch (NumberFormatException e) {
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

        try {

            LibroDisponible libro = cmbLibro.getValue();

            if (libro == null) {
                throw new IllegalArgumentException(
                        "Debe seleccionar un libro."
                );
            }

            if (idUsuarioActual <= 0) {
                throw new IllegalArgumentException(
                        "No se pudo identificar al usuario."
                );
            }

            int cantidad = obtenerCantidadIngresada();

            String observacion =
                    txtObservacion.getText() == null
                            ? ""
                            : txtObservacion.getText().trim();

            boolean registrado =
                    movimientoDAO.registrarIngresoInventario(
                            libro.getIsbn(),
                            idUsuarioActual,
                            cantidad,
                            observacion
                    );

            if (registrado) {

                mostrarConfirmacion(
                        "Ingreso registrado correctamente.\n"
                        + "Libro: " + libro.getTitulo()
                        + "\nCantidad ingresada: " + cantidad
                );

                limpiarFormulario();
                cargarLibros();
            }

        } catch (IllegalArgumentException e) {

            mostrarError(e.getMessage());

        } catch (SQLException e) {

            mostrarError(
                    "No se pudo registrar el ingreso.\n"
                    + e.getMessage()
            );
        }
    }

    private void limpiarFormulario() {

        cmbLibro.getSelectionModel().clearSelection();
        txtCantidad.clear();
        txtObservacion.clear();
        lblIsbn.setText("-");
        lblStockActual.setText("0");
    }

    private void mostrarConfirmacion(String mensaje) {

        Alert alert = new Alert(
                Alert.AlertType.INFORMATION
        );

        alert.setTitle("Ingreso de Inventario");
        alert.setHeaderText("Operación exitosa");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarError(String mensaje) {

        Alert alert = new Alert(
                Alert.AlertType.ERROR
        );

        alert.setTitle("Ingreso de Inventario");
        alert.setHeaderText("No se pudo completar la operación");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}