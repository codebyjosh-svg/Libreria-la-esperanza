package org.esperanza.Controller;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import org.esperanza.dao.LibroDAO;
import org.esperanza.dao.impl.LibroDAOImpl;
import org.esperanza.model.Libro;

public class ActualizarPrecioController implements Initializable {

    @FXML
    private TableView<Libro> tablaLibros;

    @FXML
    private TableColumn<Libro, String> colIsbn;

    @FXML
    private TableColumn<Libro, String> colTitulo;

    @FXML
    private TableColumn<Libro, Double> colPrecioActual;

    @FXML
    private TextField txtBuscarLibro;

    @FXML
    private TextField txtIsbn;

    @FXML
    private TextField txtTitulo;

    @FXML
    private TextField txtPrecioActual;

    @FXML
    private TextField txtNuevoPrecio;

    private final ObservableList<Libro> libros =
            FXCollections.observableArrayList();

    private final LibroDAO libroDAO =
            new LibroDAOImpl();

    private Libro libroSeleccionado;

    @Override
    public void initialize(
            URL url,
            ResourceBundle resourceBundle) {

        configurarTabla();
        cargarLibros();
        configurarSeleccion();
    }

    private void configurarTabla() {

        colIsbn.setCellValueFactory(
                new PropertyValueFactory<>("isbn")
        );

        colTitulo.setCellValueFactory(
                new PropertyValueFactory<>("titulo")
        );

        colPrecioActual.setCellValueFactory(
                new PropertyValueFactory<>("precio")
        );

        tablaLibros.setItems(libros);
    }

    private void cargarLibros() {

        libros.clear();

        List<Libro> lista =
                libroDAO.listarTodos();

        if (lista == null) {
            return;
        }

        for (Libro libro : lista) {

            if (libro.isActivo()) {
                libros.add(libro);
            }
        }
    }

    private void configurarSeleccion() {

        tablaLibros.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable,
                         anterior,
                         seleccionado) -> {

                            if (seleccionado != null) {
                                cargarLibroSeleccionado(
                                        seleccionado
                                );
                            }
                        }
                );
    }

    private void cargarLibroSeleccionado(
            Libro libro) {

        libroSeleccionado = libro;

        txtIsbn.setText(
                libro.getIsbn()
        );

        txtTitulo.setText(
                libro.getTitulo()
        );

        txtPrecioActual.setText(
                String.format(
                        "Q%.2f",
                        libro.getPrecio()
                )
        );

        txtNuevoPrecio.clear();
    }

    @FXML
    private void onActualizarPrecioClick() {

        if (libroSeleccionado == null) {

            mostrarAdvertencia(
                    "Debes seleccionar un libro."
            );

            return;
        }

        String textoPrecio =
                txtNuevoPrecio.getText();

        if (textoPrecio == null
                || textoPrecio.trim().isEmpty()) {

            mostrarAdvertencia(
                    "Debes ingresar el nuevo precio."
            );

            return;
        }

        BigDecimal nuevoPrecio;

        try {

            nuevoPrecio =
                    new BigDecimal(
                            textoPrecio.trim()
                    );

        } catch (NumberFormatException ex) {

            mostrarAdvertencia(
                    "El precio debe ser un valor numérico."
            );

            return;
        }

        if (nuevoPrecio.compareTo(
                BigDecimal.ZERO) <= 0) {

            mostrarAdvertencia(
                    "El nuevo precio debe ser mayor que Q0.00."
            );

            return;
        }

        boolean actualizado =
                libroDAO.actualizarPrecio(
                        libroSeleccionado.getIsbn(),
                        nuevoPrecio
                );

        if (!actualizado) {

            mostrarError(
                    "No se pudo actualizar el precio del libro."
            );

            return;
        }

        libroSeleccionado.setPrecio(
                nuevoPrecio.doubleValue()
        );

        tablaLibros.refresh();

        txtPrecioActual.setText(
                String.format(
                        "Q%.2f",
                        libroSeleccionado.getPrecio()
                )
        );

        txtNuevoPrecio.clear();

        mostrarInformacion(
                "Precio actualizado correctamente."
        );
    }

    private void mostrarAdvertencia(
            String mensaje) {

        Alert alerta =
                new Alert(
                        Alert.AlertType.WARNING
                );

        alerta.setTitle(
                "Actualización de Precio"
        );

        alerta.setHeaderText(
                "Valor no válido"
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

        alerta.setTitle(
                "Actualización de Precio"
        );

        alerta.setHeaderText(
                "Error"
        );

        alerta.setContentText(
                mensaje
        );

        alerta.showAndWait();
    }

    private void mostrarInformacion(
            String mensaje) {

        Alert alerta =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alerta.setTitle(
                "Actualización de Precio"
        );

        alerta.setHeaderText(
                "Actualización correcta"
        );

        alerta.setContentText(
                mensaje
        );

        alerta.showAndWait();
    }
}