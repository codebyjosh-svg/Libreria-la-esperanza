package org.esperanza.Controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import org.esperanza.Model.Rol;
import org.esperanza.Service.NavegacionRol;
import org.esperanza.Service.SesionUsuario;

public class DashboardBodegaController {

    @FXML
    private Label lblBienvenida;

    @FXML
    private Label lblRol;

    @FXML
    private Label lblPermisos;

    @FXML
    private Button btnCerrarSesion;

    // =====================================================
    // T1.19 / T1.22
    // =====================================================

    @FXML
    private void initialize() {

        if (!NavegacionRol.validarRol(
                Rol.BODEGA)) {

            Platform.runLater(
                    this::redirigirAlDashboardCorrecto
            );

            return;
        }

        actualizarEncabezado();
    }

    private void actualizarEncabezado() {

        SesionUsuario sesion =
                SesionUsuario.getInstancia();

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

        StringBuilder permisos =
                new StringBuilder(
                        "Permisos: "
                );

        for (String permiso
                : sesion
                        .getRolActual()
                        .getPermisos()) {

            permisos.append(
                    permiso
            ).append("  ");
        }

        lblPermisos.setText(
                permisos.toString()
        );
    }

    // =====================================================
    // INVENTARIO
    // =====================================================

    @FXML
    private void onInventario(
            ActionEvent event) {

        if (!NavegacionRol.validarPermiso(
                "GESTION_INVENTARIO")) {

            return;
        }

        mostrarInfo(
                "Inventario",
                "Gestión de inventario próximamente."
        );
    }

    // =====================================================
    // ENTRADAS / SALIDAS
    // =====================================================

    @FXML
    private void onEntradasSalidas(
            ActionEvent event) {

        if (!NavegacionRol.validarPermiso(
                "ENTRADAS_SALIDAS")) {

            return;
        }

        mostrarInfo(
                "Entradas / Salidas",
                "Registro de entradas y salidas próximamente."
        );
    }

    // =====================================================
    // CERRAR
    // =====================================================

    @FXML
    private void onCerrarSesion(
            ActionEvent event) {

        SesionUsuario
                .getInstancia()
                .cerrarSesion();

        Stage stage =
                (Stage) btnCerrarSesion
                        .getScene()
                        .getWindow();

        stage.close();
    }

    private void redirigirAlDashboardCorrecto() {

        if (lblBienvenida == null
                || lblBienvenida.getScene() == null
                || lblBienvenida
                        .getScene()
                        .getWindow() == null) {

            return;
        }

        Stage stage =
                (Stage) lblBienvenida
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

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(
                titulo
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