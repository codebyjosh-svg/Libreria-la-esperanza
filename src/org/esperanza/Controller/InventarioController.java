package org.esperanza.controller;

import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import org.esperanza.dao.LibroDAO;
import org.esperanza.dao.impl.LibroDAOImpl;
import org.esperanza.model.Libro;
import org.esperanza.service.Pantallas;

public class InventarioController implements Initializable {

    @FXML private TextField txtBuscar;
    @FXML private Label lblTotal;
    @FXML private TableView<Libro> tablaInventario;

    @FXML private TableColumn<Libro, String> colIsbn;
    @FXML private TableColumn<Libro, String> colTitulo;
    @FXML private TableColumn<Libro, Double> colPrecio;
    @FXML private TableColumn<Libro, Integer> colStockActual;
    @FXML private TableColumn<Libro, Integer> colStockMinimo;
    @FXML private TableColumn<Libro, String> colEstado;

    private final LibroDAO libroDAO = new LibroDAOImpl();

    private final ObservableList<Libro> listaLibros =
            FXCollections.observableArrayList();

    private final FilteredList<Libro> librosFiltrados =
            new FilteredList<>(listaLibros, libro -> true);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTabla();
        tablaInventario.setItems(librosFiltrados);
        configurarBusqueda();
        cargarInventario();
    }

    private void configurarTabla() {
        colIsbn.setCellValueFactory(
                new PropertyValueFactory<>("isbn")
        );
        colTitulo.setCellValueFactory(
                new PropertyValueFactory<>("titulo")
        );
        colPrecio.setCellValueFactory(
                new PropertyValueFactory<>("precio")
        );
        colStockActual.setCellValueFactory(
                new PropertyValueFactory<>("stockActual")
        );
        colStockMinimo.setCellValueFactory(
                new PropertyValueFactory<>("stockMinimo")
        );

        colEstado.setCellValueFactory(
                datos -> new ReadOnlyStringWrapper(
                        obtenerEstado(datos.getValue())
                )
        );

        tablaInventario.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        tablaInventario.setRowFactory(tabla -> new TableRow<>() {
            @Override
            protected void updateItem(Libro libro, boolean empty) {
                super.updateItem(libro, empty);

                if (empty || libro == null) {
                    setStyle("");
                } else if (!libro.isActivo()) {
                    setStyle("-fx-background-color:#f1f5f9;");
                } else if (libro.esStockCritico()) {
                    setStyle("-fx-background-color:#fff1f2;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void configurarBusqueda() {
        txtBuscar.textProperty().addListener(
                (observable, anterior, nuevo) ->
                        filtrarInventario(nuevo)
        );
    }

    private void cargarInventario() {
        try {
            List<Libro> libros = libroDAO.listarTodos();

            listaLibros.clear();

            if (libros != null) {
                listaLibros.addAll(libros);
            }

            actualizarTotal();

        } catch (Exception ex) {
            mostrarError(
                    "No se pudo cargar el inventario.\n"
                    + ex.getMessage()
            );
        }
    }

    private void filtrarInventario(String texto) {
        String busqueda = texto == null
                ? ""
                : texto.trim().toLowerCase(Locale.ROOT);

        librosFiltrados.setPredicate(libro -> {
            if (busqueda.isEmpty()) {
                return true;
            }

            String isbn = libro.getIsbn() == null
                    ? "" : libro.getIsbn();

            String titulo = libro.getTitulo() == null
                    ? "" : libro.getTitulo();

            return isbn.toLowerCase(Locale.ROOT).contains(busqueda)
                    || titulo.toLowerCase(Locale.ROOT).contains(busqueda)
                    || String.valueOf(libro.getPrecio()).contains(busqueda)
                    || String.valueOf(libro.getStockActual()).contains(busqueda)
                    || String.valueOf(libro.getStockMinimo()).contains(busqueda)
                    || obtenerEstado(libro)
                            .toLowerCase(Locale.ROOT)
                            .contains(busqueda);
        });

        actualizarTotal();
    }

    private String obtenerEstado(Libro libro) {
        if (!libro.isActivo()) {
            return "Inactivo";
        }

        return libro.esStockCritico() ? "Crítico" : "Normal";
    }

    private void actualizarTotal() {
        if (lblTotal != null) {
            lblTotal.setText(
                    "Mostrando " + librosFiltrados.size()
                    + " de " + listaLibros.size() + " libros"
            );
        }
    }

    @FXML
    private void onActualizar() {
        cargarInventario();
        filtrarInventario(txtBuscar.getText());
    }

    @FXML
    private void onVolver() {
        Pantallas.volver(txtBuscar);
    }

    private void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Gestión de Inventario");
        alerta.setHeaderText("Ocurrió un problema");
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}