package org.esperanza.Controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import org.esperanza.dao.LibroDAO;
import org.esperanza.dao.impl.LibroDAOImpl;
import org.esperanza.model.Libro;

public class LibrosCriticosController implements Initializable {

    @FXML private TableView<Libro> tblLibrosCriticos;
    @FXML private TableColumn<Libro, String> colIsbn;
    @FXML private TableColumn<Libro, String> colTitulo;
    @FXML private TableColumn<Libro, Integer> colStockActual;
    @FXML private TableColumn<Libro, Integer> colStockMinimo;
    @FXML private Button btnCerrar;

    private LibroDAO libroDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            this.libroDAO = new LibroDAOImpl();
            configurarColumnas();
            cargarDatos();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void configurarColumnas() {
        colIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colStockActual.setCellValueFactory(new PropertyValueFactory<>("stockActual"));
        colStockMinimo.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));
    }

    private void cargarDatos() {
        try {
            List<Libro> listaCriticos = libroDAO.obtenerStockCritico();
            if (listaCriticos != null) {
                ObservableList<Libro> observableList = FXCollections.observableArrayList(listaCriticos);
                tblLibrosCriticos.setItems(observableList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onCerrar() {
        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        stage.close();
    }
}