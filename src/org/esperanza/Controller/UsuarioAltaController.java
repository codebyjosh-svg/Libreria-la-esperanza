package org.esperanza.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import org.esperanza.dao.UsuarioDao;
import org.esperanza.service.SesionUsuario;
import org.esperanza.util.PasswordUtil;

public class UsuarioAltaController {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private PasswordField txtConfirmar;

    @FXML
    private ComboBox<String> cmbRol;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtApellido;

    @FXML
    private TextField txtCorreo;

    private final UsuarioDao usuarioDao = new UsuarioDao();

    @FXML
    private void initialize() {

        if (!puedeGestionarUsuarios()) {

            txtUsername.setDisable(true);
            txtPassword.setDisable(true);
            txtConfirmar.setDisable(true);
            txtNombre.setDisable(true);
            txtApellido.setDisable(true);
            txtCorreo.setDisable(true);
            cmbRol.setDisable(true);

            return;
        }

        cmbRol.getItems().addAll(
                "ADMIN",
                "CAJERO",
                "BODEGA"
        );

        cmbRol.getSelectionModel().selectFirst();
    }

    @FXML
    private void onGuardar(ActionEvent event) {

        if (!puedeGestionarUsuarios()) {

            mostrarError(
                    "Solo un administrador con sesión activa "
                    + "puede registrar usuarios."
            );

            return;
        }

        if (!validarCampos()) {
            return;
        }

        String username = txtUsername
                .getText()
                .trim();

        String correo = txtCorreo
                .getText()
                .trim();

        if (usuarioDao.existeUsername(username)) {

            mostrarAdvertencia(
                    "Usuario duplicado",
                    "El nombre de usuario ya existe."
            );

            txtUsername.requestFocus();
            return;
        }

        String passwordHash = PasswordUtil.hashSHA256(
                txtPassword.getText()
        );

        String rol = cmbRol
                .getValue()
                .trim()
                .toUpperCase();

        boolean guardado = usuarioDao.registrarUsuario(
                username,
                passwordHash,
                rol,
                txtNombre.getText().trim(),
                txtApellido.getText().trim(),
                correo
        );

        if (guardado) {

            Alert alerta = new Alert(
                    Alert.AlertType.INFORMATION
            );

            alerta.setTitle("Alta de usuario");
            alerta.setHeaderText(null);

            alerta.setContentText(
                    "Usuario registrado correctamente."
            );

            alerta.showAndWait();

            cerrar();

        } else {

            mostrarError(
                    "No fue posible registrar el usuario. "
                    + "Revisa la conexión y la base de datos."
            );
        }
    }

    private boolean puedeGestionarUsuarios() {

        SesionUsuario sesion =
                SesionUsuario.getInstancia();

        return sesion.haySesionActiva()
                && sesion.getUsuarioActual().isActivo()
                && sesion.esAdmin();
    }

    private boolean validarCampos() {

        if (vacio(txtUsername)
                || vacio(txtPassword)
                || vacio(txtConfirmar)
                || vacio(txtNombre)
                || vacio(txtApellido)
                || vacio(txtCorreo)
                || cmbRol.getValue() == null) {

            mostrarAdvertencia(
                    "Campos incompletos",
                    "Todos los campos son obligatorios."
            );

            return false;
        }

        if (!txtUsername
                .getText()
                .trim()
                .matches("[A-Za-z0-9._-]{4,20}")) {

            mostrarAdvertencia(
                    "Usuario inválido",
                    "Usa de 4 a 20 caracteres: "
                    + "letras, números, punto, "
                    + "guion o guion bajo."
            );

            txtUsername.requestFocus();
            return false;
        }

        if (txtPassword.getText().length() < 6) {

            mostrarAdvertencia(
                    "Contraseña inválida",
                    "La contraseña debe tener "
                    + "al menos 6 caracteres."
            );

            txtPassword.requestFocus();
            return false;
        }

        if (!txtPassword
                .getText()
                .equals(txtConfirmar.getText())) {

            mostrarAdvertencia(
                    "Contraseñas diferentes",
                    "Las contraseñas no coinciden."
            );

            txtConfirmar.requestFocus();
            return false;
        }

        if (!txtNombre
                .getText()
                .trim()
                .matches(
                        "[A-Za-zÁÉÍÓÚáéíóúÑñ ]{2,40}"
                )) {

            mostrarAdvertencia(
                    "Nombre inválido",
                    "El nombre solo debe contener "
                    + "letras y espacios."
            );

            txtNombre.requestFocus();
            return false;
        }

        if (!txtApellido
                .getText()
                .trim()
                .matches(
                        "[A-Za-zÁÉÍÓÚáéíóúÑñ ]{2,40}"
                )) {

            mostrarAdvertencia(
                    "Apellido inválido",
                    "El apellido solo debe contener "
                    + "letras y espacios."
            );

            txtApellido.requestFocus();
            return false;
        }

        String correo = txtCorreo
                .getText()
                .trim();

        if (!validarCorreo(correo)) {

            mostrarAdvertencia(
                    "Correo inválido",
                    "Ingresa un correo electrónico válido.\n\n"
                    + "Ejemplo: usuario@gmail.com\n\n"
                    + "No se permite:\n"
                    + "• Punto al inicio o antes del @\n"
                    + "• Puntos consecutivos\n"
                    + "• Espacios\n"
                    + "• Más de un @\n"
                    + "• Dominio incompleto"
            );

            txtCorreo.requestFocus();
            return false;
        }

        return true;
    }

