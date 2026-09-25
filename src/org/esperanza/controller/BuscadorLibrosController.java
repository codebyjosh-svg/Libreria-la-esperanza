package org.esperanza.Controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import org.esperanza.dao.LibroDAO;
import org.esperanza.dao.impl.LibroDAOImpl;
import org.esperanza.Model.Libro;

public class BuscadorLibrosController implements Initializable {

    private static final Logger LOGGER
            = Logger.getLogger(BuscadorLibrosController.class.getName());

    // =========================
    // COMPONENTES FXML
    // =========================
    @FXML
    private ComboBox<String> cmbFiltro;

    @FXML
    private TextField txtBusqueda;

    @FXML
    private TableView<Libro> tblLibros;

    @FXML
    private TableColumn<Libro, String> colIsbn;

    @FXML
    private TableColumn<Libro, String> colTitulo;

    @FXML
    private TableColumn<Libro, String> colAutor;

    @FXML
    private TableColumn<Libro, Double> colPrecio;

    @FXML
    private TableColumn<Libro, Integer> colStock;

    // =========================
    // DAO
    // =========================
    private LibroDAO libroDAO;

    // =========================
    // LISTA DE LIBROS
    // =========================
    private final ObservableList<Libro> listaLibros
            = FXCollections.observableArrayList();

    // =========================
    // INITIALIZE
    // =========================
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        try {

            libroDAO = new LibroDAOImpl();

            configurarFiltros();

            configurarTabla();

            cargarLibros();

            configurarCierre();

            LOGGER.info(
                    "Vista BuscadorLibros cargada correctamente."
            );

        } catch (Exception e) {

            LOGGER.log(
                    Level.SEVERE,
                    "Error al inicializar BuscadorLibrosController.",
                    e
            );
        }
    }

    // =========================
    // CONFIGURAR FILTROS
    // =========================
    private void configurarFiltros() {

        cmbFiltro.getItems().clear();

        cmbFiltro.getItems().addAll(
                "ISBN",
                "Título",
                "Autor"
        );

        cmbFiltro.getSelectionModel().selectFirst();
    }

    // =========================
    // CONFIGURAR TABLA
    // =========================
    private void configurarTabla() {

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

        tblLibros.setItems(listaLibros);

        tblLibros.setPlaceholder(
                new Label("No se encontraron libros.")
        );
    }

    // =========================
    // CARGAR TODOS LOS LIBROS
    // =========================
    private void cargarLibros() {

        try {

            listaLibros.clear();

            List<Libro> libros
                    = libroDAO.listarTodos();

            if (libros != null) {
                listaLibros.addAll(libros);
            }

            LOGGER.info(
                    "Libros cargados correctamente. Total: "
                    + listaLibros.size()
            );

        } catch (Exception e) {

            LOGGER.log(
                    Level.SEVERE,
                    "Error al cargar los libros.",
                    e
            );
        }
    }

    // =========================
    // BUSCAR LIBRO
    // =========================
    @FXML
    private void buscarLibro(ActionEvent event) {

        if (libroDAO == null) {

            LOGGER.warning(
                    "El DAO de libros no está inicializado."
            );

            return;
        }

        String criterio = txtBusqueda.getText();

        if (criterio == null) {
            criterio = "";
        }

        criterio = criterio.trim();

        String filtroSeleccionado
                = cmbFiltro.getValue();

        // Si el usuario no escribió nada
        if (criterio.isEmpty()) {

            cargarLibros();

            LOGGER.info(
                    "Campo de búsqueda vacío. "
                    + "Mostrando todos los libros."
            );

            return;
        }

        // Evitar NullPointerException
        if (filtroSeleccionado == null) {

            LOGGER.warning(
                    "No se seleccionó un filtro de búsqueda."
            );

            return;
        }

        try {

            listaLibros.clear();

            switch (filtroSeleccionado) {

                case "ISBN":

                    buscarPorIsbn(criterio);

                    break;

                case "Título":

                    buscarPorTitulo(criterio);

                    break;

                case "Autor":

                    buscarPorAutor(criterio);

                    break;

                default:

                    LOGGER.warning(
                            "Filtro de búsqueda no reconocido: "
                            + filtroSeleccionado
                    );

                    break;
            }

            LOGGER.info(
                    "Búsqueda realizada. Filtro: "
                    + filtroSeleccionado
                    + " | Criterio: "
                    + criterio
                    + " | Resultados: "
                    + listaLibros.size()
            );

        } catch (Exception e) {

            LOGGER.log(
                    Level.SEVERE,
                    "Error durante la búsqueda de libros.",
                    e
            );
        }
    }

    // =========================
    // BUSCAR POR ISBN
    // =========================
    private void buscarPorIsbn(String isbn) {

        Libro libroEncontrado
                = libroDAO.buscarPorIsbn(isbn);

        if (libroEncontrado != null) {

            listaLibros.add(
                    libroEncontrado
            );
        }
    }

    // =========================
    // BUSCAR POR TÍTULO
    // =========================
    private void buscarPorTitulo(String titulo) {

        List<Libro> resultados
                = libroDAO.buscarPorTitulo(titulo);

        if (resultados != null
                && !resultados.isEmpty()) {

            listaLibros.addAll(
                    resultados
            );
        }
    }

    // =========================
    // BUSCAR POR AUTOR
    // =========================
    private void buscarPorAutor(String autor) {

        List<Libro> resultados
                = libroDAO.buscarPorAutor(autor);

        if (resultados != null
                && !resultados.isEmpty()) {

            listaLibros.addAll(
                    resultados
            );
        }
    }

    // =========================
    // LIMPIAR BÚSQUEDA
    // =========================
    @FXML
    private void limpiarBusqueda() {

        txtBusqueda.clear();

        cmbFiltro.getSelectionModel()
                .selectFirst();

        cargarLibros();

        txtBusqueda.requestFocus();
    }

    // =========================
    // CONFIGURAR CIERRE
    // =========================
    private void configurarCierre() {

        Platform.runLater(() -> {

            if (tblLibros == null
                    || tblLibros.getScene() == null
                    || tblLibros.getScene().getWindow() == null) {

                LOGGER.warning(
                        "No fue posible configurar el cierre de la ventana."
                );

                return;
            }

            if (tblLibros.getScene().getWindow() instanceof Stage) {

                Stage stage
                        = (Stage) tblLibros
                                .getScene()
                                .getWindow();

                stage.setOnCloseRequest(event -> {

                    event.consume();

                    regresarDashboard(stage);
                });
            }
        });
    }

    // =========================
    // REGRESAR AL DASHBOARD
    // =========================
    private void regresarDashboard(Stage stage) {

        if (stage == null) {
            return;
        }

        try {

            URL fxmlUrl
                    = getClass().getResource(
                            "/org/esperanza/view/DashboardAdmin.fxml"
                    );

            if (fxmlUrl == null) {

                LOGGER.severe(
                        "No se encontró DashboardAdmin.fxml."
                );

                return;
            }

            FXMLLoader loader
                    = new FXMLLoader(fxmlUrl);

            Parent root
                    = loader.load();

            stage.setOnCloseRequest(null);

            Scene scene
                    = new Scene(root);

            stage.setScene(scene);

            stage.setTitle(
                    "Librería La Esperanza - Panel Administrador"
            );

            stage.centerOnScreen();

            stage.show();

        } catch (IOException e) {

            LOGGER.log(
                    Level.SEVERE,
                    "Error al cargar DashboardAdmin.fxml.",
                    e
            );
        }
    }
}
