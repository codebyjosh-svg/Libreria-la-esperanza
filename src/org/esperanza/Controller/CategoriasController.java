package org.esperanza.Controller;

import java.io.IOException;
import java.sql.SQLException;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import org.esperanza.dao.CategoriaDao;
import org.esperanza.model.Categoria;

public class CategoriasController {

    @FXML private TextField txtIdCategoria;
    @FXML private TextField txtNombre;
    @FXML private TextArea txtDescripcion;
    
    @FXML private TableView<Categoria> tablaCategorias;
    @FXML private TableColumn<Categoria, Integer> colId;
    @FXML private TableColumn<Categoria, String> colNombre;
    @FXML private TableColumn<Categoria, String> colDescripcion;

    private CategoriaDao categoriaDao = new CategoriaDao();

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idCategoria"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        
        cargarCategorias();
    }

    private void cargarCategorias() {
        try {
            tablaCategorias.setItems(FXCollections.observableArrayList(categoriaDao.listarCategorias()));
        } catch (SQLException e) {
            mostrarError("Error al cargar la lista de categorías:\n" + e.getMessage());
        }
    }

    @FXML
    private void onGuardar() {
        String nombre = txtNombre.getText();
        String descripcion = txtDescripcion.getText();

        if (nombre == null || nombre.trim().isEmpty()) {
            mostrarError("El campo 'Nombre de la Categoría' es obligatorio.");
            return;
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(nombre.trim());
        categoria.setDescripcion(descripcion != null ? descripcion.trim() : "");

        try {
            if (txtIdCategoria.getText() == null || txtIdCategoria.getText().isEmpty()) {
                categoriaDao.insertarCategoria(categoria);
                mostrarInfo("Categoría guardada correctamente.", "Éxito");
            } else {
                categoria.setIdCategoria(Integer.parseInt(txtIdCategoria.getText()));
                categoriaDao.actualizarCategoria(categoria);
                mostrarInfo("Categoría actualizada correctamente.", "Éxito");
            }
            
            onLimpiar();
            cargarCategorias();
            
        } catch (SQLException e) {
            mostrarError("Error al guardar en la base de datos:\n" + e.getMessage());
        }
    }

    @FXML
    private void onSeleccionarCategoria() {
        Categoria seleccionada = tablaCategorias.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            txtIdCategoria.setText(String.valueOf(seleccionada.getIdCategoria()));
            txtNombre.setText(seleccionada.getNombre());
            txtDescripcion.setText(seleccionada.getDescripcion());
        }
    }

    @FXML
    private void onLimpiar() {
        txtIdCategoria.clear();
        txtNombre.clear();
        txtDescripcion.clear();
        tablaCategorias.getSelectionModel().clearSelection();
    }

    @FXML
    private void onVolver() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/esperanza/view/DashboardAdmin.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) txtNombre.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (IOException e) {
            mostrarError("No se pudo regresar al inicio:\n" + e.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de Validación");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarInfo(String mensaje, String titulo) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}