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

    private int idUsuarioActual = -1;

    private final UsuarioDao usuarioDao = new UsuarioDao();

    public void setIdUsuarioActual(int idUsuarioActual) {
        this.idUsuarioActual = idUsuarioActual;
    }

    @FXML
    private void handleGuardar() {

        // Comprobar que el Dashboard envió un usuario válido
        if (idUsuarioActual <= 0) {

            mostrarAlerta(
                    "Error",
                    "No se pudo identificar al usuario que inició sesión.",
                    Alert.AlertType.ERROR
            );

            return;
        }

        String actual = txtContrasenaActual.getText();
        String nueva = txtNuevaContrasena.getText();
        String confirmar = txtConfirmarContrasena.getText();

        if (actual == null
                || actual.isBlank()
                || nueva == null
                || nueva.isBlank()
                || confirmar == null
                || confirmar.isBlank()) {

            mostrarAlerta(
                    "Error",
                    "Todos los campos son obligatorios.",
                    Alert.AlertType.ERROR
            );
            return;
        }

        if (!nueva.equals(confirmar)) {

            mostrarAlerta(
                    "Error",
                    "La confirmación no coincide con la nueva contraseña.",
                    Alert.AlertType.ERROR
            );
            return;
        }

        if (nueva.length() < 6) {

            mostrarAlerta(
                    "Error",
                    "La nueva contraseña debe tener al menos 6 caracteres.",
                    Alert.AlertType.WARNING
            );

            return;
        }

        if (actual.equals(nueva)) {

            mostrarAlerta(
                    "Error",
                    "La nueva contraseña debe ser diferente a la contraseña actual.",
                    Alert.AlertType.WARNING
            );
            return;
        }

        boolean passwordCorrecta =
                usuarioDao.validarPasswordActual(
                        idUsuarioActual,
                        actual
                );

        if (!passwordCorrecta) {
            mostrarAlerta(
                    "Error",
                    "La contraseña actual es incorrecta.",
                    Alert.AlertType.ERROR
            );
            return;
        }

        boolean actualizado =
                usuarioDao.actualizarPassword(
                        idUsuarioActual,
                        nueva);

        if (actualizado) {
            mostrarAlerta(
                    "Éxito",
                    "Contraseña actualizada correctamente.",
                    Alert.AlertType.INFORMATION
            );
            cerrarVentana();
        } else {

            mostrarAlerta(
                    "Error",
                    "No se pudo actualizar la contraseña.",
                    Alert.AlertType.ERROR
            );
        }
    }

    @FXML
    private void handleCancelar() {
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage =
                (Stage) btnCancelar
                        .getScene()
                        .getWindow();

        stage.close();
    }

    private void mostrarAlerta(
            String titulo,
            String mensaje,
            Alert.AlertType tipo) {

        Alert alerta = new Alert(tipo);

        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}