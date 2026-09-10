package org.esperanza.controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import org.esperanza.dao.LibroDAO;
import org.esperanza.dao.impl.LibroDAOImpl;
import org.esperanza.model.Libro;

public class BuscadorLibrosController implements Initializable {

    private static final Logger LOGGER =
            Logger.getLogger(BuscadorLibrosController.class.getName());

    @FXML private ComboBox<String> cmbFiltro;
    @FXML private TextField txtBusqueda;
    @FXML private Button btnBuscar;

    @FXML private TableView<Libro> tblLibros;
    @FXML private TableColumn<Libro, String> colIsbn;
    @FXML private TableColumn<Libro, String> colTitulo;
    @FXML private TableColumn<Libro, String> colAutor;
    @FXML private TableColumn<Libro, Double> colPrecio;
    @FXML private TableColumn<Libro, Integer> colStock;

    private LibroDAO libroDAO;

    private final ObservableList<Libro> listaLibrosMaster =
            FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        libroDAO = new LibroDAOImpl();

        cmbFiltro.getItems().addAll(
                "ISBN",
                "Título",
                "Autor"
        );

        cmbFiltro.getSelectionModel().selectFirst();

        colIsbn.setCellValueFactory(
                new PropertyValueFactory<>("isbn")
        );

        colTitulo.setCellValueFactory(
                new PropertyValueFactory<>("titulo")
        );

        colAutor.setCellValueFactory(
                new PropertyValueFactory<>("nombreAutor")
        );

        colPrecio.setCellValueFactory(
                new PropertyValueFactory<>("precio")
        );

        colStock.setCellValueFactory(
                new PropertyValueFactory<>("stockActual")
        );

        listaLibrosMaster.addAll(
                libroDAO.listarTodos()
        );

        tblLibros.setItems(
                listaLibrosMaster
        );

        configurarCierre();

        LOGGER.info(
                "Vista BuscadorLibros cargada correctamente."
        );
    }

    private void configurarCierre() {

        Platform.runLater(() -> {

            Stage stage = (Stage) tblLibros
                    .getScene()
                    .getWindow();

            stage.setOnCloseRequest(event -> {

                event.consume();

                regresarDashboard(stage);
            });
        });
    }

    private void regresarDashboard(Stage stage) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/org/esperanza/view/DashboardAdmin.fxml"
                    )
            );

            Parent root = loader.load();

            stage.setOnCloseRequest(null);

            stage.setScene(
                    new Scene(root)
            );

            stage.setTitle(
                    "Librería La Esperanza - Panel Administrador"
            );

            stage.centerOnScreen();

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    @FXML
    private void buscarLibro(ActionEvent event) {

        String criterio =
                txtBusqueda.getText().trim();

        String filtroSeleccionado =
                cmbFiltro.getValue();

        if (criterio.isEmpty()) {

            listaLibrosMaster.clear();

            listaLibrosMaster.addAll(
                    libroDAO.listarTodos()
            );

            LOGGER.info(
                    "Campo de búsqueda vacío, mostrando todos los libros."
            );

            return;
        }

        listaLibrosMaster.clear();

        switch (filtroSeleccionado) {

            case "ISBN":

                Libro libroEncontrado =
                        libroDAO.buscarPorIsbn(
                                criterio
                        );

                if (libroEncontrado != null) {
                    listaLibrosMaster.add(
                            libroEncontrado
                    );
                }

                break;

            case "Título":

                List<Libro> resultadosTitulo =
                        libroDAO.buscarPorTitulo(
                                criterio
                        );

                if (resultadosTitulo != null) {
                    listaLibrosMaster.addAll(
                            resultadosTitulo
                    );
                }

                break;

            case "Autor":

                List<Libro> resultadosAutor =
                        libroDAO.buscarPorAutor(
                                criterio
                        );

                if (resultadosAutor != null) {
                    listaLibrosMaster.addAll(
                            resultadosAutor
                    );
                }

                break;

            default:

                LOGGER.warning(
                        "Filtro de búsqueda no reconocido."
                );

                break;
        }

        LOGGER.info(
                "Búsqueda ejecutada mediante DAO usando el filtro: "
                + filtroSeleccionado
        );
    }
}