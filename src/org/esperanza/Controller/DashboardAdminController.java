package org.esperanza.controller;

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

import org.esperanza.Model.Rol;
import org.esperanza.Model.Usuario;
import org.esperanza.Service.NavegacionRol;
import org.esperanza.Service.SesionUsuario;
import org.esperanza.dao.DashboardIndicadoresDao;
import org.esperanza.model.IndicadoresDashboardAdmin;

public class DashboardAdminController {

    @FXML
    private Label lblUsuario;

    @FXML private Label lblVentasTotales;
    @FXML private Label lblCantidadLibros;
    @FXML private Label lblUsuariosActivos;
    @FXML private Label lblEstadoIndicadores;
    @FXML private Button btnActualizarIndicadores;

    private final DashboardIndicadoresDao indicadoresDao;
    private Usuario usuarioActual;

    // FXMLLoader crea el controlador usando este constructor.
    public DashboardAdminController() {
        this(new DashboardIndicadoresDao());
    }

    // Permite probar el controlador con un DAO controlado, sin importar clases de test.
    public DashboardAdminController(DashboardIndicadoresDao indicadoresDao) {
        this.indicadoresDao = Objects.requireNonNull(indicadoresDao, "indicadoresDao");
    }

    @FXML
    private void initialize() {

        if (!NavegacionRol.validarRol(Rol.ADMIN)) {
            Platform.runLater(this::redirigirDashboardCorrecto);
            return;
        }

        usuarioActual = SesionUsuario
                .getInstancia()
                .getUsuarioActual();

        actualizarUsuario();
        actualizarIndicadores();
    }

    @FXML
    private void onActualizarIndicadoresClick() {
        actualizarIndicadores();
    }

    private void actualizarIndicadores() {
        if (!SesionUsuario.getInstancia().esAdmin()
                || btnActualizarIndicadores.isDisabled()) {
            return;
        }

        btnActualizarIndicadores.setDisable(true);
        lblEstadoIndicadores.getStyleClass().remove("kpi-status-error");
        lblEstadoIndicadores.setText("Actualizando indicadores...");

        Task<IndicadoresDashboardAdmin> tarea = new Task<>() {
            @Override
            protected IndicadoresDashboardAdmin call() throws Exception {
                return indicadoresDao.obtenerIndicadores();
            }
        };

        tarea.setOnSucceeded(evento -> {
            if (!SesionUsuario.getInstancia().esAdmin()) {
                return;
            }
            IndicadoresDashboardAdmin datos = tarea.getValue();
            NumberFormat moneda = NumberFormat.getCurrencyInstance(
                    Locale.forLanguageTag("es-GT"));
            NumberFormat numero = NumberFormat.getIntegerInstance(
                    Locale.forLanguageTag("es-GT"));
            lblVentasTotales.setText(moneda.format(datos.getVentasTotales()));
            lblCantidadLibros.setText(numero.format(datos.getCantidadLibros()));
            lblUsuariosActivos.setText(numero.format(datos.getUsuariosActivos()));
            lblEstadoIndicadores.setText("Indicadores actualizados");
            btnActualizarIndicadores.setDisable(false);
        });

        tarea.setOnFailed(evento -> {
            if (!SesionUsuario.getInstancia().esAdmin()) {
                return;
            }
            System.err.println("Error al cargar indicadores: "
                    + tarea.getException().getMessage());
            lblEstadoIndicadores.getStyleClass().add("kpi-status-error");
            lblEstadoIndicadores.setText(
                    "No se pudieron cargar los indicadores. Revisa la conexión y pulsa Actualizar.");
            btnActualizarIndicadores.setDisable(false);
        });

        Thread hilo = new Thread(tarea, "indicadores-dashboard-admin");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void actualizarUsuario() {

        if (usuarioActual != null && lblUsuario != null) {
            lblUsuario.setText(usuarioActual.getUsrname());
        }
    }

    public void setUsuarioActual(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
        actualizarUsuario();
    }

    @FXML
    private void onUsuariosClick() {

        if (!NavegacionRol.validarPermiso("GESTION_USUARIOS")) {
            return;
        }

        abrirModuloModal(
                "/org/esperanza/view/Usuarios.fxml",
                "Gestión de Usuarios - Librería La Esperanza",
                900,
                600
        );
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

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/org/esperanza/view/CambioContrasenaDashboard.fxml"
                            )
                    );

            Parent root = loader.load();

            CambioContrasenaController controller =
                    loader.getController();

            controller.setIdUsuarioActual(
                    usuarioActual.getId()
            );

            Stage ventana = new Stage();

            ventana.initOwner(
                    lblUsuario
                            .getScene()
                            .getWindow()
            );

            ventana.initModality(
                    Modality.WINDOW_MODAL
            );

            ventana.setScene(
                    new Scene(root)
            );

            ventana.setTitle(
                    "Cambiar Contraseña"
            );

