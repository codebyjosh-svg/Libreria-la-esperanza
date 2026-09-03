package org.esperanza.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class CambioContrasenaDashboard {

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

    @FXML
    private void handleGuardar() {
        String contrasenaActual = txtContrasenaActual.getText();
        String nuevaContrasena = txtNuevaContrasena.getText();
        String confirmarContrasena = txtConfirmarContrasena.getText();
    }

    @FXML
    private void handleCancelar() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }
}