    private boolean validarCorreo(String correo) {

        if (correo == null
                || correo.isBlank()) {

            return false;
        }

        correo = correo.trim();

        /*
         * No permitir espacios.
         */
        if (correo.contains(" ")) {
            return false;
        }

        /*
         * Debe existir solamente un @.
         */
        int primerArroba =
                correo.indexOf('@');

        int ultimoArroba =
                correo.lastIndexOf('@');

        if (primerArroba <= 0
                || primerArroba != ultimoArroba) {

            return false;
        }

        String usuario =
                correo.substring(
                        0,
                        primerArroba
                );

        String dominio =
                correo.substring(
                        primerArroba + 1
                );

        /*
         * Validaciones antes del @.
         */
        if (usuario.startsWith(".")
                || usuario.endsWith(".")) {

            return false;
        }

        if (usuario.contains("..")) {
            return false;
        }

        if (!usuario.matches(
                "[A-Za-z0-9._%+-]+"
        )) {

            return false;
        }

        /*
         * Validaciones después del @.
         */
        if (dominio.isBlank()) {
            return false;
        }

        if (dominio.startsWith(".")
                || dominio.endsWith(".")
                || dominio.contains("..")) {

            return false;
        }

        /*
         * Ejemplos permitidos:
         *
         * gmail.com
         * outlook.com
         * kinal.edu.gt
         */
        if (!dominio.matches(
                "[A-Za-z0-9-]+"
                + "(\\.[A-Za-z0-9-]+)+"
        )) {

            return false;
        }

        String[] partesDominio =
                dominio.split("\\.");

        /*
         * Ninguna parte del dominio puede
         * iniciar o terminar con guion.
         */
        for (String parte : partesDominio) {

            if (parte.isBlank()) {
                return false;
            }

            if (parte.startsWith("-")
                    || parte.endsWith("-")) {

                return false;
            }
        }

        /*
         * La extensión debe tener
         * por lo menos dos letras.
         *
         * Ejemplos:
         * .com
         * .gt
         * .edu
         * .org
         */
        String extension =
                partesDominio[
                        partesDominio.length - 1
                ];

        if (!extension.matches(
                "[A-Za-z]{2,}"
        )) {

            return false;
        }

        return true;
    }

    private boolean vacio(TextField campo) {

        return campo.getText() == null
                || campo
                        .getText()
                        .trim()
                        .isEmpty();
    }

    private boolean vacio(
            PasswordField campo) {

        return campo.getText() == null
                || campo
                        .getText()
                        .isEmpty();
    }

    @FXML
    private void onCancelar(
            ActionEvent event) {

        cerrar();
    }

    private void cerrar() {

        Stage stage =
                (Stage) txtUsername
                        .getScene()
                        .getWindow();

        stage.close();
    }

    private void mostrarAdvertencia(
            String titulo,
            String mensaje) {

        Alert alerta = new Alert(
                Alert.AlertType.WARNING
        );

        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);

        alerta.showAndWait();
    }

    private void mostrarError(
            String mensaje) {

        Alert alerta = new Alert(
                Alert.AlertType.ERROR
        );

        alerta.setTitle("Error");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);

        alerta.showAndWait();
    }
}