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

    @FXML
    private TextField txtIdCategoria;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextArea txtDescripcion;

    @FXML
    private TableView<Categoria> tablaCategorias;

    @FXML
    private TableColumn<Categoria, Integer> colId;

    @FXML
    private TableColumn<Categoria, String> colNombre;

    @FXML
    private TableColumn<Categoria, String> colDescripcion;

    private final CategoriaDao categoriaDao =
            new CategoriaDao();

    @FXML
    private void initialize() {

        colId.setCellValueFactory(
                new PropertyValueFactory<>("idCategoria")
        );

        colNombre.setCellValueFactory(
                new PropertyValueFactory<>("nombre")
        );

        colDescripcion.setCellValueFactory(
                new PropertyValueFactory<>("descripcion")
        );

        txtIdCategoria.setEditable(false);

        cargarCategorias();
    }

    private void cargarCategorias() {

        try {

            tablaCategorias.setItems(
                    FXCollections.observableArrayList(
                            categoriaDao.listarCategorias()
                    )
            );

        } catch (SQLException e) {

            mostrarError(
                    "Error al cargar las categorías.\n"
                    + e.getMessage()
            );
        }
    }

    @FXML
    private void onGuardar() {

        String nombre =
                txtNombre.getText() == null
                ? ""
                : txtNombre.getText().trim();

        String descripcion =
                txtDescripcion.getText() == null
                ? ""
                : txtDescripcion.getText().trim();

        if (nombre.isEmpty()) {

            mostrarError(
                    "El nombre de la categoría es obligatorio."
            );

            txtNombre.requestFocus();
            return;
        }

        try {

            Integer idCategoria = null;

            if (txtIdCategoria.getText() != null
                    && !txtIdCategoria
                            .getText()
                            .trim()
                            .isEmpty()) {

                idCategoria = Integer.parseInt(
                        txtIdCategoria
                                .getText()
                                .trim()
                );
            }

            if (categoriaDao.existeNombre(
                    nombre,
                    idCategoria)) {

                mostrarError(
                        "Ya existe una categoría con ese nombre."
                );

                txtNombre.requestFocus();
                return;
            }

            Categoria categoria =
                    new Categoria();

            categoria.setNombre(nombre);
            categoria.setDescripcion(descripcion);

            if (idCategoria == null) {

                categoriaDao.insertarCategoria(
                        categoria
                );

                mostrarInfo(
                        "Categoría guardada correctamente.",
                        "Categoría"
                );

            } else {

                categoria.setIdCategoria(
                        idCategoria
                );

                categoriaDao.actualizarCategoria(
                        categoria
                );

                mostrarInfo(
                        "Categoría actualizada correctamente.",
                        "Categoría"
                );
            }

            onLimpiar();
            cargarCategorias();

        } catch (NumberFormatException e) {

            mostrarError(
                    "El ID de la categoría no es válido."
            );

        } catch (SQLException e) {

            mostrarError(
                    "Error al guardar la categoría.\n"
                    + e.getMessage()
            );
        }
    }

    @FXML
    private void onSeleccionarCategoria() {

        Categoria seleccionada =
                tablaCategorias
                        .getSelectionModel()
                        .getSelectedItem();

        if (seleccionada == null) {
            return;
        }

        txtIdCategoria.setText(
                String.valueOf(
                        seleccionada.getIdCategoria()
                )
        );

        txtNombre.setText(
                seleccionada.getNombre()
        );

        txtDescripcion.setText(
                seleccionada.getDescripcion() == null
                ? ""
                : seleccionada.getDescripcion()
        );
    }

    @FXML
    private void onLimpiar() {

        txtIdCategoria.clear();
        txtNombre.clear();
        txtDescripcion.clear();

        tablaCategorias
                .getSelectionModel()
                .clearSelection();

        txtNombre.requestFocus();
    }

    @FXML
    private void onVolver() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/org/esperanza/view/"
                                    + "DashboardAdmin.fxml"
                            )
                    );

            Parent root =
                    loader.load();

            Stage stage =
                    (Stage) txtNombre
                            .getScene()
                            .getWindow();

            stage.setScene(
                    new Scene(root)
            );

            stage.setTitle(
                    "Dashboard Administrativo"
            );

            stage.centerOnScreen();

        } catch (IOException e) {

            mostrarError(
                    "No se pudo regresar al Dashboard.\n"
                    + e.getMessage()
            );
        }
    }

    private void mostrarError(
            String mensaje) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle(
                "Error"
        );

        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarInfo(
            String mensaje,
            String titulo) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}