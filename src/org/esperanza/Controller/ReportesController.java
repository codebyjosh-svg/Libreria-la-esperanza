package org.esperanza.Controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import org.esperanza.dao.AuditoriaDao;

public class ReportesController {

    @FXML
    private TableView<AuditoriaDao.ReporteUso>
            tablaReportes;

    @FXML
    private TableColumn<
            AuditoriaDao.ReporteUso,
            String
            > colUsuario;

    @FXML
    private TableColumn<
            AuditoriaDao.ReporteUso,
            String
            > colTiempo;

    @FXML
    private TableColumn<
            AuditoriaDao.ReporteUso,
            Integer
            > colCambios;

    @FXML
    private TableColumn<
            AuditoriaDao.ReporteUso,
            String
            > colUltima;

    @FXML
    private TableColumn<
            AuditoriaDao.ReporteUso,
            String
            > colDetalle;

    @FXML
    private Label lblResumen;

    private final AuditoriaDao auditoriaDao =
            new AuditoriaDao();

    @FXML
    private void initialize() {

        colUsuario.setCellValueFactory(
                dato ->
                        new SimpleStringProperty(
                                dato
                                        .getValue()
                                        .getUsername()
                        )
        );

        colTiempo.setCellValueFactory(
                dato ->
                        new SimpleStringProperty(
                                dato
                                        .getValue()
                                        .getTiempoTotal()
                        )
        );

        colCambios.setCellValueFactory(
                dato ->
                        new SimpleIntegerProperty(
                                dato
                                        .getValue()
                                        .getCambios()
                        ).asObject()
        );

        colUltima.setCellValueFactory(
                dato ->
                        new SimpleStringProperty(
                                dato
                                                .getValue()
                                                .getUltimaActividad()
                                        == null
                                        ? "Sin actividad"
                                        : dato
                                                .getValue()
                                                .getUltimaActividad()
                                                .toString()
                        )
        );

        colDetalle.setCellValueFactory(
                dato ->
                        new SimpleStringProperty(
                                dato
                                        .getValue()
                                        .getCambiosTexto()
                        )
        );

        colDetalle.setCellFactory(
                columna ->
                        new TableCell<>() {

                    @Override
                    protected void updateItem(
                            String item,
                            boolean empty) {

                        super.updateItem(
                                item,
                                empty
                        );

                        setText(
                                empty
                                        ? null
                                        : item
                        );

                        setWrapText(
                                true
                        );
                    }
                }
        );

        cargar();
    }

    @FXML
    private void onActualizar() {
        cargar();
    }

    private void cargar() {

        var datos =
                auditoriaDao
                        .obtenerReporte();

        tablaReportes.setItems(
                FXCollections
                        .observableArrayList(
                                datos
                        )
        );

        long totalCambios =
                datos
                        .stream()
                        .mapToLong(
                                AuditoriaDao
                                        .ReporteUso
                                        ::getCambios
                        )
                        .sum();

        lblResumen.setText(
                "Usuarios: "
                + datos.size()
                + "   |   Cambios registrados: "
                + totalCambios
                + "   |   El tiempo incluye "
                + "la sesión que está activa."
        );
    }
}