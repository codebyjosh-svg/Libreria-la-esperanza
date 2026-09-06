package org.esperanza.Controller;

import java.io.IOException;
import java.net.URL;

import javafx.application.Platform;
import javafx.event.ActionEvent;
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

public class DashboardAdminController {

    @FXML
    private Label lblBienvenida;

    @FXML
    private Label lblRol;

    @FXML
    private Label lblPermisos;

    @FXML
    private Button btnCerrarSesion;

    // =====================================================
    // T1.20 / T1.22
    // =====================================================

    @FXML
    private void initialize() {

        // Impide que Cajero o Bodega
        // entren al Dashboard Admin.
        if (!NavegacionRol.validarRol(
                Rol.ADMIN)) {

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
    // GESTIÓN DE USUARIOS
    // =====================================================

    @FXML
    private void onGestionUsuarios(
            ActionEvent event) {

        if (!NavegacionRol.validarPermiso(
                "GESTION_USUARIOS")) {

            return;
        }

        abrirModulo(
                "/org/esperanza/view/Usuarios.fxml",
                "Gestión de Usuarios - Librería La Esperanza",
                950,
                600
        );
    }

    // =====================================================
    // REPORTES
    // =====================================================

    @FXML
    private void onReportes(
            ActionEvent event) {

        if (!NavegacionRol.validarPermiso(
                "VER_REPORTES")) {

            return;
        }

        abrirModulo(
                "/org/esperanza/view/Reportes.fxml",
                "Reportes de uso y cambios - Librería La Esperanza",
                1100,
                650
        );
    }

    // =====================================================
    // CONFIGURACIÓN
    // =====================================================

    @FXML
    private void onConfiguracion(
            ActionEvent event) {

        if (!NavegacionRol.validarPermiso(
                "CONFIGURACION")) {

            return;
        }

        abrirModulo(
                "/org/esperanza/view/Configuracion.fxml",
                "Configuración - Librería La Esperanza",
                600,
                480
        );
    }

    // =====================================================
    // CERRAR SESIÓN
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

    // =====================================================
    // REDIRECCIÓN
    // =====================================================

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

    // =====================================================
    // ABRIR MÓDULOS
    // =====================================================

    private void abrirModulo(
            String rutaFXML,
            String titulo,
            double ancho,
            double alto) {

        URL recurso =
                getClass()
                        .getResource(
                                rutaFXML
                        );

        if (recurso == null) {

            mostrarError(
                    "No se encontró la vista: "
                    + rutaFXML
            );

            return;
        }

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            recurso
                    );

            Parent root =
                    loader.load();

            Stage stage =
                    new Stage();

            stage.setTitle(
                    titulo
            );

            stage.setScene(
                    new Scene(
                            root,
                            ancho,
                            alto
                    )
            );

            stage.centerOnScreen();

            stage.show();

        } catch (IOException e) {

            e.printStackTrace();

            mostrarError(
                    "No se pudo abrir el módulo: "
                    + e.getMessage()
            );
        }
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