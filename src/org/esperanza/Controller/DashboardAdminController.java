package org.esperanza.Controller;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.esperanza.dao.DashboardIndicadoresDao;
import org.esperanza.Model.IndicadoresDashboardAdmin;
import org.esperanza.Model.Rol;
import org.esperanza.Model.Usuario;
import org.esperanza.Service.NavegacionRol;
import org.esperanza.Service.Pantallas;
import org.esperanza.Service.SesionUsuario;

public class DashboardAdminController {

    @FXML
    private Label lblUsuario;

    @FXML
    private Label lblVentasTotales;

    @FXML
    private Label lblCantidadLibros;

    @FXML
    private Label lblUsuariosActivos;

    @FXML
    private Label lblEstadoIndicadores;

    @FXML
    private Button btnActualizarIndicadores;

    private final DashboardIndicadoresDao indicadoresDao;
    private Usuario usuarioActual;

    public DashboardAdminController() {
        this(new DashboardIndicadoresDao());
    }

    public DashboardAdminController(
            DashboardIndicadoresDao indicadoresDao) {

        this.indicadoresDao = Objects.requireNonNull(
                indicadoresDao,
                "indicadoresDao"
        );
    }

    @FXML
    private void initialize() {

        if (!NavegacionRol.validarRol(Rol.ADMIN)) {
            Platform.runLater(this::redirigirDashboardCorrecto);
            return;
        }

        usuarioActual
                = SesionUsuario
                        .getInstancia()
                        .getUsuarioActual();

        actualizarUsuario();
        actualizarIndicadores();

        Platform.runLater(() -> {
            Stage stage = obtenerStagePrincipal();
            if (stage != null) {
                stage.setOnCloseRequest(null);
            }
        });
    }

    @FXML
    private void onActualizarIndicadoresClick() {
        actualizarIndicadores();
    }

    private void actualizarIndicadores() {

        if (!SesionUsuario.getInstancia().esAdmin()) {
            return;
        }

        if (btnActualizarIndicadores == null
                || lblVentasTotales == null
                || lblCantidadLibros == null
                || lblUsuariosActivos == null
                || lblEstadoIndicadores == null) {
            return;
        }

        if (btnActualizarIndicadores.isDisabled()) {
            return;
        }

        btnActualizarIndicadores.setDisable(true);

        lblEstadoIndicadores
                .getStyleClass()
                .remove("kpi-status-error");

        lblEstadoIndicadores.setText(
                "Actualizando indicadores..."
        );

        Task<IndicadoresDashboardAdmin> tarea
                = new Task<>() {

            @Override
            protected IndicadoresDashboardAdmin call()
                    throws Exception {

                return indicadoresDao.obtenerIndicadores();
            }
        };

        tarea.setOnSucceeded(evento -> {

            if (!SesionUsuario
                    .getInstancia()
                    .esAdmin()) {
                return;
            }

            IndicadoresDashboardAdmin datos
                    = tarea.getValue();

            if (datos == null) {

                lblEstadoIndicadores.setText(
                        "No se pudieron obtener los indicadores."
                );

                btnActualizarIndicadores.setDisable(false);
                return;
            }

            NumberFormat moneda
                    = NumberFormat.getCurrencyInstance(
                            Locale.forLanguageTag("es-GT")
                    );

            NumberFormat numero
                    = NumberFormat.getIntegerInstance(
                            Locale.forLanguageTag("es-GT")
                    );

            lblVentasTotales.setText(
                    moneda.format(
                            datos.getVentasTotales()
                    )
            );

            lblCantidadLibros.setText(
                    numero.format(
                            datos.getCantidadLibros()
                    )
            );

            lblUsuariosActivos.setText(
                    numero.format(
                            datos.getUsuariosActivos()
                    )
            );

            lblEstadoIndicadores.setText(
                    "Indicadores actualizados"
            );

            btnActualizarIndicadores.setDisable(false);
        });

        tarea.setOnFailed(evento -> {

            Throwable error = tarea.getException();

            if (error != null) {
                System.err.println(
                        "Error al cargar indicadores: "
                        + error.getMessage()
                );
            }

            lblEstadoIndicadores
                    .getStyleClass()
                    .remove("kpi-status-error");

            lblEstadoIndicadores
                    .getStyleClass()
                    .add("kpi-status-error");

            lblEstadoIndicadores.setText(
                    "No se pudieron cargar los indicadores. "
                    + "Revisa la conexión y pulsa Actualizar."
            );

            btnActualizarIndicadores.setDisable(false);
        });

        Thread hilo
                = new Thread(
                        tarea,
                        "indicadores-dashboard-admin"
                );

        hilo.setDaemon(true);
        hilo.start();
    }

