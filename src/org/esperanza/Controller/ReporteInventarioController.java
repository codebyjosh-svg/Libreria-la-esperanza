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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import javafx.scene.control.DatePicker;

import org.esperanza.dao.ReporteInventarioDao;
import org.esperanza.Model.LibroMasVendido;
import org.esperanza.Model.StockValorizado;

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

    private final ObservableList<LibroMasVendido> ranking
            = FXCollections.observableArrayList();

    @FXML
    private TableView<StockValorizado> tablaStockValorizado;

    @FXML
    private TableColumn<StockValorizado, String> colStockIsbn;

    @FXML
    private TableColumn<StockValorizado, String> colStockTitulo;

    @FXML
    private TableColumn<StockValorizado, Integer> colStockActual;

    @FXML
    private TableColumn<StockValorizado, BigDecimal> colPrecio;

    @FXML
    private TableColumn<StockValorizado, BigDecimal> colValorInventario;

    @FXML
    private Label lblValorTotalInventario;

    @FXML
    private DatePicker dpFechaInicio;

    @FXML
    private DatePicker dpFechaFin;

    private final ObservableList<StockValorizado> stockValorizado
            = FXCollections.observableArrayList();

    private final ReporteInventarioDao reporteDao
            = new ReporteInventarioDao();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        configurarTablaRanking();
        configurarTablaStock();

        cargarRanking();
        cargarStockValorizado();
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

    private void configurarTablaStock() {

        colStockIsbn.setCellValueFactory(
                new PropertyValueFactory<>("isbn")
        );

        colStockTitulo.setCellValueFactory(
                new PropertyValueFactory<>("titulo")
        );

        colStockActual.setCellValueFactory(
                new PropertyValueFactory<>("stockActual")
        );

        colPrecio.setCellValueFactory(
                new PropertyValueFactory<>("precio")
        );

        colValorInventario.setCellValueFactory(
                new PropertyValueFactory<>("valorInventario")
        );

        tablaStockValorizado.setItems(stockValorizado);
    }

    private void cargarRanking() {

        try {

            List<LibroMasVendido> libros
                    = reporteDao.listarLibrosMasVendidos();

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

    private void cargarStockValorizado() {

        try {

            List<StockValorizado> libros
                    = reporteDao.listarStockValorizado();

            stockValorizado.clear();

            if (libros != null) {
                stockValorizado.addAll(libros);
            }

            actualizarValorTotal();

        } catch (SQLException ex) {

            mostrarError(
                    "No se pudo cargar la valoración del inventario.\n"
                    + ex.getMessage()
            );
        }
    }

    private void actualizarValorTotal() {

        BigDecimal total = BigDecimal.ZERO;

        for (StockValorizado libro : stockValorizado) {

            if (libro.getValorInventario() != null) {
                total = total.add(libro.getValorInventario());
            }
        }

        lblValorTotalInventario.setText(
                String.format(
                        "Valor total: Q%.2f",
                        total
                )
        );
    }

    private void mostrarError(String mensaje) {

        Alert alerta
                = new Alert(Alert.AlertType.ERROR);

        alerta.setTitle(
                "Reportes de Inventario"
        );

        alerta.setHeaderText(
                "No se pudo cargar el reporte"
        );

        alerta.setContentText(mensaje);

        alerta.showAndWait();
    }

    @FXML
    private void onFiltrarClick() {

        LocalDate fechaInicio = dpFechaInicio.getValue();
        LocalDate fechaFin = dpFechaFin.getValue();

        if (fechaInicio == null || fechaFin == null) {
            mostrarError("Debes seleccionar una fecha de inicio y una fecha final.");
            return;
        }

        if (fechaInicio.isAfter(fechaFin)) {
            mostrarError("La fecha de inicio no puede ser mayor que la fecha final.");
            return;
        }

        if (fechaInicio.isAfter(LocalDate.now())) {
            mostrarError("La fecha inicial no puede ser mayor a la fecha actual.");
            return;
        }

        if (fechaFin.isAfter(LocalDate.now())) {
            mostrarError("La fecha final no puede ser mayor a la fecha actual.");
            return;
        }

        try {

            List<LibroMasVendido> libros
                    = reporteDao.listarLibrosMasVendidos(
                            fechaInicio,
                            fechaFin
                    );

            ranking.clear();
            ranking.addAll(libros);

        } catch (SQLException ex) {

            mostrarError(
                    "No se pudo aplicar el filtro.\n"
                    + ex.getMessage()
            );
        }
    }

    @FXML
    private void onLimpiarClick() {

        dpFechaInicio.setValue(null);
        dpFechaFin.setValue(null);

        cargarRanking();
    }
}
