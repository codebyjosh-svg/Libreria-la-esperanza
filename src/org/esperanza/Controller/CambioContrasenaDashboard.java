package org.esperanza.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import org.esperanza.dao.UsuarioDao;

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

    private int idUsuarioActual = -1;

    private final UsuarioDao usuarioDao =
            new UsuarioDao();

    public void setIdUsuarioActual(
            int idUsuarioActual) {

        this.idUsuarioActual =
                idUsuarioActual;
    }

    @FXML
    private void handleGuardar() {

        if (idUsuarioActual <= 0) {

            mostrarAlerta(
                    "Error",
                    "No se pudo identificar al usuario actual.",
                    Alert.AlertType.ERROR
            );

            return;
        }

        String actual =
                txtContrasenaActual.getText();

        String nueva =
                txtNuevaContrasena.getText();

        String confirmar =
                txtConfirmarContrasena.getText();

        // T1.23 / T1.26
        if (actual == null
                || actual.isEmpty()
                || nueva == null
                || nueva.isEmpty()
                || confirmar == null
                || confirmar.isEmpty()) {

            mostrarAlerta(
                    "Campos incompletos",
                    "Todos los campos son obligatorios.",
                    Alert.AlertType.WARNING
            );

            return;
        }

        // T1.26
        if (!nueva.equals(confirmar)) {

            mostrarAlerta(
                    "Contraseñas diferentes",
                    "La nueva contraseña y la confirmación no coinciden.",
                    Alert.AlertType.ERROR
            );

            return;
        }

        if (nueva.length() < 6) {

            mostrarAlerta(
                    "Contraseña inválida",
                    "La nueva contraseña debe tener al menos 6 caracteres.",
                    Alert.AlertType.WARNING
            );

            return;
        }

        // T1.24
        if (!usuarioDao.validarPasswordActual(
                idUsuarioActual,
                actual)) {

            mostrarAlerta(
                    "Contraseña incorrecta",
                    "La contraseña actual no es correcta.",
                    Alert.AlertType.ERROR
            );

            return;
        }

        // Evita usar nuevamente la misma contraseña
        if (actual.equals(nueva)) {

            mostrarAlerta(
                    "Contraseña inválida",
                    "La nueva contraseña debe ser diferente a la actual.",
                    Alert.AlertType.WARNING
            );

            return;
        }

        // T1.25
        boolean actualizado =
                usuarioDao.actualizarPassword(
                        idUsuarioActual,
                        nueva
                );

        if (actualizado) {

            mostrarAlerta(
                    "Cambio exitoso",
                    "La contraseña se actualizó correctamente.",
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

        Alert alerta =
                new Alert(tipo);

        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);

        alerta.showAndWait();
    }
}