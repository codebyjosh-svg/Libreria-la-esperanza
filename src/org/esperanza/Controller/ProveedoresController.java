package org.esperanza.controller;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.esperanza.dao.ProveedorDAO;
import org.esperanza.model.Proveedor;
import org.esperanza.service.NavegacionRol;
import org.esperanza.service.SesionUsuario;

/** Formulario administrativo con consultas JDBC fuera del hilo de JavaFX. */
public final class ProveedoresController {
    private static final Logger LOG = Logger.getLogger(ProveedoresController.class.getName());
    @FXML private HBox contenido;
    @FXML private TextField txtBuscar;
    @FXML private CheckBox chkInactivos;
    @FXML private TableView<Proveedor> tablaProveedores;
    @FXML private TableColumn<Proveedor, String> colNit, colNombre, colTelefono, colEstado;
    @FXML private TextField txtNit, txtNombre, txtContacto, txtTelefono, txtCorreo, txtDireccion;
    @FXML private Label lblFormulario, lblMensaje;
    @FXML private Button btnEstado;

    private final ProveedorDAO dao = new ProveedorDAO();
    private Proveedor seleccionado;
    private boolean ocupado;

    /** Permite al dashboard impedir el cierre con X durante una escritura o recarga. */
    public boolean estaOcupado() { return ocupado; }

    @FXML
    private void initialize() {
        colNit.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getNit()));
        colNombre.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getNombre()));
        colTelefono.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getTelefono()));
        colEstado.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().isActivo() ? "Activo" : "Inactivo"));
        tablaProveedores.setPlaceholder(new Label("No hay proveedores para esta búsqueda."));
        tablaProveedores.getSelectionModel().selectedItemProperty().addListener((obs, anterior, nuevo) -> mostrar(nuevo));
        if (!SesionUsuario.getInstancia().tienePermiso("GESTION_PROVEEDORES")) {
            contenido.setDisable(true);
            mensaje("Solo un administrador puede gestionar proveedores.", true);
            return;
        }
        onNuevo();
        onActualizar();
    }

    @FXML
    private void onActualizar() {
        String filtro = txtBuscar.getText();
        boolean inactivos = chkInactivos.isSelected();
        ejecutar(() -> dao.listar(filtro, inactivos), lista -> {
            actualizarLista(lista);
            mensaje("Proveedores encontrados: " + lista.size(), false);
        });
    }

    @FXML
    private void onNuevo() {
        tablaProveedores.getSelectionModel().clearSelection();
        mostrar(null);
        txtNit.requestFocus();
    }

    private void mostrar(Proveedor proveedor) {
        seleccionado = proveedor;
        lblFormulario.setText(proveedor == null ? "Nuevo proveedor" : "Editar proveedor #" + proveedor.getId());
        txtNit.setText(proveedor == null ? "" : proveedor.getNit());
        txtNombre.setText(proveedor == null ? "" : proveedor.getNombre());
        txtContacto.setText(proveedor == null ? "" : proveedor.getContacto());
        txtTelefono.setText(proveedor == null ? "" : proveedor.getTelefono());
        txtCorreo.setText(proveedor == null ? "" : proveedor.getCorreo());
        txtDireccion.setText(proveedor == null ? "" : proveedor.getDireccion());
        btnEstado.setDisable(proveedor == null);
        btnEstado.setText(proveedor != null && !proveedor.isActivo() ? "Reactivar" : "Desactivar");
    }

    @FXML
    private void onGuardar() {
        try {
            boolean nuevo = seleccionado == null;
            Proveedor datos = new Proveedor(nuevo ? 0 : seleccionado.getId(), txtNit.getText(),
                    txtNombre.getText(), txtContacto.getText(), txtTelefono.getText(), txtCorreo.getText(),
                    txtDireccion.getText(), nuevo || seleccionado.isActivo());
            // La recarga es independiente de la escritura: si falla, se informa que los datos sí se guardaron.
            ejecutar(() -> {
                if (nuevo) return dao.crear(datos);
                dao.actualizar(datos);
                return datos.getId();
            }, id -> {
                onNuevo();
                recargarTrasGuardar("Proveedor #" + id + (nuevo ? " creado." : " actualizado."));
            });
        } catch (IllegalArgumentException ex) {
            mensaje(ex.getMessage(), true);
        }
    }

    @FXML
    private void onCambiarEstado() {
        if (seleccionado == null) { mensaje("Seleccione un proveedor.", true); return; }
        Proveedor proveedor = seleccionado;
        boolean activar = !proveedor.isActivo();
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Desea " + (activar ? "reactivar" : "desactivar") + " a " + proveedor.getNombre() + "?"
                + (activar ? "" : " Se conservarán su información y su NIT."), ButtonType.YES, ButtonType.NO);
        confirmacion.setTitle("Gestión de proveedores");
        confirmacion.setHeaderText(null);
        confirmacion.initOwner(tablaProveedores.getScene().getWindow());
        if (confirmacion.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) return;
        ejecutar(() -> { dao.cambiarEstado(proveedor.getId(), activar); return true; }, resultado -> {
            onNuevo();
            recargarTrasGuardar("Proveedor " + (activar ? "reactivado." : "desactivado."));
        });
    }

    private void recargarTrasGuardar(String confirmado) {
        String filtro = txtBuscar.getText();
        boolean inactivos = chkInactivos.isSelected();
        ejecutar(() -> dao.listar(filtro, inactivos), lista -> {
            actualizarLista(lista);
            mensaje(confirmado + " Proveedores encontrados: " + lista.size(), false);
        }, confirmado + " No se pudo actualizar el listado; pulse Buscar / actualizar.");
    }

    private void actualizarLista(List<Proveedor> lista) {
        tablaProveedores.getItems().setAll(lista);
        onNuevo();
    }

    private <T> void ejecutar(Callable<T> trabajo, Consumer<T> terminado) {
        ejecutar(trabajo, terminado, null);
    }

    private <T> void ejecutar(Callable<T> trabajo, Consumer<T> terminado, String errorRecarga) {
        if (ocupado) return;
        ocupado = true;
        contenido.setDisable(true);
        mensaje("Procesando…", false);
        Task<T> tarea = new Task<>() {
            @Override protected T call() throws Exception { return trabajo.call(); }
        };
        tarea.setOnSucceeded(e -> { ocupado = false; contenido.setDisable(false); terminado.accept(tarea.getValue()); });
        tarea.setOnFailed(e -> {
            ocupado = false;
            Throwable ex = tarea.getException();
            contenido.setDisable(ex instanceof SecurityException);
            LOG.log(Level.WARNING, "Operación de proveedores fallida", ex);
            String texto = ex instanceof SQLException
                    ? "No se pudo acceder a los proveedores. Revise la conexión y la migración SQL de proveedores."
                    : ex.getMessage();
            mensaje(errorRecarga == null ? texto : errorRecarga, true);
        });
        Thread hilo = new Thread(tarea, "proveedores-db");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void mensaje(String texto, boolean error) {
        lblMensaje.setText(texto == null ? "No se pudo completar la operación." : texto);
        lblMensaje.setStyle(error ? "-fx-text-fill: #b91c1c;" : "-fx-text-fill: #166534;");
    }

    @FXML
    private void onCerrar() {
        if (ocupado) {
            mensaje("Espere a que termine la operación antes de cerrar.", true);
            return;
        }
        Stage ventana = (Stage) tablaProveedores.getScene().getWindow();
        NavegacionRol.abrirDashboardSegunRol(ventana);
    }
}
