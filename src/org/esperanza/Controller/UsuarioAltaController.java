package org.esperanza.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.esperanza.dao.UsuarioDao;
import org.esperanza.util.PasswordUtil;

public class UsuarioAltaController {
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmar;
    @FXML private ComboBox<String> cmbRol;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtCorreo;

    private final UsuarioDao usuarioDao = new UsuarioDao();

    @FXML
    private void initialize() {
        cmbRol.getItems().addAll("Administrador", "Empleado");
        cmbRol.getSelectionModel().selectFirst();
    }

    @FXML
    private void onGuardar(ActionEvent event) {
        if (!validarCampos()) return;

        String username = txtUsername.getText().trim();
        if (usuarioDao.existeUsername(username)) {
            mostrarAdvertencia("Usuario duplicado", "El nombre de usuario ya existe.");
            txtUsername.requestFocus();
            return;
        }

        boolean guardado = usuarioDao.registrarUsuario(
                username,
                PasswordUtil.hashSHA256(txtPassword.getText()),
                cmbRol.getValue(),
                txtNombre.getText().trim(),
                txtApellido.getText().trim(),
                txtCorreo.getText().trim());

        if (guardado) {
            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle("Alta de usuario");
            alerta.setHeaderText(null);
            alerta.setContentText("Usuario registrado correctamente.");
            alerta.showAndWait();
            cerrar();
        } else {
            mostrarError("No fue posible registrar el usuario. Revisa la conexión y la base de datos.");
        }
    }

    private boolean validarCampos() {
        if (vacio(txtUsername) || vacio(txtPassword) || vacio(txtConfirmar)
                || vacio(txtNombre) || vacio(txtApellido) || vacio(txtCorreo)
                || cmbRol.getValue() == null) {
            mostrarAdvertencia("Campos incompletos", "Todos los campos son obligatorios.");
            return false;
        }
        if (!txtUsername.getText().trim().matches("[A-Za-z0-9._-]{4,20}")) {
            mostrarAdvertencia("Usuario inválido", "Usa de 4 a 20 caracteres: letras, números, punto, guion o guion bajo.");
            txtUsername.requestFocus(); return false;
        }
        if (txtPassword.getText().length() < 6) {
            mostrarAdvertencia("Contraseña inválida", "La contraseña debe tener al menos 6 caracteres.");
            txtPassword.requestFocus(); return false;
        }
        if (!txtPassword.getText().equals(txtConfirmar.getText())) {
            mostrarAdvertencia("Contraseñas diferentes", "Las contraseñas no coinciden.");
            txtConfirmar.requestFocus(); return false;
        }
        if (!txtNombre.getText().trim().matches("[A-Za-zÁÉÍÓÚáéíóúÑñ ]{2,40}")) {
            mostrarAdvertencia("Nombre inválido", "El nombre solo debe contener letras y espacios.");
            txtNombre.requestFocus(); return false;
        }
        if (!txtApellido.getText().trim().matches("[A-Za-zÁÉÍÓÚáéíóúÑñ ]{2,40}")) {
            mostrarAdvertencia("Apellido inválido", "El apellido solo debe contener letras y espacios.");
            txtApellido.requestFocus(); return false;
        }
        if (!txtCorreo.getText().trim().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            mostrarAdvertencia("Correo inválido", "Ingresa un correo electrónico válido.");
            txtCorreo.requestFocus(); return false;
        }
        return true;
    }

    private boolean vacio(TextField campo) { return campo.getText() == null || campo.getText().trim().isEmpty(); }
    private boolean vacio(PasswordField campo) { return campo.getText() == null || campo.getText().isEmpty(); }

    @FXML private void onCancelar(ActionEvent event) { cerrar(); }
    private void cerrar() { ((Stage) txtUsername.getScene().getWindow()).close(); }

    private void mostrarAdvertencia(String titulo, String mensaje) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(titulo); a.setHeaderText(null); a.setContentText(mensaje); a.showAndWait();
    }
    private void mostrarError(String mensaje) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error"); a.setHeaderText(null); a.setContentText(mensaje); a.showAndWait();
    }
}
