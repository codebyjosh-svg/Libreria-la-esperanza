package org.esperanza.Controller;

import java.io.IOException;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.esperanza.Model.Rol;
import org.esperanza.Model.Usuario;
import org.esperanza.Service.NavegacionRol;
import org.esperanza.Service.SesionUsuario;
import org.esperanza.controller.CarritoVentaController;        

public class DashboardAdminController {

    @FXML
    private Label lblUsuario;

    private Usuario usuarioActual;

    @FXML
    private void initialize() {

        if (!NavegacionRol.validarRol(
                Rol.ADMIN)) {

            Platform.runLater(
                    this::redirigirDashboardCorrecto
            );

            return;
        }

        usuarioActual =
                SesionUsuario
                        .getInstancia()
                        .getUsuarioActual();

        actualizarUsuario();
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

        this.usuarioActual =
                usuarioActual;

        actualizarUsuario();
    }

    @FXML
    private void onUsuariosClick() {

        if (!NavegacionRol.validarPermiso(
                "GESTION_USUARIOS")) {

            return;
        }

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass()
                                    .getResource(
                                            "/org/esperanza/view/Usuarios.fxml"
                                    )
                    );

            Parent root =
                    loader.load();

            Stage stage =
                    (Stage) lblUsuario
                            .getScene()
                            .getWindow();

            stage.setScene(
                    new Scene(root)
            );

            stage.setTitle(
                    "Gestión de Usuarios - Librería La Esperanza"
            );

            stage.centerOnScreen();

        } catch (IOException e) {

            e.printStackTrace();

            mostrarError(
                    "No se pudo abrir Gestión de Usuarios.\n"
                    + e.getMessage()
            );
        }
    }

    @FXML
    private void onCambiarContrasenaClick() {

        if (usuarioActual == null) {

            mostrarError(
                    "No se pudo identificar al usuario "
                    + "que inició sesión."
            );

            return;
        }

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass()
                                    .getResource(
                                            "/org/esperanza/view/CambioContrasenaDashboard.fxml"
                                    )
                    );

            Parent root =
                    loader.load();

            CambioContrasenaController controller =
                    loader.getController();

            controller.setIdUsuarioActual(
                    usuarioActual.getId()
            );

            Stage ventana =
                    new Stage();

            ventana.setTitle(
                    "Cambiar Contraseña"
            );

            ventana.setScene(
                    new Scene(root)
            );

            ventana.initOwner(
                    lblUsuario
                            .getScene()
                            .getWindow()
            );

            ventana.initModality(
                    Modality.WINDOW_MODAL
            );

            ventana.setResizable(
                    false
            );

            ventana.centerOnScreen();

            ventana.showAndWait();

        } catch (IOException e) {

            e.printStackTrace();

            mostrarError(
                    "No se pudo abrir la pantalla "
                    + "de Cambio de Contraseña.\n"
                    + e.getMessage()
            );
        }
    }

    @FXML
    private void onLibrosClick() {

        mostrarEnConstruccion(
                "Libros"
        );
    }

    @FXML
    private void onAutoresClick() {

        mostrarEnConstruccion(
                "Autores"
        );
    }

    @FXML
    private void onCategoriasClick() {

        mostrarEnConstruccion(
                "Categorías"
        );
    }

    @FXML
    private void onEditorialesClick() {

        mostrarEnConstruccion(
                "Editoriales"
        );
    }

@FXML
private void onVentasClick() {

    if (!NavegacionRol.validarPermiso("VENTAS")) {
        return;
    }

    if (usuarioActual == null) {
        mostrarError("No se pudo identificar al usuario actual.");
        return;
    }

    try {
        FXMLLoader loader = new FXMLLoader(
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

        ventana.setTitle(
                "Registrar Venta - Librería La Esperanza"
        );

        ventana.setScene(
                new Scene(root)
        );

        ventana.initOwner(
                lblUsuario.getScene().getWindow()
        );

        ventana.centerOnScreen();
        ventana.show();

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

        mostrarEnConstruccion(
                "Autores-Libro"
        );
    }

    @FXML
    private void onDetalleVentasClick() {

        mostrarEnConstruccion(
                "Detalle Ventas"
        );
    }

    @FXML
    private void onClientesClick() {

        mostrarEnConstruccion(
                "Clientes"
        );
    }

    @FXML
    private void onCerrarSesionClick() {

        SesionUsuario
                .getInstancia()
                .cerrarSesion();

        usuarioActual =
                null;

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass()
                                    .getResource(
                                            "/org/esperanza/view/Login.fxml"
                                    )
                    );

            Parent root =
                    loader.load();

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
                || lblUsuario
                        .getScene()
                        .getWindow() == null) {

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
                    .abrirDashboardSegunRol(
                            stage
                    );

        } else {

            stage.close();
        }
    }

    private void mostrarEnConstruccion(
            String modulo) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(
                modulo
        );

        alert.setHeaderText(
                null
        );

        alert.setContentText(
                "El módulo "
                + modulo
                + " se encuentra en construcción."
        );

        alert.showAndWait();
    }

    private void mostrarError(
            String mensaje) {

        Alert alert =
                new Alert(
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