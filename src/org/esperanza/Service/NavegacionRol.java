package org.esperanza.Service;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import org.esperanza.Model.Rol;

public final class NavegacionRol {

    private NavegacionRol() {
    }

    public static boolean abrirDashboardSegunRol(
            Stage stage) {

        if (stage == null) {

            mostrarError(
                    "No se pudo identificar la ventana principal."
            );

            return false;
        }

        SesionUsuario sesion =
                SesionUsuario
                        .getInstancia();

        if (!sesion.haySesionActiva()) {

            mostrarAccesoDenegado(
                    "No existe una sesión activa."
            );

            return false;
        }

        Rol rol =
                sesion.getRolActual();

        if (rol == null) {

            mostrarAccesoDenegado(
                    "El usuario no tiene un rol válido."
            );

            return false;
        }

        String rutaFXML;
        String titulo;

        switch (rol) {

            case ADMIN -> {

                rutaFXML =
                        "/org/esperanza/view/DashboardAdmin.fxml";

                titulo =
                        "Librería La Esperanza - Panel Administrador";
            }

            case CAJERO -> {

                rutaFXML =
                        "/org/esperanza/view/DashboardCajero.fxml";

                titulo =
                        "Librería La Esperanza - Panel Cajero";
            }

            case BODEGA -> {

                rutaFXML =
                        "/org/esperanza/view/DashboardBodega.fxml";

                titulo =
                        "Librería La Esperanza - Panel Bodega";
            }

            default -> {

                mostrarAccesoDenegado(
                        "El rol del usuario no tiene "
                        + "un dashboard configurado."
                );

                return false;
            }
        }

        URL recurso =
                NavegacionRol.class
                        .getResource(
                                rutaFXML
                        );

        if (recurso == null) {

            mostrarError(
                    "No se encontró la vista:\n"
                    + rutaFXML
            );

            return false;
        }

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            recurso
                    );

            Parent root =
                    loader.load();

            stage.setScene(
                    new Scene(root)
            );

            stage.setTitle(
                    titulo
            );

            stage.centerOnScreen();

            stage.show();

            return true;

        } catch (IOException e) {

            e.printStackTrace();

            mostrarError(
                    "No se pudo abrir el dashboard.\n"
                    + e.getMessage()
            );

            return false;
        }
    }

    public static boolean validarRol(
            Rol rolPermitido) {

        SesionUsuario sesion =
                SesionUsuario
                        .getInstancia();

        if (!sesion.haySesionActiva()) {

            mostrarAccesoDenegado(
                    "No existe una sesión activa."
            );

            return false;
        }

        if (sesion.getRolActual()
                != rolPermitido) {

            mostrarAccesoDenegado(
                    "Este panel es exclusivo para el rol "
                    + rolPermitido
                            .getNombreVisible()
                    + "."
            );

            return false;
        }

        return true;
    }

    public static boolean validarPermiso(
            String permiso) {

        SesionUsuario sesion =
                SesionUsuario
                        .getInstancia();

        if (!sesion.haySesionActiva()) {

            mostrarAccesoDenegado(
                    "No existe una sesión activa."
            );

            return false;
        }

        if (!sesion.tienePermiso(
                permiso)) {

            mostrarAccesoDenegado(
                    "El rol "
                    + sesion
                            .getRolActual()
                            .getNombreVisible()
                    + " no tiene permiso para realizar "
                    + "esta acción."
            );

            return false;
        }

        return true;
    }

    public static void mostrarAccesoDenegado(
            String mensaje) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle(
                "Acceso denegado"
        );

        alert.setHeaderText(
                "No tienes permisos suficientes"
        );

        alert.setContentText(
                mensaje
        );

        alert.showAndWait();
    }

    private static void mostrarError(
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