package org.esperanza.Controller;

import org.esperanza.Dao.LibroDAO;
import org.esperanza.Dao.Impl.LibroDAOImpl;
import org.esperanza.Model.Libro;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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
    @FXML private TableColumn<Libro, String> colAutor; // <--- Referencia al Autor
    @FXML private TableColumn<Libro, Double> colPrecio;
    @FXML private TableColumn<Libro, Integer> colStock;

    private LibroDAO libroDAO;
    private ObservableList<Libro> listaLibrosMaster = FXCollections.observableArrayList();
    private FilteredList<Libro> filteredData;

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

        filteredData = new FilteredList<>(listaLibrosMaster, b -> true);

        txtBusqueda.textProperty().addListener((observable, oldValue, newValue) -> {
            String filtroSeleccionado = cmbFiltro.getValue();
            
            if ("Autor".equals(filtroSeleccionado)) {
                return; 
            }

            filteredData.setPredicate(libro -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                
                String lowerCaseFilter = newValue.toLowerCase();

                if ("ISBN".equals(filtroSeleccionado)) {
                    return libro.getIsbn() != null && libro.getIsbn().toLowerCase().contains(lowerCaseFilter);
                } else if ("Título".equals(filtroSeleccionado)) {
                    return libro.getTitulo() != null && libro.getTitulo().toLowerCase().contains(lowerCaseFilter);
                }
                
                return false;
            });
        });

        SortedList<Libro> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tblLibros.comparatorProperty());
        tblLibros.setItems(sortedData);
        
      
    }

    @FXML
    private void buscarLibro(ActionEvent event) {
        String criterio = txtBusqueda.getText().trim();
        String filtro = cmbFiltro.getValue();

        if (criterio.isEmpty()) {
            listaLibrosMaster.clear();
            listaLibrosMaster.addAll(libroDAO.listarTodos());
            filteredData.setPredicate(b -> true);
            return;
        }

        if ("Autor".equals(filtro)) {
            List<Libro> resultadosAutor = libroDAO.buscarPorAutor(criterio);
            listaLibrosMaster.clear();
            listaLibrosMaster.addAll(resultadosAutor);
            filteredData.setPredicate(b -> true);
        }
    }
}