package org.esperanza.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import org.esperanza.dao.UsuarioDao;

public class CambioContrasenaController {

    @FXML
    private PasswordField txtContrasenaActual;

    @FXML
    private PasswordField txtNuevaContrasena;

    @FXML
    private PasswordField txtConfirmarContrasena;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnCancelar;

    private int idUsuarioActual = 1; 

    private UsuarioDao usuarioDao = new UsuarioDao();

    @FXML
    private void handleGuardar() {
        String actual = txtContrasenaActual.getText();
        String nueva = txtNuevaContrasena.getText();
        String confirmar = txtConfirmarContrasena.getText();

        if (actual.isEmpty() || nueva.isEmpty() || confirmar.isEmpty()) {
            mostrarAlerta("Error", "Todos los campos son obligatorios", Alert.AlertType.ERROR);
            return;
        }

        if (!nueva.equals(confirmar)) {
            mostrarAlerta("Error", "La confirmación no coincide con la nueva contraseña", Alert.AlertType.ERROR);
            return;
        }

        if (!usuarioDao.validarPasswordActual(idUsuarioActual, actual)) {
            mostrarAlerta("Error", "La contraseña actual es incorrecta", Alert.AlertType.ERROR);
            return;
        }

        boolean actualizado = usuarioDao.actualizarPassword(idUsuarioActual, nueva);
        if (actualizado) {
            mostrarAlerta("Éxito", "Contraseña actualizada correctamente", Alert.AlertType.INFORMATION);
            cerrarVentana();
        } else {
            mostrarAlerta("Error", "No se pudo actualizar la contraseña", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancelar() {
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}