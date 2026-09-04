package org.esperanza.Controller;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.esperanza.Model.Usuario;

public class DashboardAdminController {

    @FXML
    private Label lblUsuario;

    private Usuario usuarioActual;

    // =====================================================
    // RECIBIR USUARIO DESDE LOGIN
    // =====================================================

    public void setUsuarioActual(Usuario usuarioActual) {

        this.usuarioActual = usuarioActual;

        if (usuarioActual != null && lblUsuario != null) {

            lblUsuario.setText(
                    usuarioActual.getUsrname()
            );
        }
    }

    // =====================================================
    // ABRIR GESTIÓN DE USUARIOS
    // =====================================================

    @FXML
    private void onUsuariosClick() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/org/esperanza/view/Usuarios.fxml"
                            )
                    );

            Parent root =
                    loader.load();

            Stage stage =
                    (Stage) lblUsuario
                            .getScene()
                            .getWindow();

            stage.setScene(
                    new Scene(root)
            );

            stage.setTitle(
                    "Gestión de Usuarios - Librería La Esperanza"
            );

            stage.centerOnScreen();

        } catch (IOException e) {

            e.printStackTrace();

            mostrarError(
                    "No se pudo abrir Gestión de Usuarios.\n"
                    + e.getMessage()
            );
        }
    }

    // =====================================================
    // US-1.4 - CAMBIO DE CONTRASEÑA
    // =====================================================

    @FXML
    private void onCambiarContrasenaClick() {

        if (usuarioActual == null) {

            mostrarError(
                    "No se pudo identificar al usuario que inició sesión."
            );

            return;
        }

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/org/esperanza/view/CambioContrasenaDashboard.fxml"
                            )
                    );

            Parent root =
                    loader.load();

            CambioContrasenaController controller =
                    loader.getController();

            // Mandamos el ID REAL del usuario logueado
            controller.setIdUsuarioActual(
                    usuarioActual.getId()
            );

            Stage ventana =
                    new Stage();

            ventana.setTitle(
                    "Cambiar Contraseña"
            );

            ventana.setScene(
                    new Scene(root)
            );

            // Hace que esta ventana dependa del dashboard
            ventana.initOwner(
                    lblUsuario
                            .getScene()
                            .getWindow()
            );

            ventana.initModality(
                    Modality.WINDOW_MODAL
            );

            ventana.setResizable(false);

            ventana.centerOnScreen();

            ventana.showAndWait();

        } catch (IOException e) {

            e.printStackTrace();

            mostrarError(
                    "No se pudo abrir la pantalla de Cambio de Contraseña.\n"
                    + e.getMessage()
            );
        }
    }

    // =====================================================
    // LIBROS
    // =====================================================

    @FXML
    private void onLibrosClick() {

        mostrarEnConstruccion(
                "Libros"
        );
    }

    // =====================================================
    // AUTORES
    // =====================================================

    @FXML
    private void onAutoresClick() {

        mostrarEnConstruccion(
                "Autores"
        );
    }

    // =====================================================
    // CATEGORÍAS
    // =====================================================

    @FXML
    private void onCategoriasClick() {

        mostrarEnConstruccion(
                "Categorías"
        );
    }

    // =====================================================
    // EDITORIALES
    // =====================================================

    @FXML
    private void onEditorialesClick() {

        mostrarEnConstruccion(
                "Editoriales"
        );
    }

    // =====================================================
    // VENTAS
    // =====================================================

    @FXML
    private void onVentasClick() {

        mostrarEnConstruccion(
                "Ventas"
        );
    }

    // =====================================================
    // AUTORES-LIBRO
    // =====================================================

    @FXML
    private void onAutoresLibroClick() {

        mostrarEnConstruccion(
                "Autores-Libro"
        );
    }

    // =====================================================
    // DETALLE VENTAS
    // =====================================================

    @FXML
    private void onDetalleVentasClick() {

        mostrarEnConstruccion(
                "Detalle Ventas"
        );
    }

    // =====================================================
    // CLIENTES
    // =====================================================

    @FXML
    private void onClientesClick() {

        mostrarEnConstruccion(
                "Clientes"
        );
    }

    // =====================================================
    // CERRAR SESIÓN
    // =====================================================

    @FXML
    private void onCerrarSesionClick() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/org/esperanza/view/Login.fxml"
                            )
                    );

            Parent root =
                    loader.load();

            Stage stage =
                    (Stage) lblUsuario
                            .getScene()
                            .getWindow();

            stage.setScene(
                    new Scene(root)
            );

            stage.setTitle(
                    "Librería La Esperanza"
            );

            stage.centerOnScreen();

        } catch (IOException e) {

            e.printStackTrace();

            mostrarError(
                    "No se pudo cerrar la sesión.\n"
                    + e.getMessage()
            );
        }
    }

    // =====================================================
    // MENSAJE MÓDULO EN CONSTRUCCIÓN
    // =====================================================

    private void mostrarEnConstruccion(
            String modulo) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(
                modulo
        );

        alert.setHeaderText(
                null
        );

        alert.setContentText(
                "Módulo de "
                + modulo
                + " en construcción."
        );

        alert.showAndWait();
    }

    // =====================================================
    // MOSTRAR ERROR
    // =====================================================

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