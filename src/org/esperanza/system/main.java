package org.esperanza.system;

import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import org.esperanza.Model.Usuario;
import org.esperanza.Service.NavegacionRol;
import org.esperanza.Service.SesionUsuario;
import org.esperanza.dao.UsuarioDao;

/**
 * Punto de entrada para probar US-1.3.
 *
 * Esta rama todavía no contiene el login de develop,
 * por eso utiliza temporalmente el primer usuario activo.
 */
public class main {

    public static class Ventana
            extends Application {

        @Override
        public void start(
                Stage stage) {

            UsuarioDao usuarioDao =
                    new UsuarioDao();

           Usuario usuarioInicial =
        usuarioDao
                .listarUsuariosActivos()
                .stream()
                .filter(usuario ->
                        "cajero1".equalsIgnoreCase(
                                usuario.getUsrname()
                        )
                )
                .findFirst()
                .orElse(null);

            if (usuarioInicial == null) {

                mostrarError(
                        "No existen usuarios activos "
                        + "para iniciar la aplicación."
                );

                return;
            }

            // T1.16
            boolean sesionCorrecta =
                    SesionUsuario
                            .getInstancia()
                            .iniciarSesion(
                                    usuarioInicial
                            );

            if (!sesionCorrecta) {

                mostrarError(
                        "El usuario está inactivo "
                        + "o tiene un rol no válido."
                );

                return;
            }

            // T1.21
            boolean dashboardAbierto =
                    NavegacionRol
                            .abrirDashboardSegunRol(
                                    stage
                            );

            if (!dashboardAbierto) {

                SesionUsuario
                        .getInstancia()
                        .cerrarSesion();

                return;
            }

            stage.setOnCloseRequest(
                    e -> SesionUsuario
                            .getInstancia()
                            .cerrarSesion()
            );
        }

        private void mostrarError(
                String mensaje) {

            Alert alert =
                    new Alert(
                            Alert.AlertType.ERROR
                    );

            alert.setTitle(
                    "Error"
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

    public static void main(
            String[] args) {

        Application.launch(
                Ventana.class,
                args
        );
    }
}