            ventana.setResizable(false);
            ventana.centerOnScreen();
            ventana.showAndWait();

        } catch (IOException e) {

            e.printStackTrace();

            mostrarError(
                    "No se pudo abrir la pantalla de Cambio de Contraseña.\n"
                    + e.getMessage()
            );
        }
    }

    @FXML
    private void onLibrosClick() {

        abrirModuloModal(
                "/org/esperanza/view/Libros.fxml",
                "Gestión de Libros - Librería La Esperanza",
                1000,
                650
        );
    }

    @FXML
    private void onAutoresClick() {
        mostrarEnConstruccion("Autores");
    }

    @FXML
    private void onCategoriasClick() {
        mostrarEnConstruccion("Categorías");
    }

    @FXML
    private void onEditorialesClick() {
        mostrarEnConstruccion("Editoriales");
    }

    @FXML
    private void onVentasClick() {

        if (!NavegacionRol.validarPermiso("VENTAS")) {
            return;
        }

        if (usuarioActual == null) {
            mostrarError(
                    "No se pudo identificar al usuario actual."
            );
            return;
        }

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/org/esperanza/view/CarritoVenta.fxml"
                            )
                    );

            Parent root = loader.load();

            CarritoVentaController controller =
                    loader.getController();

            controller.setIdUsuario(
                    usuarioActual.getId()
            );

            Stage ventana = new Stage();

            ventana.initOwner(
                    lblUsuario
                            .getScene()
                            .getWindow()
            );

            ventana.initModality(
                    Modality.WINDOW_MODAL
            );

            ventana.setScene(
                    new Scene(
                            root,
                            1100,
                            680
                    )
            );

            ventana.setTitle(
                    "Registrar Venta - Librería La Esperanza"
            );

            ventana.setMinWidth(950);
            ventana.setMinHeight(600);
            ventana.setResizable(true);
            ventana.centerOnScreen();
            ventana.showAndWait();
            actualizarIndicadores();

        } catch (IOException e) {

            e.printStackTrace();

            mostrarError(
                    "No se pudo abrir el carrito de venta.\n"
                    + e.getMessage()
            );
        }
    }

    @FXML
    private void onAutoresLibroClick() {
        mostrarEnConstruccion("Autores-Libro");
    }

    @FXML
    private void onDetalleVentasClick() {

        if (!NavegacionRol.validarPermiso("VENTAS")) {
            return;
        }

        abrirModuloModal(
                "/org/esperanza/view/DashboardVentasDia.fxml",
                "Ventas del Día - Librería La Esperanza",
                950,
                600
        );
    }

    @FXML
    private void onClientesClick() {
        mostrarEnConstruccion("Clientes");
    }

    @FXML
    private void onReportesInventarioClick() {

        abrirModuloModal(
                "/org/esperanza/view/ReportesInventario.fxml",
                "Reportes de Inventario - Librería La Esperanza",
                950,
                640
        );
    }

    @FXML
    private void onActualizarPrecioClick() {

        abrirModuloModal(
                "/org/esperanza/view/ActualizarPrecio.fxml",
                "Actualizar Precio - Librería La Esperanza",
                560,
                390
        );
    }

    private void abrirModuloModal(
            String recurso,
            String titulo,
            double ancho,
            double alto) {

        try {
            URL pantalla = getClass().getResource(recurso);
            if (pantalla == null) {
                mostrarError("No se encontró la pantalla: " + recurso);
                return;
            }

            FXMLLoader loader = new FXMLLoader(pantalla);

            Parent root = loader.load();

            Stage ventana = new Stage();

            if (lblUsuario != null
                    && lblUsuario.getScene() != null
                    && lblUsuario.getScene().getWindow() != null) {

                ventana.initOwner(
                        lblUsuario
                                .getScene()
                                .getWindow()
                );
            }

            ventana.initModality(
                    Modality.WINDOW_MODAL
            );

            ventana.setScene(
                    new Scene(
                            root,
                            ancho,
                            alto
                    )
            );

            ventana.setTitle(titulo);
            ventana.centerOnScreen();
            ventana.showAndWait();
            actualizarIndicadores();

        } catch (IOException e) {

            e.printStackTrace();

            mostrarError(
                    "No se pudo abrir el módulo.\n"
                    + e.getMessage()
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

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/org/esperanza/view/Login.fxml"
                            )
                    );

            Parent root = loader.load();

            Stage stage =
                    (Stage) lblUsuario
                            .getScene()
                            .getWindow();

            stage.setScene(
                    new Scene(root)
            );

            stage.setTitle(
                    "Librería La Esperanza"
            );

            stage.centerOnScreen();

        } catch (IOException e) {

            e.printStackTrace();

            mostrarError(
                    "No se pudo cerrar la sesión.\n"
                    + e.getMessage()
            );
        }
    }

    private void redirigirDashboardCorrecto() {

        if (lblUsuario == null
                || lblUsuario.getScene() == null
                || lblUsuario.getScene().getWindow() == null) {
            return;
        }

        Stage stage =
                (Stage) lblUsuario
                        .getScene()
                        .getWindow();

        if (SesionUsuario
                .getInstancia()
                .haySesionActiva()) {

            NavegacionRol
                    .abrirDashboardSegunRol(stage);

        } else {
            stage.close();
        }
    }

    private void mostrarEnConstruccion(String modulo) {

        Alert alert =
                new Alert(
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

    private void mostrarError(String mensaje) {

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
