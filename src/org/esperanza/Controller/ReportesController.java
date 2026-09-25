package org.esperanza.Controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
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
import org.esperanza.Model.FiltroFecha;
import org.esperanza.Model.Venta;

public class ReportesController {

    @FXML
    private ComboBox<FiltroFecha> cmbFiltroFecha;

    @FXML
    private TableView<Venta> tablaReportes;

    @FXML
    private TableColumn<Venta, Integer> colIdVenta;

    @FXML
    private TableColumn<Venta, String> colFecha;

    @FXML
    private TableColumn<Venta, String> colCliente;

    @FXML
    private TableColumn<Venta, BigDecimal> colTotal;

    @FXML
    private Label lblTotalVentas;

    private final ReporteVentasDao reportesDao =
            new ReporteVentasDao();

    private final DateTimeFormatter formatoFecha =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    private void initialize() {

        configurarFiltros();
        configurarTabla();

        lblTotalVentas.setText("Q 0.00");
    }

    private void configurarFiltros() {

        cmbFiltroFecha.setItems(
                FXCollections.observableArrayList(
                        FiltroFecha.values()
                )
        );

        cmbFiltroFecha
                .getSelectionModel()
                .selectFirst();
    }

    private void configurarTabla() {


        colIdVenta.setCellValueFactory(
                new PropertyValueFactory<>("idVenta")
        );

        colFecha.setCellValueFactory(datos -> {

            LocalDateTime fecha =
                    datos.getValue().getFechaVenta();

            if (fecha == null) {
                return new SimpleStringProperty("");
            }

            return new SimpleStringProperty(
                    fecha.format(formatoFecha)
            );
        });


        colCliente.setCellValueFactory(datos -> {

            long cui =
                    datos.getValue().getCuiCliente();

            return new SimpleStringProperty(
                    String.valueOf(cui)
            );
        });

        /*
         * Total de la venta.
         */
        colTotal.setCellValueFactory(
                new PropertyValueFactory<>("total")
        );
    }

    @FXML
    private void onGenerarReporte(ActionEvent event) {

        FiltroFecha filtro =
                cmbFiltroFecha.getValue();

        if (filtro == null) {
            mostrarError(
                    "Seleccione un período para generar el reporte."
            );
            return;
        }

        try {

            List<Venta> ventas =
                    reportesDao.obtenerVentasPorFiltro(
                            filtro
                    );

            ObservableList<Venta> listaVentas =
                    FXCollections.observableArrayList(
                            ventas
                    );

            tablaReportes.setItems(listaVentas);

            actualizarTotal(ventas);

        } catch (SQLException e) {

            mostrarError(
                    "No se pudo generar el reporte.\n"
                    + e.getMessage()
            );
        }
    }

    private void actualizarTotal(
            List<Venta> ventas) {

        BigDecimal sumaTotal =
                BigDecimal.ZERO;

        for (Venta venta : ventas) {

            if (venta.getTotal() != null) {

                sumaTotal =
                        sumaTotal.add(
                                venta.getTotal()
                        );
            }
        }

        lblTotalVentas.setText(
                "Q " + sumaTotal.setScale(2)
        );
    }

    @FXML
    private void onVolver(ActionEvent event) {

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
                    (Stage) cmbFiltroFecha
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

        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}