package org.esperanza.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import org.esperanza.service.SesionUsuario;
import org.esperanza.dao.LibroDAO;
import org.esperanza.dao.impl.LibroDAOImpl;
import org.esperanza.model.Libro;

public class RegistroEntradaController implements Initializable {

    @FXML private ComboBox<Libro> cmbLibros;
    @FXML private TextField txtCantidad;
    @FXML private TextArea txtObservacion;

    private LibroDAO libroDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            this.libroDAO = new LibroDAOImpl();
            cargarLibros();
            configurarComboBox();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlertaNativa("Error de Inicialización", "No se pudo inicializar el controlador: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void cargarLibros() {
        try {
            List<Libro> lista = libroDAO.listarTodos();
            if (lista != null) {
                ObservableList<Libro> observableLista = FXCollections.observableArrayList(lista);
                cmbLibros.setItems(observableLista);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlertaNativa("Error", "Error al cargar libros: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void configurarComboBox() {
        cmbLibros.setCellFactory(param -> new ListCell<Libro>() {
            @Override
            protected void updateItem(Libro libro, boolean empty) {
                super.updateItem(libro, empty);
                if (empty || libro == null) {
                    setText(null);
                } else {
                    setText(libro.getTitulo() + " (ISBN: " + libro.getIsbn() + ")");
                }
            }
        });

        cmbLibros.setButtonCell(new ListCell<Libro>() {
            @Override
            protected void updateItem(Libro libro, boolean empty) {
                super.updateItem(libro, empty);
                if (empty || libro == null) {
                    setText(null);
                } else {
                    setText(libro.getTitulo() + " (ISBN: " + libro.getIsbn() + ")");
                }
            }
        });

        cmbLibros.setConverter(new StringConverter<Libro>() {
            @Override
            public String toString(Libro libro) {
                if (libro == null) return "";
                return libro.getTitulo() + " (ISBN: " + libro.getIsbn() + ")";
            }

            @Override
            public Libro fromString(String string) {
                return null;
            }
        });
    }

    @FXML
    private void onGuardarEntrada() {
        try {
            Libro libroSeleccionado = cmbLibros.getValue();
            if (libroSeleccionado == null) {
                mostrarAlertaNativa("Aviso", "Debe seleccionar un libro del listado.", Alert.AlertType.WARNING);
                return;
            }

            String cantidadTexto = txtCantidad.getText();
            if (cantidadTexto == null || cantidadTexto.trim().isEmpty()) {
                mostrarAlertaNativa("Aviso", "Debe ingresar una cantidad.", Alert.AlertType.WARNING);
                return;
            }

            int cantidad = Integer.parseInt(cantidadTexto.trim());
            if (cantidad <= 0) {
                mostrarAlertaNativa("Aviso", "La cantidad debe ser mayor a cero.", Alert.AlertType.WARNING);
                return;
            }

            String observacion = txtObservacion.getText();
            String observacionFinal = (observacion == null || observacion.trim().isEmpty()) 
                ? "Reabastecimiento de stock" 
                : observacion.trim();

            SesionUsuario sesion = SesionUsuario.getInstancia();
            if (!sesion.haySesionActiva() || !sesion.tienePermiso("ENTRADAS_SALIDAS")) {
                mostrarAlertaNativa("Acceso denegado", "Inicie sesión con permiso para registrar entradas.", Alert.AlertType.WARNING);
                return;
            }
            int idUsuarioBodega = sesion.getUsuarioActual().getId();

            boolean exito = libroDAO.registrarMovimiento(
                libroSeleccionado.getIsbn(), 
                "INGRESO", 
                cantidad, 
                idUsuarioBodega, 
                observacionFinal
            );

            if (exito) {
                mostrarAlertaNativa("Éxito", "Entrada registrada correctamente.", Alert.AlertType.INFORMATION);
                Stage stage = (Stage) txtCantidad.getScene().getWindow();
                stage.close();
            } else {
                mostrarAlertaNativa("Error", "La base de datos rechazó el registro del movimiento.", Alert.AlertType.ERROR);
            }

        } catch (NumberFormatException e) {
            mostrarAlertaNativa("Error de Formato", "Ingrese un valor numérico válido para la cantidad.", Alert.AlertType.WARNING);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlertaNativa("Error Crítico", "Ocurrió un error inesperado: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onCancelar() {
        Stage stage = (Stage) txtCantidad.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlertaNativa(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}