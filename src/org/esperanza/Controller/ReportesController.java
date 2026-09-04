package org.esperanza.Controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.esperanza.Dao.AuditoriaDao;
import org.esperanza.Service.SesionUsuario;

public class ReportesController {
    @FXML private TableView<AuditoriaDao.ReporteUso> tablaReportes;
    @FXML private TableColumn<AuditoriaDao.ReporteUso,String> colUsuario;
    @FXML private TableColumn<AuditoriaDao.ReporteUso,String> colTiempo;
    @FXML private TableColumn<AuditoriaDao.ReporteUso,Integer> colCambios;
    @FXML private TableColumn<AuditoriaDao.ReporteUso,String> colUltima;
    @FXML private TableColumn<AuditoriaDao.ReporteUso,String> colDetalle;
    @FXML private Label lblResumen;

    private final AuditoriaDao auditoriaDao = new AuditoriaDao();

    @FXML
    private void initialize() {
        colUsuario.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getUsername()));
        colTiempo.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getTiempoTotal()));
        colCambios.setCellValueFactory(d -> new javafx.beans.property.SimpleIntegerProperty(d.getValue().getCambios()).asObject());
        colUltima.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getUltimaActividad() == null ? "Sin actividad" : d.getValue().getUltimaActividad().toString()));
        colDetalle.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getCambiosTexto()));
        colDetalle.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setWrapText(true);
            }
        });
        cargar();
    }

    @FXML private void onActualizar() { cargar(); }

    private void cargar() {
        var datos = auditoriaDao.obtenerReporte();
        tablaReportes.setItems(FXCollections.observableArrayList(datos));
        long totalCambios = datos.stream().mapToLong(AuditoriaDao.ReporteUso::getCambios).sum();
        lblResumen.setText("Usuarios: " + datos.size() + "   |   Cambios registrados: " + totalCambios
                + "   |   El tiempo incluye la sesión que está activa.");
    }
}
