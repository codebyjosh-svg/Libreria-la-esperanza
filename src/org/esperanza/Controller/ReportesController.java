package org.esperanza.Controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import org.esperanza.dao.ReporteVentasDao;
import org.esperanza.model.FiltroFecha;
import org.esperanza.model.Venta;

public class ReportesController {

    @FXML private ComboBox<FiltroFecha> cmbFiltroFecha;
    
    @FXML private TableView<Venta> tablaReportes;
    @FXML private TableColumn<Venta, Integer> colIdVenta;
    @FXML private TableColumn<Venta, String> colFecha;
    @FXML private TableColumn<Venta, String> colCliente;
    @FXML private TableColumn<Venta, BigDecimal> colTotal;
    
    @FXML private Label lblTotalVentas;

    private ReporteVentasDao reportesDao = new ReporteVentasDao();

    @FXML
    private void initialize() {
        cmbFiltroFecha.setItems(FXCollections.observableArrayList(FiltroFecha.values()));
        cmbFiltroFecha.getSelectionModel().selectFirst();

        colIdVenta.setCellValueFactory(new PropertyValueFactory<>("idVenta"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("cliente"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
    }

    @FXML
    private void onGenerarReporte(ActionEvent event) {
        FiltroFecha filtro = cmbFiltroFecha.getValue();
        if (filtro == null) return;

        try {
            List<Venta> ventas = reportesDao.obtenerVentasPorFiltro(filtro);
            ObservableList<Venta> listaVentas = FXCollections.observableArrayList(ventas);
            tablaReportes.setItems(listaVentas);

   
            BigDecimal sumaTotal = BigDecimal.ZERO;
            for (Venta v : ventas) {
                if (v.getTotal() != null) {
                    sumaTotal = sumaTotal.add(v.getTotal());
                }
            }
            lblTotalVentas.setText(String.format("Q %.2f", sumaTotal.doubleValue()));

        } catch (SQLException e) {
            mostrarError("No se pudo generar el reporte:\n" + e.getMessage());
        }
    }

    @FXML
    private void onVolver(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/esperanza/view/DashboardAdmin.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) cmbFiltroFecha.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (IOException e) {
            mostrarError("No se pudo regresar al inicio:\n" + e.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}