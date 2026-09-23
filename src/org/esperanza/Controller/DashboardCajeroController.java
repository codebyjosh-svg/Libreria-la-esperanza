package org.esperanza.controller;

import java.io.IOException;
import java.util.ArrayList;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.esperanza.service.NavegacionRol;
import org.esperanza.service.Pantallas;
import org.esperanza.service.SesionUsuario;

public class DashboardCajeroController {

    @FXML
    private Label lblBienvenida;

    @FXML
    private Label lblRol;

    @FXML
    private Label lblPermisos;

    @FXML
    private void initialize() {

        SesionUsuario sesion =
                SesionUsuario.getInstancia();

        if (!sesion.esCajero()) {

            throw new SecurityException(
                    "Este panel es para Cajero."
            );
        }

        lblBienvenida.setText(
                sesion.getNombreCompleto()
        );

        lblRol.setText(
                "Cajero"
        );

        lblPermisos.setText(
                "Ventas | Devoluciones | Consultar productos"
        );
    }

    private Stage ventana() {

        return (Stage) lblBienvenida
                .getScene()
                .getWindow();
    }

    @FXML
    private void onNuevaVenta() {

        if (!SesionUsuario
                .getInstancia()
                .esCajero()) {
            return;
        }

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/org/esperanza/view/CarritoVenta.fxml"
                            )
                    );

            Parent root = loader.load();

            CarritoVentaController controller =
                    loader.getController();

            controller.setIdUsuario(
                    SesionUsuario
                            .getInstancia()
                            .getUsuarioActual()
                            .getId()
            );

            Stage stage = ventana();

            stage.setOnCloseRequest(null);

            stage.setScene(
                    new Scene(root)
            );

            stage.setTitle(
                    "Nueva venta - Librería La Esperanza"
            );

            stage.sizeToScene();
            stage.centerOnScreen();

        } catch (Exception ex) {

            Pantallas.error(
                    ex.getMessage()
            );
        }
    }

    @FXML
    private void onClientes() {

        Pantallas.catalogo(
                lblBienvenida,
                "clientes",
                "Clientes"
        );
    }

    @FXML
    private void onDevoluciones() {

        if (!NavegacionRol.validarPermiso(
                "DEVOLUCIONES")) {
            return;
        }

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/org/esperanza/view/Devoluciones.fxml"
                            )
                    );

            Parent root = loader.load();

            Stage stage = ventana();

            DevolucionesController controller =
                    loader.getController();

            stage.setOnCloseRequest(event -> {
                event.consume();

                if (controller == null
                        || !controller.estaOcupado()) {
                    NavegacionRol.abrirDashboardSegunRol(stage);
                }
            });

            stage.setScene(
                    new Scene(root)
            );

            stage.setTitle(
                    "Devoluciones de ventas - Librería La Esperanza"
            );

            stage.sizeToScene();
            stage.centerOnScreen();

        } catch (IOException ex) {

            Pantallas.error(
                    "No se pudo abrir Devoluciones.\n"
                    + ex.getMessage()
            );
        }
    }

    @FXML
    private void onConsultarProductos() {

        ModulosCajero.stock(
                ventana()
        );
    }

    @FXML
    private void onMisVentas() {

        ModulosCajero.ventas(
                ventana()
        );
    }

    @FXML
    private void onEditarClientes() {

        EdicionClientes.mostrar(
                ventana()
        );
    }

    @FXML
    private void onCambiarContrasena() {

        if (SesionUsuario
                .getInstancia()
                .getUsuarioActual() == null) {

            Pantallas.error(
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

            Parent root = loader.load();

            CambioContrasenaController controller =
                    loader.getController();

            controller.setIdUsuarioActual(
                    SesionUsuario
                            .getInstancia()
                            .getUsuarioActual()
                            .getId()
            );

            Stage modal = new Stage();

            modal.setTitle(
                    "Cambiar Contraseña"
            );

            modal.setScene(
                    new Scene(root)
            );

            modal.initOwner(
                    lblBienvenida
                            .getScene()
                            .getWindow()
            );

            modal.initModality(
                    Modality.WINDOW_MODAL
            );

            modal.setResizable(false);
            modal.centerOnScreen();
            modal.showAndWait();

        } catch (Exception ex) {

            Pantallas.error(
                    "No se pudo abrir la pantalla de Cambio de Contraseña.\n"
                    + ex.getMessage()
            );
        }
    }

    @FXML
    private void onCerrarSesion() {

        try {

            Parent root =
                    FXMLLoader.load(
                            getClass().getResource(
                                    "/org/esperanza/view/Login.fxml"
                            )
                    );

            Stage principal =
                    ventana();

            while (principal
                    .getOwner()
                    instanceof Stage) {

                principal =
                        (Stage) principal
                                .getOwner();
            }

            principal.setOnCloseRequest(
                    null
            );

            SesionUsuario
                    .getInstancia()
                    .cerrarSesion();

            principal.setScene(
                    new Scene(root)
            );

            for (Window otra
                    : new ArrayList<>(
                            Window.getWindows()
                    )) {

                if (otra != principal) {

                    if (otra instanceof Stage stage) {

                        stage.setOnCloseRequest(
                                null
                        );
                    }

                    otra.hide();
                }
            }

            principal.setTitle(
                    "Librería La Esperanza"
            );

            principal.sizeToScene();
            principal.centerOnScreen();
            principal.show();
            principal.toFront();

        } catch (Exception ex) {

            Pantallas.error(
                    ex.getMessage()
            );
        }
    }
}
