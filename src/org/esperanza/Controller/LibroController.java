package org.esperanza.controller;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import org.esperanza.dao.LibroDAO;
import org.esperanza.dao.impl.LibroDAOImpl;
import org.esperanza.model.Libro;

public class LibroController {

    @FXML private TextField txtIsbn, txtTitulo, txtPrecio, txtIdCategoria, txtNitEditorial, txtIdProveedor, txtStockActual, txtStockMinimo;
    @FXML private DatePicker dpFecha;
    @FXML private TableView<Libro> tbLibros;
    @FXML private TableColumn<Libro, String> colIsbn, colTitulo;
    @FXML private TableColumn<Libro, Double> colPrecio;
    @FXML private TableColumn<Libro, Integer> colStock;
    @FXML private TableColumn<Libro, Boolean> colActivo;
    
    private LibroDAO libroDao = new LibroDAOImpl();
    private boolean modoEdicion = false;

    @FXML
    public void initialize() {
        colIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockActual"));
        colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));
        cargarTabla();
    }

    private void cargarTabla() {
        List<Libro> lista = libroDao.listarTodos();
        ObservableList<Libro> obsLibros = FXCollections.observableArrayList(lista);
        tbLibros.setItems(obsLibros);
    }

    @FXML
    void guardarLibro(ActionEvent event) {
        // T3.4.15, 16, 17: Validaciones
        if (txtIsbn.getText().isEmpty() || txtTitulo.getText().isEmpty() || txtPrecio.getText().isEmpty()) {
            mostrarAlerta("Error", "Los campos ISBN, Título y Precio son obligatorios.");
            return;
        }

        try {
            double precio = Double.parseDouble(txtPrecio.getText());
            if (precio <= 0) {
                mostrarAlerta("Error", "El precio debe ser mayor a 0.");
                return;
            }

            int stockAct = txtStockActual.getText().isEmpty() ? 0 : Integer.parseInt(txtStockActual.getText());
            int stockMin = txtStockMinimo.getText().isEmpty() ? 0 : Integer.parseInt(txtStockMinimo.getText());
            if (stockMin < 0 || stockAct < 0) {
                mostrarAlerta("Error", "El stock no puede ser negativo.");
                return;
            }

            Libro libro = new Libro();
            libro.setIsbn(txtIsbn.getText());
            libro.setTitulo(txtTitulo.getText());
            libro.setPrecio(precio);
            
            if (dpFecha.getValue() != null) {
                libro.setFechaPublicacion(Date.valueOf(dpFecha.getValue()));
            } else {
                libro.setFechaPublicacion(Date.valueOf(LocalDate.now())); // Default
            }

            libro.setIdCategoria(txtIdCategoria.getText().isEmpty() ? 1 : Integer.parseInt(txtIdCategoria.getText()));
            libro.setNitEditorial(txtNitEditorial.getText().isEmpty() ? "1001-A" : txtNitEditorial.getText());
            libro.setIdProveedor(txtIdProveedor.getText().isEmpty() ? 1 : Integer.parseInt(txtIdProveedor.getText()));
            libro.setStockActual(stockAct);
            libro.setStockMinimo(stockMin);

            // T3.4.12 (Alta) y T3.4.13 (Edición)
            boolean exito;
            if (modoEdicion) {
                exito = libroDao.actualizar(libro);
            } else {
                exito = libroDao.insertar(libro);
            }

            if (exito) {
                mostrarAlerta("Éxito", "Libro guardado correctamente.");
                limpiarFormulario();
                cargarTabla();
            } else {
                mostrarAlerta("Error", "No se pudo guardar en la base de datos.");
            }

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "El precio, stocks y campos ID deben ser valores numéricos válidos.");
        }
    }

    @FXML
    void seleccionarLibro(MouseEvent event) {
        Libro seleccionado = tbLibros.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            modoEdicion = true;
            txtIsbn.setText(seleccionado.getIsbn());
            txtIsbn.setDisable(true); // El ISBN (Primary Key) no debe editarse[cite: 3]
            txtTitulo.setText(seleccionado.getTitulo());
            txtPrecio.setText(String.valueOf(seleccionado.getPrecio()));
            txtIdCategoria.setText(String.valueOf(seleccionado.getIdCategoria()));
            txtNitEditorial.setText(seleccionado.getNitEditorial());
            txtIdProveedor.setText(String.valueOf(seleccionado.getIdProveedor()));
            txtStockActual.setText(String.valueOf(seleccionado.getStockActual()));
            txtStockMinimo.setText(String.valueOf(seleccionado.getStockMinimo()));
            
            if (seleccionado.getFechaPublicacion() != null) {
                dpFecha.setValue(seleccionado.getFechaPublicacion().toLocalDate());
            }
        }
    }

    @FXML
    void desactivarLibro(ActionEvent event) {
        // T3.4.14: Activar/Desactivar (Baja lógica)
        if (txtIsbn.getText().isEmpty()) {
            mostrarAlerta("Atención", "Seleccione un libro de la tabla para desactivar.");
            return;
        }
        
        boolean exito = libroDao.eliminar(txtIsbn.getText());
        if (exito) {
            mostrarAlerta("Éxito", "El libro se ha desactivado (baja lógica).");
            limpiarFormulario();
            cargarTabla();
        } else {
            mostrarAlerta("Error", "No se pudo desactivar el libro.");
        }
    }

    @FXML
    void limpiarFormulario() {
        modoEdicion = false;
        txtIsbn.setDisable(false);
        txtIsbn.clear();
        txtTitulo.clear();
        txtPrecio.clear();
        dpFecha.setValue(null);
        txtIdCategoria.clear();
        txtNitEditorial.clear();
        txtIdProveedor.clear();
        txtStockActual.clear();
        txtStockMinimo.clear();
    }

    private void mostrarAlerta(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}