package org.esperanza.Controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import org.esperanza.dao.LibroDAO;
import org.esperanza.dao.impl.LibroDAOImpl;
import org.esperanza.Model.Libro;
import org.esperanza.Service.NavegacionRol;
import org.esperanza.Service.Pantallas;

public class LibrosCriticosController
        implements Initializable {

    @FXML
    private TableView<Libro> tblLibrosCriticos;

    @FXML
    private TableColumn<Libro, String> colIsbn;

    @FXML
    private TableColumn<Libro, String> colTitulo;

    @FXML
    private TableColumn<Libro, Integer> colStockActual;

    @FXML
    private TableColumn<Libro, Integer> colStockMinimo;

    @FXML
    private Button btnCerrar;

    private final LibroDAO libroDAO =
            new LibroDAOImpl();

    @Override
    public void initialize(
            URL url,
            ResourceBundle resources) {

        if (!NavegacionRol.validarPermiso(
                "GESTION_INVENTARIO")) {
            return;
        }

        configurarColumnas();

        tblLibrosCriticos.setPlaceholder(
                new Label(
                        "No hay libros con stock crítico."
                )
        );

        btnCerrar.setText(
                "Volver al Dashboard"
        );

        cargarDatos();
    }

    private void configurarColumnas() {

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

        colStockActual.setCellValueFactory(
                new PropertyValueFactory<>(
                        "stockActual"
                )
        );

        colStockMinimo.setCellValueFactory(
                new PropertyValueFactory<>(
                        "stockMinimo"
                )
        );
    }

    private void cargarDatos() {

        try {

            List<Libro> libros =
                    libroDAO
                            .obtenerStockCritico();

            tblLibrosCriticos
                    .getItems()
                    .setAll(
                            libros == null
                                    ? List.of()
                                    : libros
                    );

        } catch (Exception ex) {

            tblLibrosCriticos.setPlaceholder(
                    new Label(
                            "No se pudo cargar el stock crítico."
                    )
            );

            Platform.runLater(
                    () -> {

                        Alert alerta =
                                new Alert(
                                        Alert.AlertType.ERROR
                                );

                        if (tblLibrosCriticos
                                .getScene() != null
                                && tblLibrosCriticos
                                        .getScene()
                                        .getWindow() != null) {

                            alerta.initOwner(
                                    tblLibrosCriticos
                                            .getScene()
                                            .getWindow()
                            );
                        }

                        alerta.setTitle(
                                "Stock crítico"
                        );

                        alerta.setHeaderText(
                                "No se pudo consultar el stock crítico"
                        );

                        alerta.setContentText(
                                ex.getMessage()
                        );

                        alerta.showAndWait();
                    }
            );
        }
    }

    @FXML
    private void onCerrar() {

        Pantallas.volver(
                tblLibrosCriticos
        );
    }
}