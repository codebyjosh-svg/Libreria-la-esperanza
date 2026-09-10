package org.esperanza.controller;

import org.esperanza.dao.LibroDAO;
import org.esperanza.dao.impl.LibroDAOImpl;
import org.esperanza.model  .Libro;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class BuscadorLibrosController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(BuscadorLibrosController.class.getName());

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
    private ObservableList<Libro> listaLibrosMaster = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        libroDAO = new LibroDAOImpl();

        cmbFiltro.getItems().addAll("ISBN", "Título", "Autor");
        cmbFiltro.getSelectionModel().selectFirst();

        colIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colAutor.setCellValueFactory(new PropertyValueFactory<>("nombreAutor")); 
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockActual"));

        listaLibrosMaster.addAll(libroDAO.listarTodos());
        tblLibros.setItems(listaLibrosMaster);

        LOGGER.info("Vista BuscadorLibros cargada correctamente.");
    }

    @FXML
    private void buscarLibro(ActionEvent event) {
        String criterio = txtBusqueda.getText().trim();
        String filtroSeleccionado = cmbFiltro.getValue();

        if (criterio.isEmpty()) {
            listaLibrosMaster.clear();
            listaLibrosMaster.addAll(libroDAO.listarTodos());
            LOGGER.info("Campo de búsqueda vacío, mostrando todos los libros.");
            return;
        }

        listaLibrosMaster.clear();

        switch (filtroSeleccionado) {
            case "ISBN":
                Libro libroEncontrado = libroDAO.buscarPorIsbn(criterio);
                if (libroEncontrado != null) {
                    listaLibrosMaster.add(libroEncontrado);
                }
                break;
            case "Título":
                List<Libro> resultadosTitulo = libroDAO.buscarPorTitulo(criterio);
                if (resultadosTitulo != null) {
                    listaLibrosMaster.addAll(resultadosTitulo);
                }
                break;
            case "Autor":
                List<Libro> resultadosAutor = libroDAO.buscarPorAutor(criterio);
                if (resultadosAutor != null) {
                    listaLibrosMaster.addAll(resultadosAutor);
                }
                break;
            default:
                LOGGER.warning("Filtro de búsqueda no reconocido.");
                break;
        }

        LOGGER.info("Búsqueda ejecutada mediante DAO usando el filtro: " + filtroSeleccionado);
    }
}