package org.esperanza.Controller;

import java.io.IOException;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import org.esperanza.Model.Rol;
import org.esperanza.Service.NavegacionRol;
import org.esperanza.Service.SesionUsuario;
import org.esperanza.controller.CarritoVentaController;

public class DashboardCajeroController {

    @FXML
    private Label lblBienvenida;

    @FXML
    private Label lblRol;

    @FXML
    private Label lblPermisos;

    @FXML
    private Button btnCerrarSesion;

    @FXML
    private void initialize() {

        if (!NavegacionRol.validarRol(
                Rol.CAJERO)) {

            Platform.runLater(
                    this::redirigirDashboardCorrecto
            );

            return;
        }

        actualizarEncabezado();
    }

    private void actualizarEncabezado() {

        SesionUsuario sesion
                = SesionUsuario
                        .getInstancia();

        lblBienvenida.setText(
                "Bienvenido, "
                + sesion.getNombreCompleto()
        );

        lblRol.setText(
                "Rol: "
                + sesion
                        .getRolActual()
                        .getNombreVisible()
        );

        lblPermisos.setText(
                "Permisos: VENTAS | CONSULTAR_PRODUCTOS"
        );
    }

    @FXML
    private void onNuevaVenta() {

        if (!NavegacionRol.validarPermiso("VENTAS")) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/org/esperanza/view/CarritoVenta.fxml"
                    )
            );

            Parent root = loader.load();

            CarritoVentaController controller
                    = loader.getController();

            controller.setIdUsuario(
                    SesionUsuario
                            .getInstancia()
                            .getUsuarioActual()
                            .getId()
            );

            Stage ventana = new Stage();

            ventana.setTitle(
                    "Registrar Venta - Librería La Esperanza"
            );

            ventana.setScene(
                    new Scene(root)
            );

            ventana.initOwner(
                    lblBienvenida
                            .getScene()
                            .getWindow()
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
    private void onConsultarProductos() {

        if (!NavegacionRol.validarPermiso(
                "CONSULTAR_PRODUCTOS")) {

            return;
        }

        mostrarInfo(
                "Productos",
                "Acceso a consulta de productos autorizado."
        );
    }

    @FXML
    private void onCerrarSesion() {

        SesionUsuario
                .getInstancia()
                .cerrarSesion();

        volverLogin();
    }

    private void volverLogin() {

        try {

            FXMLLoader loader
                    = new FXMLLoader(
                            getClass()
                                    .getResource(
                                            "/org/esperanza/view/Login.fxml"
                                    )
                    );

            Parent root
                    = loader.load();

            Stage stage
                    = (Stage) btnCerrarSesion
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
                    "No se pudo regresar al login.\n"
                    + e.getMessage()
            );
        }
    }

    private void redirigirDashboardCorrecto() {

        if (lblBienvenida == null
                || lblBienvenida.getScene() == null
                || lblBienvenida
                        .getScene()
                        .getWindow() == null) {

            return;
        }

        Stage stage
                = (Stage) lblBienvenida
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

    private void mostrarInfo(
            String titulo,
            String mensaje) {

        Alert alert
                = new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

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
