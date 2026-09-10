package org.esperanza.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import org.esperanza.Model.Usuario;
import org.esperanza.Service.AutenticacionService;
import org.esperanza.Service.NavegacionRol;
import org.esperanza.Service.ResultadoLogin;
import org.esperanza.Service.SesionUsuario;

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

    @FXML
    private void initialize() {

        if (lblMensaje != null) {
            lblMensaje.setText("");
        }
    }

    // =====================================================
    // BOTÓN INGRESAR
    // =====================================================

    @FXML
    private void onIngresarClick(ActionEvent event) {

        String username =
                txtUsername.getText();

        String password =
                txtPassword.getText();

        ResultadoLogin resultado =
                autenticacionService.autenticar(
                        username,
                        password
                );

        switch (resultado.getEstado()) {

            case EXITO -> {

                Usuario usuario =
                        resultado.getUsuario();

                if (usuario == null) {

                    mostrarAdvertencia(
                            "Error",
                            "No se pudo obtener la información del usuario."
                    );

                    return;
                }

                /*
                 * T1.16
                 * Guardar usuario autenticado en sesión.
                 */
                boolean sesionCorrecta =
                        SesionUsuario
                                .getInstancia()
                                .iniciarSesion(usuario);

                if (!sesionCorrecta) {

                    mostrarAdvertencia(
                            "Acceso denegado",
                            "El usuario no tiene un rol válido "
                            + "o se encuentra inactivo."
                    );

                    return;
                }

                mostrarExito(
                        "Bienvenido, "
                        + usuario.getNombre()
                        + " ("
                        + usuario.getRol()
                        + ")"
                );

                Stage stage =
                        (Stage) btnIngresar
                                .getScene()
                                .getWindow();

                /*
                 * T1.21
                 * Ya NO abrimos DashboardAdmin
                 * directamente.
                 *
                 * NavegacionRol decide cuál abrir:
                 *
                 * ADMIN  -> DashboardAdmin
                 * CAJERO -> DashboardCajero
                 * BODEGA -> DashboardBodega
                 */
                boolean abierto =
                        NavegacionRol
                                .abrirDashboardSegunRol(
                                        stage
                                );

                if (!abierto) {

                    SesionUsuario
                            .getInstancia()
                            .cerrarSesion();
                }
            }

            case CAMPOS_VACIOS -> {

                mostrarAdvertencia(
                        "Campos incompletos",
                        "Completa usuario y contraseña."
                );
            }

            case USUARIO_NO_ENCONTRADO -> {

                mostrarAdvertencia(
                        "Usuario no encontrado",
                        "El usuario no existe."
                );
            }

            case CONTRASENA_INCORRECTA -> {

                mostrarAdvertencia(
                        "Credenciales incorrectas",
                        "Usuario o contraseña incorrectos."
                );
            }

            case USUARIO_INACTIVO -> {

                mostrarAdvertencia(
                        "Usuario inactivo",
                        "Este usuario está inactivo. "
                        + "Contacta al administrador."
                );
            }

            default -> {

                mostrarAdvertencia(
                        "Error",
                        "No fue posible iniciar sesión."
                );
            }
        }
    }

    // =====================================================
    // ADVERTENCIAS
    // =====================================================

    private void mostrarAdvertencia(
            String titulo,
            String mensaje) {

        if (lblMensaje != null) {
            lblMensaje.setText(mensaje);
        }

        Alert alert =
                new Alert(
                        Alert.AlertType.WARNING
                );

        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        alert.showAndWait();
    }

    // =====================================================
    // MENSAJE DE ÉXITO
    // =====================================================

    private void mostrarExito(
            String mensaje) {

        if (lblMensaje != null) {
            lblMensaje.setText("");
        }

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(
                "Inicio de sesión"
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