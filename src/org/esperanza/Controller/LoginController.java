package org.esperanza.Controller;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import org.esperanza.Model.Usuario;
import org.esperanza.Service.AutenticacionService;
import org.esperanza.Service.ResultadoLogin;

public class LoginController {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Button btnIngresar;

    @FXML
    private Label lblMensaje;

    private final AutenticacionService autenticacionService =
            new AutenticacionService();

    // =====================================================
    // BOTÓN INGRESAR
    // =====================================================

    @FXML
    private void onIngresarClick(ActionEvent event) {

        String username = txtUsername.getText();
        String password = txtPassword.getText();

        ResultadoLogin resultado =
                autenticacionService.autenticar(
                        username,
                        password
                );

        switch (resultado.getEstado()) {

            case EXITO:

                Usuario usuario =
                        resultado.getUsuario();

                mostrarExito(
                        "Bienvenido, "
                        + usuario.getNombre()
                        + " ("
                        + usuario.getRol()
                        + ")"
                );

                abrirDashboard(
                        usuario,
                        event
                );

                break;

            case CAMPOS_VACIOS:

                mostrarAdvertencia(
                        "Campos incompletos",
                        "Completa usuario y contraseña."
                );

                break;

            case USUARIO_NO_ENCONTRADO:

                mostrarAdvertencia(
                        "Usuario no encontrado",
                        "El usuario no existe."
                );

                break;

            case CONTRASENA_INCORRECTA:

                mostrarAdvertencia(
                        "Credenciales incorrectas",
                        "Usuario o contraseña incorrectos."
                );

                break;

            case USUARIO_INACTIVO:

                mostrarAdvertencia(
                        "Usuario inactivo",
                        "Este usuario está inactivo. Contacta al administrador."
                );

                break;

            default:

                mostrarAdvertencia(
                        "Error",
                        "No fue posible iniciar sesión."
                );

                break;
        }
    }

    // =====================================================
    // ABRIR DASHBOARD
    // =====================================================

    private void abrirDashboard(
            Usuario usuario,
            ActionEvent event) {

        if (usuario == null) {

            mostrarAdvertencia(
                    "Error",
                    "No se pudo obtener la información del usuario."
            );

            return;
        }

        if (usuario.getRol() == null) {

            mostrarAdvertencia(
                    "Error",
                    "El usuario no tiene un rol asignado."
            );

            return;
        }

        String rol =
                usuario.getRol()
                        .trim()
                        .toUpperCase();

        // Por ahora solamente existe el dashboard de ADMIN
        if (!rol.equals("ADMIN")) {

            mostrarAdvertencia(
                    "Acceso",
                    "El dashboard para el rol "
                    + usuario.getRol()
                    + " todavía no está disponible."
            );

            return;
        }

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/org/esperanza/view/DashboardAdmin.fxml"
                            )
                    );

            Parent root =
                    loader.load();

            // =================================================
            // OBTENER EL CONTROLADOR DEL DASHBOARD
            // =================================================

            DashboardAdminController dashboardController =
                    loader.getController();

            // =================================================
            // ENVIAR EL USUARIO QUE INICIÓ SESIÓN
            // Esto permite obtener posteriormente su ID
            // para cambiar la contraseña.
            // =================================================

            dashboardController.setUsuarioActual(
                    usuario
            );

            // =================================================
            // CAMBIAR DE PANTALLA
            // =================================================

            Stage stage =
                    (Stage) ((Button) event.getSource())
                            .getScene()
                            .getWindow();

            stage.setScene(
                    new Scene(root)
            );

            stage.setTitle(
                    "Librería La Esperanza - Panel Administrador"
            );

            stage.centerOnScreen();

        } catch (IOException e) {

            e.printStackTrace();

            mostrarAdvertencia(
                    "Error",
                    "No se pudo cargar el dashboard: "
                    + e.getMessage()
            );
        }
    }

    // =====================================================
    // MOSTRAR ADVERTENCIAS
    // =====================================================

    private void mostrarAdvertencia(
            String titulo,
            String mensaje) {

        Alert alert =
                new Alert(
                        Alert.AlertType.WARNING
                );

        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        alert.showAndWait();

        if (lblMensaje != null) {

            lblMensaje.getStyleClass()
                    .setAll("lbl-mensaje");

            lblMensaje.setStyle(
                    "-fx-text-fill: #ff6b6b;"
            );

            lblMensaje.setText(mensaje);
        }
    }

    // =====================================================
    // MOSTRAR MENSAJE DE ÉXITO
    // =====================================================

    private void mostrarExito(
            String mensaje) {

        if (lblMensaje != null) {

            lblMensaje.getStyleClass()
                    .setAll("lbl-mensaje");

            lblMensaje.setStyle(
                    "-fx-text-fill: #4ade80;"
            );

            lblMensaje.setText(mensaje);
        }
    }
}