    private void actualizarUsuario() {

        if (usuarioActual != null
                && lblUsuario != null) {

            lblUsuario.setText(
                    usuarioActual.getUsrname()
            );
        }
    }

    public void setUsuarioActual(
            Usuario usuarioActual) {

        this.usuarioActual = usuarioActual;
        actualizarUsuario();
    }

    @FXML
    private void onUsuariosClick() {

        if (!NavegacionRol.validarPermiso(
                "GESTION_USUARIOS")) {
            return;
        }

        try {

            URL recurso = getClass().getResource(
                    "/org/esperanza/view/Usuarios.fxml"
            );

            if (recurso == null) {
                mostrarError(
                        "No se encontró Usuarios.fxml."
                );
                return;
            }

            FXMLLoader loader = new FXMLLoader(recurso);
            Parent root = loader.load();

            Stage stage = obtenerStagePrincipal();

            if (stage == null) {
                return;
            }

            stage.setOnCloseRequest(null);
            stage.setScene(new Scene(root));
            stage.setTitle(
                    "Gestión de Usuarios - Librería La Esperanza"
            );
            stage.sizeToScene();
            stage.centerOnScreen();
            stage.show();

        } catch (IOException ex) {

            mostrarError(
                    "No se pudo abrir Gestión de Usuarios.\n"
                    + ex.getMessage()
            );
        }
    }

    @FXML
    private void onCambiarContrasenaClick() {

        if (usuarioActual == null) {

            mostrarError(
                    "No se pudo identificar al usuario que inició sesión."
            );

            return;
        }

        try {

            URL recurso = getClass().getResource(
                    "/org/esperanza/view/CambioContrasenaDashboard.fxml"
            );

            if (recurso == null) {
                mostrarError(
                        "No se encontró CambioContrasenaDashboard.fxml."
                );
                return;
            }

            FXMLLoader loader = new FXMLLoader(recurso);
            Parent root = loader.load();

            CambioContrasenaController Controller
                    = loader.getController();

            Controller.setIdUsuarioActual(
                    usuarioActual.getId()
            );

            Stage ventana = new Stage();

            ventana.initOwner(
                    lblUsuario.getScene().getWindow()
            );

            ventana.initModality(
                    Modality.WINDOW_MODAL
            );

            ventana.setScene(new Scene(root));
            ventana.setTitle("Cambiar Contraseña");
            ventana.setResizable(false);
            ventana.centerOnScreen();
            ventana.showAndWait();

        } catch (IOException ex) {

            mostrarError(
                    "No se pudo abrir la pantalla de Cambio de Contraseña.\n"
                    + ex.getMessage()
            );
        }
    }

    @FXML
    private void onLibrosClick() {

        if (!NavegacionRol.validarPermiso(
                "GESTION_INVENTARIO")) {
            return;
        }

        try {

            FXMLLoader loader
                    = new FXMLLoader(
                            getClass().getResource(
                                    "/org/esperanza/view/Libros.fxml"
                            )
                    );

            Parent root = loader.load();

            Stage ventana
                    = (Stage) lblUsuario
                            .getScene()
                            .getWindow();

            ventana.setOnCloseRequest(null);

            ventana.setScene(
                    new Scene(root)
            );

            ventana.setTitle(
                    "Gestión de Libros - Librería La Esperanza"
            );

            ventana.sizeToScene();
            ventana.centerOnScreen();

        } catch (IOException ex) {

            mostrarError(
                    "No se pudo abrir Libros.\n"
                    + ex.getMessage()
            );
        }
    }

    @FXML
    private void onAutoresClick() {

        Pantallas.catalogo(
                lblUsuario,
                "autores",
                "Autores"
        );
    }

