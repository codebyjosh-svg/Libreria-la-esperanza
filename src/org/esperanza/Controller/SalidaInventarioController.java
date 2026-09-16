package org.esperanza.Controller;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.esperanza.Model.SalidaInventario;
import org.esperanza.Model.TipoSalida;
import org.esperanza.Service.SesionUsuario;
import org.esperanza.dao.SalidaInventarioDao;
import org.esperanza.dao.StockDao;

public class SalidaInventarioController {
    @FXML private VBox formulario;
    @FXML private TextField txtIsbn;
    @FXML private TextField txtCantidad;
    @FXML private ComboBox<TipoSalida> cmbTipo;
    @FXML private TextArea txtObservacion;
    @FXML private Label lblStock;
    @FXML private Label lblEstado;
    private boolean ocupado;

    @FXML private void initialize() {
        cmbTipo.getItems().setAll(TipoSalida.values());
        txtCantidad.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("[0-9]{0,10}") ? change : null));
        txtIsbn.textProperty().addListener((obs, anterior, nuevo) -> lblStock.setText("Stock: sin consultar"));
        formulario.setDisable(!SesionUsuario.getInstancia().tienePermiso("ENTRADAS_SALIDAS"));
    }

    @FXML private void onConsultarStock() {
        if (!autorizar()) return;
        String isbn = txtIsbn.getText().trim();
        if (isbn.isEmpty()) { mostrarError("Ingresa el ISBN del libro."); return; }
        ejecutar(() -> new StockDao().obtenerStockActual(isbn), stock -> {
            lblStock.setText(stock < 0 ? "Libro inexistente o inactivo" : "Stock disponible: " + stock);
            lblEstado.setText("Consulta terminada.");
        });
    }

    @FXML private void onRegistrar() {
        if (!autorizar()) return;
        final SalidaInventario salida;
        try {
            salida = new SalidaInventario(txtIsbn.getText(), cmbTipo.getValue(),
                    Integer.parseInt(txtCantidad.getText()), txtObservacion.getText(),
                    SesionUsuario.getInstancia().getUsuarioActual().getId());
        } catch (NumberFormatException ex) {
            mostrarError("Ingresa una cantidad entera entre 1 y 2147483647.");
            return;
        } catch (IllegalArgumentException ex) {
            mostrarError(ex.getMessage());
            return;
        }
        ejecutar(() -> { new SalidaInventarioDao().registrar(salida); return salida; }, registrada -> {
            txtCantidad.clear();
            txtObservacion.clear();
            cmbTipo.getSelectionModel().clearSelection();
            lblStock.setText("Stock actualizado; consulta para ver las existencias actuales.");
            lblEstado.setText("Salida registrada: " + registrada.cantidad() + " unidad(es), " + registrada.tipo() + ".");
        });
    }

    private boolean autorizar() {
        if (ocupado) return false;
        if (!SesionUsuario.getInstancia().tienePermiso("ENTRADAS_SALIDAS")) {
            mostrarError("Tu sesión no tiene permiso para registrar salidas.");
            return false;
        }
        return true;
    }

    private <T> void ejecutar(Callable<T> accion, Consumer<T> alCompletar) {
        ocupado = true;
        formulario.setDisable(true);
        lblEstado.setText("Procesando…");
        Task<T> tarea = new Task<>() {
            @Override protected T call() throws Exception { return accion.call(); }
        };
        tarea.setOnSucceeded(event -> {
            terminar();
            alCompletar.accept(tarea.getValue());
        });
        tarea.setOnFailed(event -> {
            terminar();
            lblEstado.setText("No se completó la operación.");
            mostrarError(tarea.getException().getMessage());
        });
        Thread hilo = new Thread(tarea, "salida-inventario");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void terminar() {
        ocupado = false;
        formulario.setDisable(false);
    }

    public boolean estaOcupado() { return ocupado; }

    @FXML private void onCerrar() {
        if (!ocupado) ((Stage) formulario.getScene().getWindow()).close();
    }

    private void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Salida de inventario");
        alerta.setHeaderText("Revisa la operación");
        alerta.setContentText(mensaje == null ? "No se pudo completar la operación." : mensaje);
        alerta.showAndWait();
    }
}
