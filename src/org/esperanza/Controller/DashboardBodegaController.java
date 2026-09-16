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

public class DashboardBodegaController {

    @FXML private Label lblBienvenida;
    @FXML private Label lblRol;
    @FXML private Label lblPermisos;
    @FXML private Button btnCerrarSesion;

    @FXML
    private void initialize() {

        if (!NavegacionRol.validarRol(Rol.BODEGA)) {
            Platform.runLater(
                    this::redirigirDashboardCorrecto
            );
            return;
        }

        actualizarEncabezado();
        configurarCierre();
    }

    private void actualizarEncabezado() {

        SesionUsuario sesion =
                SesionUsuario
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
                "Permisos: GESTION_INVENTARIO | "
                + "CONSULTAR_PRODUCTOS | "
                + "ENTRADAS_SALIDAS"
        );
    }

    private void configurarCierre() {

        Platform.runLater(() -> {

            Stage stage = (Stage) lblBienvenida
                    .getScene()
                    .getWindow();

            stage.setOnCloseRequest(event -> {

                event.consume();

                SesionUsuario
                        .getInstancia()
                        .cerrarSesion();

                volverLogin();
            });
        });
    }

    @FXML
    private void onInventario() {

        if (!NavegacionRol.validarPermiso(
                "GESTION_INVENTARIO")) {

            return;
        }

        mostrarInfo(
                "Inventario",
                "Acceso a Gestión de Inventario autorizado."
        );
    }

    @FXML
    private void onEntradasSalidas() {

        if (!NavegacionRol.validarPermiso(
                "ENTRADAS_SALIDAS")) {

            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/org/esperanza/view/SalidaInventario.fxml"));
            Parent root = loader.load();
            SalidaInventarioController controller = loader.getController();
            Stage ventana = new Stage();
            ventana.initOwner(lblBienvenida.getScene().getWindow());
            ventana.initModality(javafx.stage.Modality.WINDOW_MODAL);
            ventana.setTitle("Salida de inventario - Librería La Esperanza");
            ventana.setScene(new Scene(root));
            ventana.setResizable(false);
            ventana.setOnCloseRequest(event -> {
                if (controller.estaOcupado()) event.consume();
            });
            ventana.showAndWait();
        } catch (IOException ex) {
            mostrarError("No se pudo abrir el formulario de salida.\n" + ex.getMessage());
        }
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

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/org/esperanza/view/Login.fxml"
                    )
            );

            Parent root = loader.load();

            Stage stage = (Stage) lblBienvenida
                    .getScene()
                    .getWindow();

            stage.setOnCloseRequest(null);

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

        Stage stage = (Stage) lblBienvenida
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

        Alert alert = new Alert(
                Alert.AlertType.INFORMATION
        );

        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarError(
            String mensaje) {

        Alert alert = new Alert(
                Alert.AlertType.ERROR
        );

        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