    @FXML
    private void onCategoriasClick() {

        try {

            URL recurso = getClass().getResource(
                    "/org/esperanza/view/Categorias.fxml"
            );

            if (recurso == null) {
                mostrarError(
                        "No se encontró Categorias.fxml."
                );
                return;
            }

            FXMLLoader loader = new FXMLLoader(recurso);
            Parent root = loader.load();

            Stage stage = obtenerStagePrincipal();

            if (stage == null) {
                return;
            }

            stage.setOnCloseRequest(event -> {
                event.consume();
                NavegacionRol.abrirDashboardSegunRol(stage);
            });

            stage.setScene(new Scene(root));
            stage.setTitle(
                    "Gestión de Categorías - Librería La Esperanza"
            );
            stage.sizeToScene();
            stage.centerOnScreen();

        } catch (IOException ex) {

            mostrarError(
                    "No se pudo abrir Gestión de Categorías.\n"
                    + ex.getMessage()
            );
        }
    }

    @FXML
    private void onEditorialesClick() {

        Pantallas.catalogo(
                lblUsuario,
                "editoriales",
                "Editoriales"
        );
    }

    @FXML
    private void onProveedoresClick() {

        abrirModuloGestion(
                "GESTION_PROVEEDORES",
                "Proveedores",
                "Gestión de proveedores"
        );
    }

    @FXML
    private void onDevolucionesClick() {

        abrirModuloGestion(
                "DEVOLUCIONES",
                "Devoluciones",
                "Devoluciones de ventas"
        );
    }

    private void abrirModuloGestion(
            String permiso,
            String vista,
            String titulo) {

        if (!NavegacionRol.validarPermiso(
                permiso)) {
            return;
        }

        try {

            URL recurso = getClass().getResource(
                    "/org/esperanza/view/"
                    + vista
                    + ".fxml"
            );

            if (recurso == null) {
                mostrarError(
                        "No se encontró la pantalla: "
                        + vista
                        + ".fxml"
                );
                return;
            }

            FXMLLoader loader = new FXMLLoader(recurso);
            Parent root = loader.load();

            Stage ventana = obtenerStagePrincipal();

            if (ventana == null) {
                return;
            }

            Object Controller = loader.getController();

            ventana.setOnCloseRequest(event -> {

                boolean ocupado
                        = Controller instanceof ProveedoresController proveedor
                        && proveedor.estaOcupado();

                ocupado
                        = ocupado
                        || Controller instanceof DevolucionesController devolucion
                        && devolucion.estaOcupado();

                event.consume();

                if (!ocupado) {
                    NavegacionRol.abrirDashboardSegunRol(ventana);
                }
            });

            ventana.setScene(new Scene(root));
            ventana.setTitle(
                    titulo
                    + " - Librería La Esperanza"
            );
            ventana.sizeToScene();
            ventana.centerOnScreen();

        } catch (IOException ex) {

            mostrarError(
                    "No se pudo abrir "
                    + titulo
                    + ".\n"
                    + ex.getMessage()
            );
        }
    }

    @FXML
    private void onVentasClick() {

        if (!NavegacionRol.validarPermiso(
                "VENTAS")) {
            return;
        }

        if (usuarioActual == null) {

            mostrarError(
                    "No se pudo identificar al usuario actual."
            );

            return;
        }

        try {

            URL recurso = getClass().getResource(
                    "/org/esperanza/view/CarritoVenta.fxml"
            );

            if (recurso == null) {
                mostrarError(
                        "No se encontró CarritoVenta.fxml."
                );
                return;
            }

            FXMLLoader loader = new FXMLLoader(recurso);
            Parent root = loader.load();

            CarritoVentaController Controller
                    = loader.getController();

            Controller.setIdUsuario(
                    usuarioActual.getId()
            );

            Stage stage = obtenerStagePrincipal();

            if (stage == null) {
                return;
            }

            stage.setOnCloseRequest(null);
            stage.setScene(new Scene(root));
            stage.setTitle(
                    "Registrar Venta - Librería La Esperanza"
            );
            stage.centerOnScreen();

        } catch (IOException ex) {

            mostrarError(
                    "No se pudo abrir el carrito de venta.\n"
                    + ex.getMessage()
            );
        }
    }

    @FXML
    private void onAutoresLibroClick() {
        mostrarEnConstruccion("Autores-Libro");
    }

    @FXML
    private void onDetalleVentasClick() {

        if (!NavegacionRol.validarRol(
                Rol.ADMIN)) {
            return;
        }

        DetalleVentasAdmin.mostrar(
                (Stage) lblUsuario
                        .getScene()
                        .getWindow()
        );
    }

    @FXML
    private void onVentasDiaClick() {

        if (!NavegacionRol.validarRol(
                Rol.ADMIN)) {
            return;
        }

        Pantallas.abrir(
                lblUsuario,
                "DashboardVentasDia",
                "Ventas del Día"
        );
    }

