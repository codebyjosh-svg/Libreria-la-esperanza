package org.esperanza.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.esperanza.dao.UsuarioDao;
import org.esperanza.Model.Usuario;
import org.esperanza.Service.SesionUsuario;

public class ConfiguracionController {
    @FXML private Label lblUsuario;
    @FXML private PasswordField txtNueva;
    @FXML private PasswordField txtConfirmar;

    private final UsuarioDao usuarioDao = new UsuarioDao();

    @FXML
    private void initialize() {
        actualizarUsuario();
    }

    private void actualizarUsuario() {
        Usuario u = SesionUsuario.getInstancia().getUsuarioActual();
        lblUsuario.setText(u == null ? "Sin usuario seleccionado" : u.getUsrname());
    }

    @FXML
    private void onCambiarContrasena() {
        Usuario u = SesionUsuario.getInstancia().getUsuarioActual();
        String nueva = txtNueva.getText();
        String confirma = txtConfirmar.getText();

        if (u == null) {
            aviso("No hay usuario seleccionado.");
            return;
        }
        if (nueva.length() < 6) {
            aviso("La contraseña debe tener al menos 6 caracteres.");
            return;
        }
        if (!nueva.equals(confirma)) {
            aviso("Las contraseñas no coinciden.");
            return;
        }

        if (usuarioDao.cambiarContrasena(u.getId(), nueva)) {
            SesionUsuario.getInstancia().registrarCambio(
                "CAMBIO_CONTRASENA",
                "Se cambió la contraseña del usuario " + u.getUsrname());
            info("Contraseña actualizada correctamente.");
            txtNueva.clear();
            txtConfirmar.clear();
        } else {
            error("No fue posible cambiar la contraseña.");
        }
    }

    private void aviso(String m) {
        new Alert(Alert.AlertType.WARNING, m, ButtonType.OK).showAndWait();
    }
    private void info(String m) {
        new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK).showAndWait();
    }
    private void error(String m) {
        new Alert(Alert.AlertType.ERROR, m, ButtonType.OK).showAndWait();
    }
}
