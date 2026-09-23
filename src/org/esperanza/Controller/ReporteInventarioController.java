package org.esperanza.controller;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import org.esperanza.dao.ReporteInventarioDao;
import org.esperanza.Model.LibroMasVendido;

public class ReporteInventarioController implements Initializable {

    @FXML
    private TableView<LibroMasVendido> tablaLibrosMasVendidos;

    @FXML
    private TableColumn<LibroMasVendido, String> colRankingIsbn;

    @FXML
    private TableColumn<LibroMasVendido, String> colRankingTitulo;

    @FXML
    private TableColumn<LibroMasVendido, Integer> colCantidadVendida;

    @FXML
    private TableColumn<LibroMasVendido, BigDecimal> colTotalVendido;

    private final ObservableList<LibroMasVendido> ranking =
            FXCollections.observableArrayList();

    private final ReporteInventarioDao reporteDao =
            new ReporteInventarioDao();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurarTablaRanking();
        cargarRanking();
    }

    private void configurarTablaRanking() {

        colRankingIsbn.setCellValueFactory(
                new PropertyValueFactory<>("isbn")
        );

        colRankingTitulo.setCellValueFactory(
                new PropertyValueFactory<>("titulo")
        );

        colCantidadVendida.setCellValueFactory(
                new PropertyValueFactory<>("cantidadVendida")
        );

        colTotalVendido.setCellValueFactory(
                new PropertyValueFactory<>("totalVendido")
        );

        tablaLibrosMasVendidos.setItems(ranking);
    }

    private void cargarRanking() {

        try {

            List<LibroMasVendido> libros =
                    reporteDao.listarLibrosMasVendidos();

            ranking.clear();

            if (libros != null) {
                ranking.addAll(libros);
            }

        } catch (SQLException ex) {

            mostrarError(
                    "No se pudo cargar el ranking de libros.\n"
                    + ex.getMessage()
            );
        }
    }

    private void mostrarError(String mensaje) {

        Alert alerta = new Alert(Alert.AlertType.ERROR);

        alerta.setTitle("Reportes de Inventario");
        alerta.setHeaderText("No se pudo cargar el reporte");
        alerta.setContentText(mensaje);

        alerta.showAndWait();
    }
}