    @FXML
    private void onClientesClick() {
        mostrarEnConstruccion("Clientes");
    }

    @FXML
    private void onReportesClick() {

        if (!NavegacionRol.validarPermiso(
                "VER_REPORTES")) {
            return;
        }

        try {

            URL recurso = getClass().getResource(
                    "/org/esperanza/view/Reportes.fxml"
            );

            if (recurso == null) {
                mostrarError(
                        "No se encontró Reportes.fxml."
                );
                return;
            }

            FXMLLoader loader = new FXMLLoader(recurso);
            Parent root = loader.load();

            Stage ventanaActual = obtenerStagePrincipal();

            if (ventanaActual == null) {
                return;
            }

            ventanaActual.setOnCloseRequest(event -> {
                event.consume();
                NavegacionRol.abrirDashboardSegunRol(
                        ventanaActual
                );
            });

            ventanaActual.setScene(new Scene(root));
            ventanaActual.setTitle(
                    "Reportes de Ventas - Librería La Esperanza"
            );
            ventanaActual.sizeToScene();
            ventanaActual.centerOnScreen();

        } catch (IOException ex) {

            mostrarError(
                    "No se pudo abrir Reportes de Ventas.\n"
                    + ex.getMessage()
            );
        }
    }

    @FXML
    private void onActualizarPrecioClick() {

        abrirModuloModal(
                "/org/esperanza/view/ActualizarPrecio.fxml",
                "Actualizar Precio - Librería La Esperanza",
                900,
                600
        );
    }

    private void abrirModuloModal(
            String recurso,
            String titulo,
            double ancho,
            double alto) {

        try {

            java.net.URL pantalla
                    = getClass().getResource(
                            recurso
                    );

            if (pantalla == null) {

                mostrarError(
                        "No se encontró la pantalla: "
                        + recurso
                );

                return;
            }

            FXMLLoader loader
                    = new FXMLLoader(pantalla);

            Parent root = loader.load();

            Stage ventana
                    = (Stage) lblUsuario
                            .getScene()
                            .getWindow();

            ventana.setOnCloseRequest(event -> {
                event.consume();
                NavegacionRol.abrirDashboardSegunRol(ventana);
            });

            ventana.setScene(
                    new Scene(
                            root,
                            ancho,
                            alto
                    )
            );

            ventana.setTitle(titulo);
            ventana.centerOnScreen();

        } catch (IOException ex) {

            mostrarError(
                    "No se pudo abrir el módulo.\n"
                    + ex.getMessage()
            );
        }
    }

    @FXML
    private void onCerrarSesionClick() {

        SesionUsuario
                .getInstancia()
                .cerrarSesion();

        usuarioActual = null;

        try {

            URL recurso = getClass().getResource(
                    "/org/esperanza/view/Login.fxml"
            );

            if (recurso == null) {
                mostrarError(
                        "No se encontró Login.fxml."
                );
                return;
            }

            FXMLLoader loader = new FXMLLoader(recurso);
            Parent root = loader.load();

            Stage stage = obtenerStagePrincipal();

            if (stage == null) {
                return;
            }

            stage.setOnCloseRequest(null);
            stage.setScene(new Scene(root));
            stage.setTitle("Librería La Esperanza");
            stage.centerOnScreen();

        } catch (IOException ex) {

            mostrarError(
                    "No se pudo cerrar la sesión.\n"
                    + ex.getMessage()
            );
        }
    }

    private void redirigirDashboardCorrecto() {

        Stage stage = obtenerStagePrincipal();

        if (stage == null) {
            return;
        }

        if (SesionUsuario
                .getInstancia()
                .haySesionActiva()) {

            NavegacionRol
                    .abrirDashboardSegunRol(
                            stage
                    );

        } else {

            stage.close();
        }
    }

    private Stage obtenerStagePrincipal() {

        if (lblUsuario == null
                || lblUsuario.getScene() == null
                || lblUsuario.getScene().getWindow() == null) {

            return null;
        }

        return (Stage) lblUsuario
                .getScene()
                .getWindow();
    }

    private void mostrarEnConstruccion(
            String modulo) {

        Alert alert
                = new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(modulo);
        alert.setHeaderText(null);

        alert.setContentText(
                "El módulo "
                + modulo
                + " se encuentra en construcción."
        );

        alert.showAndWait();
    }

    private void mostrarError(
            String mensaje) {

        Alert alert
                = new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
