package org.esperanza.controller;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import org.esperanza.Service.NavegacionRol;
import org.esperanza.dao.VentaDiaDao;
import org.esperanza.model.ResumenVentasDia;
import org.esperanza.model.VentaDia;

public class DashboardVentasDiaController {

    @FXML
    private Label lblCantidadVentas;

    @FXML
    private Label lblSubtotal;

    @FXML
    private Label lblDescuentos;

    @FXML
    private Label lblTotal;

    @FXML
    private TableView<VentaDia> tablaVentas;

    @FXML
    private TableColumn<VentaDia, Integer> colId;

    @FXML
    private TableColumn<VentaDia, String> colHora;

    @FXML
    private TableColumn<VentaDia, Long> colCui;

    @FXML
    private TableColumn<VentaDia, String> colCliente;

    @FXML
    private TableColumn<VentaDia, String> colUsuario;

    @FXML
    private TableColumn<VentaDia, BigDecimal> colSubtotal;

    @FXML
    private TableColumn<VentaDia, BigDecimal> colDescuento;

    @FXML
    private TableColumn<VentaDia, BigDecimal> colTotal;

    @FXML
    private TableColumn<VentaDia, String> colEstado;

    private final VentaDiaDao ventaDiaDao =
            new VentaDiaDao();

    @FXML
    private void initialize() {

        configurarTabla();
        cargarDatos();
        configurarCierre();
    }

    private void configurarTabla() {

        colId.setCellValueFactory(c ->
                new ReadOnlyObjectWrapper<>(
                        c.getValue().getIdVenta()
                )
        );

        colHora.setCellValueFactory(c ->
                new ReadOnlyObjectWrapper<>(
                        c.getValue().getHora()
                )
        );

        colCui.setCellValueFactory(c ->
                new ReadOnlyObjectWrapper<>(
                        c.getValue().getCuiCliente()
                )
        );

        colCliente.setCellValueFactory(c ->
                new ReadOnlyObjectWrapper<>(
                        c.getValue().getCliente()
                )
        );

        colUsuario.setCellValueFactory(c ->
                new ReadOnlyObjectWrapper<>(
                        c.getValue().getUsuario()
                )
        );

        colSubtotal.setCellValueFactory(c ->
                new ReadOnlyObjectWrapper<>(
                        c.getValue().getSubtotal()
                )
        );

        colDescuento.setCellValueFactory(c ->
                new ReadOnlyObjectWrapper<>(
                        c.getValue().getDescuento()
                )
        );

        colTotal.setCellValueFactory(c ->
                new ReadOnlyObjectWrapper<>(
                        c.getValue().getTotal()
                )
        );

        colEstado.setCellValueFactory(c ->
                new ReadOnlyObjectWrapper<>(
                        c.getValue().getEstado()
                )
        );

        tablaVentas.setPlaceholder(
                new Label(
                        "No hay ventas registradas el día de hoy."
                )
        );

        tablaVentas.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );
    }

    @FXML
    private void actualizar() {

        cargarDatos();
    }

    private void cargarDatos() {

        try {

            List<VentaDia> ventas =
                    ventaDiaDao.listarVentasDelDia();

            tablaVentas.setItems(
                    FXCollections.observableArrayList(
                            ventas
                    )
            );

            ResumenVentasDia resumen =
                    ventaDiaDao.obtenerResumenDelDia();

            lblCantidadVentas.setText(
                    String.valueOf(
                            resumen.getCantidadVentas()
                    )
            );

            lblSubtotal.setText(
                    moneda(
                            resumen.getSubtotal()
                    )
            );

            lblDescuentos.setText(
                    moneda(
                            resumen.getDescuentos()
                    )
            );

            lblTotal.setText(
                    moneda(
                            resumen.getTotal()
                    )
            );

        } catch (SQLException e) {

            e.printStackTrace();

            mostrarError(
                    "No se pudieron consultar las ventas del día.\n"
                    + e.getMessage()
            );
        }
    }

    private String moneda(BigDecimal valor) {

        if (valor == null) {
            valor = BigDecimal.ZERO;
        }

        return "Q"
                + valor.setScale(
                        2,
                        java.math.RoundingMode.HALF_UP
                ).toPlainString();
    }

    private void configurarCierre() {

        Platform.runLater(() -> {

            if (tablaVentas.getScene() == null
                    || tablaVentas
                            .getScene()
                            .getWindow() == null) {

                return;
            }

            Stage stage =
                    (Stage) tablaVentas
                            .getScene()
                            .getWindow();

            stage.setOnCloseRequest(event -> {

                event.consume();
                regresarDashboard();
            });
        });
    }

    @FXML
    private void regresarDashboard() {

        Stage stage =
                (Stage) tablaVentas
                        .getScene()
                        .getWindow();

        stage.setOnCloseRequest(null);

        NavegacionRol
                .abrirDashboardSegunRol(
                        stage
                );
    }

    private void mostrarError(String mensaje) {

        Alert alert = new Alert(
                Alert.AlertType.ERROR
        );

        alert.setTitle(
                "Error"
        );

        alert.setHeaderText(
                null
        );

        alert.setContentText(
                mensaje
        );

        alert.showAndWait();
    }
}