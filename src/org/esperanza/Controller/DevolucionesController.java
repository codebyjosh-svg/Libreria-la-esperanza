package org.esperanza.controller;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.esperanza.dao.DevolucionVentaDao;
import org.esperanza.model.DetalleVenta;
import org.esperanza.model.EstadoVenta;
import org.esperanza.model.Venta;
import org.esperanza.service.NavegacionRol;
import org.esperanza.service.SesionUsuario;

public class DevolucionesController {
    @FXML private VBox formulario;
    @FXML private TextField txtIdVenta;
    @FXML private TextArea txtMotivo;
    @FXML private Label lblEstado;
    @FXML private Label lblSeleccion;
    @FXML private Button btnDevolver;
    @FXML private TableView<Venta> tablaVentas;
    @FXML private TableColumn<Venta, Integer> colVenta;
    @FXML private TableColumn<Venta, String> colFecha;
    @FXML private TableColumn<Venta, Long> colCliente;
    @FXML private TableColumn<Venta, BigDecimal> colTotal;
    @FXML private TableColumn<Venta, String> colEstado;
    @FXML private TableView<DetalleVenta> tablaDetalles;
    @FXML private TableColumn<DetalleVenta, String> colIsbn;
    @FXML private TableColumn<DetalleVenta, Integer> colCantidad;
    @FXML private TableColumn<DetalleVenta, BigDecimal> colPrecio;
    @FXML private TableColumn<DetalleVenta, BigDecimal> colSubtotal;
    private final DevolucionVentaDao dao = new DevolucionVentaDao();
    private boolean ocupado;
    private boolean detalleCargado;

