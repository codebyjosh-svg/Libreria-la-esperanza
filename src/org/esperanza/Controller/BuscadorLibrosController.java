package org.esperanza.Controller;

import org.esperanza.Dao.LibroDAO;
import org.esperanza.Dao.Impl.LibroDAOImpl;
import org.esperanza.Model.Libro;
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

public class BuscadorLibrosController implements Initializable {

    @FXML private ComboBox<String> cmbFiltro;
    @FXML private TextField txtBusqueda;
    @FXML private Button btnBuscar;

    @FXML private TableView<Libro> tblLibros;
    @FXML private TableColumn<Libro, String> colIsbn;
    @FXML private TableColumn<Libro, String> colTitulo;
    @FXML private TableColumn<Libro, Double> colPrecio; // Corregido de String a Double para que coincida con el modelo
    @FXML private TableColumn<Libro, Integer> colStock;

    private LibroDAO libroDAO;
    private ObservableList<Libro> listaLibrosObservable;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        libroDAO = new LibroDAOImpl();
        
        cmbFiltro.getItems().addAll("ISBN", "Título", "Autor");
        cmbFiltro.getSelectionModel().selectFirst();

        colIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockActual"));

        cargarTodosLosLibros();
    }

    @FXML
    private void buscarLibro(ActionEvent event) {
        String criterio = txtBusqueda.getText().trim();
        String filtro = cmbFiltro.getValue();
        listaLibrosObservable = FXCollections.observableArrayList();

        if (criterio.isEmpty()) {
            cargarTodosLosLibros();
            return;
        }

        switch (filtro) {
            case "ISBN":
                Libro libro = libroDAO.buscarPorIsbn(criterio);
                if (libro != null) {
                    listaLibrosObservable.add(libro);
                }
                break;
            case "Título":
                List<Libro> porTitulo = libroDAO.buscarPorTitulo(criterio);
                listaLibrosObservable.addAll(porTitulo);
                break;
            case "Autor":
                List<Libro> porAutor = libroDAO.buscarPorAutor(criterio);
                listaLibrosObservable.addAll(porAutor);
                break;
        }

        tblLibros.setItems(listaLibrosObservable);
    }

    private void cargarTodosLosLibros() {
        List<Libro> todos = libroDAO.listarTodos();
        listaLibrosObservable = FXCollections.observableArrayList(todos);
        tblLibros.setItems(listaLibrosObservable);
    }
}