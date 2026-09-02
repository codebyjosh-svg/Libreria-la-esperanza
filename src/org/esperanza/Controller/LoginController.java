package org.esperanza.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
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

    private final AutenticacionService autenticacionService = new AutenticacionService();

    @FXML
    private void onIngresarClick(ActionEvent event) {
        ResultadoLogin resultado = autenticacionService.autenticar(
                txtUsername.getText(), txtPassword.getText());

        switch (resultado.getEstado()) {
            case EXITO:
                mostrarExito("Bienvenido, " + resultado.getUsuario().getNombre()
                        + " (" + resultado.getUsuario().getRol() + ")");
                break;
            case CAMPOS_VACIOS:
                mostrarAdvertencia("Campos incompletos", "Completa usuario y contraseña.");
                break;
            case USUARIO_NO_ENCONTRADO:
                mostrarAdvertencia("Usuario no encontrado", "El usuario no existe.");
                break;
            case CONTRASENA_INCORRECTA:
                mostrarAdvertencia("Credenciales incorrectas", "Usuario o contraseña incorrectos.");
                break;
            case USUARIO_INACTIVO:
                mostrarAdvertencia("Usuario inactivo", "Este usuario está inactivo. Contacta al administrador.");
                break;
        }
    }

    private void mostrarAdvertencia(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();

  
        lblMensaje.getStyleClass().setAll("lbl-mensaje");
        lblMensaje.setStyle("-fx-text-fill: #ff6b6b;");
        lblMensaje.setText(mensaje);
    }

    private void mostrarExito(String mensaje) {
        lblMensaje.getStyleClass().setAll("lbl-mensaje");
        lblMensaje.setStyle("-fx-text-fill: #4ade80;");
        lblMensaje.setText(mensaje);
    }
}