    @FXML private void initialize() {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        colVenta.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getIdVenta()));
        colFecha.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getFechaVenta().format(formato)));
        colCliente.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getCuiCliente()));
        colTotal.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getTotal()));
        colEstado.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getEstado().name()));
        colIsbn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getIsbn()));
        colCantidad.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getCantidad()));
        colPrecio.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getPrecioUnitario()));
        colSubtotal.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getSubtotal()));
        tablaVentas.setPlaceholder(new Label("No hay ventas en la consulta."));
        tablaDetalles.setPlaceholder(new Label("Selecciona una venta para ver sus productos."));
        txtIdVenta.setTextFormatter(new TextFormatter<String>(c ->
                c.getControlNewText().matches("[0-9]{0,10}") ? c : null));
        txtMotivo.setTextFormatter(new TextFormatter<String>(c -> c.getControlNewText().length() <= 500 ? c : null));
        tablaVentas.getSelectionModel().selectedItemProperty().addListener((obs, antes, venta) -> seleccionar(venta));
        actualizarBoton();
        if (tienePermiso()) onConsultar();
        else {
            formulario.setDisable(true);
            lblEstado.setText("Acceso denegado: se requiere permiso de devoluciones.");
        }
    }

    @FXML private void onConsultar() {
        if (!autorizar()) return;
        final Integer id;
        try {
            String texto = txtIdVenta.getText().trim();
            id = texto.isEmpty() ? null : Integer.valueOf(texto);
            if (id != null && id <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            mostrarError("El ID debe ser un entero entre 1 y 2147483647, o quedar vacío.");
            return;
        }
        tablaVentas.getSelectionModel().clearSelection();
        ejecutar(() -> dao.listarRecientes(id), ventas -> {
            tablaVentas.getItems().setAll(ventas);
            lblEstado.setText(ventas.size() + " venta(s) en los últimos 30 días. Selecciona una para continuar.");
        });
    }

    private void seleccionar(Venta venta) {
        detalleCargado = false;
        tablaDetalles.getItems().clear();
        txtMotivo.clear();
        actualizarBoton();
        if (venta == null) { lblSeleccion.setText("Ninguna venta seleccionada."); return; }
        lblSeleccion.setText("Venta #" + venta.getIdVenta() + " · " + venta.getEstado()
                + " · Total Q " + venta.getTotal().toPlainString());
        if (!autorizar()) return;
        ejecutar(() -> dao.listarDetalles(venta.getIdVenta()), detalles -> {
            tablaDetalles.getItems().setAll(detalles);
            detalleCargado = !detalles.isEmpty();
            actualizarBoton();
            lblEstado.setText(venta.getEstado().permiteDevolucion()
                    ? "Revisa los productos e ingresa el motivo de la devolución total."
                    : "Esta venta no admite devolución: " + venta.getEstado() + ".");
        });
    }

    @FXML private void onDevolver() {
        if (!autorizar()) return;
        Venta venta = tablaVentas.getSelectionModel().getSelectedItem();
        if (venta == null || !detalleCargado || !venta.getEstado().permiteDevolucion()) {
            mostrarError("Selecciona una venta COMPLETADA con productos.");
            return;
        }
        final String motivo;
        try { motivo = DevolucionVentaDao.validarMotivo(txtMotivo.getText()); }
        catch (IllegalArgumentException ex) { mostrarError(ex.getMessage()); return; }
        Alert confirmar = new Alert(Alert.AlertType.CONFIRMATION);
        confirmar.setTitle("Confirmar devolución total");
        confirmar.setHeaderText("Devolver venta #" + venta.getIdVenta() + " por Q " + venta.getTotal());
        confirmar.setContentText("Se restaurará el stock de todos los productos y la venta quedará DEVUELTA.\nMotivo: " + motivo);
        if (confirmar.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        ejecutar(() -> { dao.devolver(venta.getIdVenta(), motivo); return venta; }, devuelta -> {
            devuelta.setEstado(EstadoVenta.DEVUELTA);
            tablaVentas.refresh();
            txtMotivo.clear();
            lblSeleccion.setText("Venta #" + devuelta.getIdVenta() + " · DEVUELTA · Total Q " + devuelta.getTotal());
            lblEstado.setText("Devolución registrada. Stock restaurado y motivo/usuario guardados.");
            actualizarBoton();
        });
    }

    private boolean tienePermiso() {
        SesionUsuario sesion = SesionUsuario.getInstancia();
        return sesion.haySesionActiva() && sesion.tienePermiso("DEVOLUCIONES")
                && sesion.getUsuarioActual() != null && sesion.getUsuarioActual().isActivo();
    }

    private boolean autorizar() {
        if (ocupado) return false;
        if (!tienePermiso()) { mostrarError("Tu sesión no tiene permiso para gestionar devoluciones."); return false; }
        return true;
    }

    private <T> void ejecutar(Callable<T> accion, Consumer<T> completar) {
        ocupado = true;
        formulario.setDisable(true);
        lblEstado.setText("Procesando…");
        Task<T> tarea = new Task<>() {
            @Override protected T call() throws Exception { return accion.call(); }
        };
        tarea.setOnSucceeded(event -> {
            terminar();
            completar.accept(tarea.getValue());
        });
        tarea.setOnFailed(event -> {
            terminar();
            lblEstado.setText("No se completó la operación. Actualiza la consulta antes de reintentar.");
            mostrarError(tarea.getException().getMessage());
        });
        Thread hilo = new Thread(tarea, "devolucion-venta");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void terminar() {
        ocupado = false;
        formulario.setDisable(!tienePermiso());
        actualizarBoton();
    }

    private void actualizarBoton() {
        Venta venta = tablaVentas.getSelectionModel().getSelectedItem();
        btnDevolver.setDisable(ocupado || !tienePermiso() || !detalleCargado
                || venta == null || !venta.getEstado().permiteDevolucion());
    }

    public boolean estaOcupado() { return ocupado; }

    @FXML private void onCerrar() {
        if (ocupado) return;
        Stage ventana = (Stage) formulario.getScene().getWindow();
        NavegacionRol.abrirDashboardSegunRol(ventana);
    }

    private void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Devoluciones de ventas");
        alerta.setHeaderText("Revisa la operación");
        alerta.setContentText(mensaje == null ? "No se pudo completar la operación." : mensaje);
        alerta.showAndWait();
    }
}
