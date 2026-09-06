package org.esperanza.Service;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import org.esperanza.Model.Rol;

/**
 * Centraliza la navegación según el rol
 * y la validación de permisos.
 */
public final class NavegacionRol {

    private NavegacionRol() {
    }

    // =====================================================
    // T1.21 - NAVEGACIÓN SEGÚN ROL
    // =====================================================

    public static boolean abrirDashboardSegunRol(
            Stage stage) {

        SesionUsuario sesion =
                SesionUsuario.getInstancia();

        if (!sesion.haySesionActiva()) {

            mostrarError(
                    "No existe una sesión activa."
            );

            return false;
        }

        Rol rol =
                sesion.getRolActual();

        String rutaFXML;
        String titulo;

        switch (rol) {

            case ADMIN -> {

                rutaFXML =
                        "/org/esperanza/view/DashboardAdmin.fxml";

                titulo =
                        "Librería La Esperanza - Administrador";
            }

            case CAJERO -> {

                rutaFXML =
                        "/org/esperanza/view/DashboardCajero.fxml";

                titulo =
                        "Librería La Esperanza - Cajero";
            }

            case BODEGA -> {

                rutaFXML =
                        "/org/esperanza/view/DashboardBodega.fxml";

                titulo =
                        "Librería La Esperanza - Bodega";
            }

            default -> {

                mostrarError(
                        "El usuario tiene un rol no reconocido."
                );

                return false;
            }
        }

        URL recurso =
                NavegacionRol.class
                        .getResource(rutaFXML);

        if (recurso == null) {

            mostrarError(
                    "No se encontró la vista: "
                    + rutaFXML
            );

            return false;
        }

        try {

            FXMLLoader loader =
                    new FXMLLoader(recurso);

            Parent root =
                    loader.load();

            stage.setScene(
                    new Scene(
                            root,
                            1050,
                            700
                    )
            );

            stage.setTitle(titulo);

            stage.setResizable(true);

            stage.centerOnScreen();

            stage.show();

            return true;

        } catch (IOException e) {

            e.printStackTrace();

            mostrarError(
                    "No se pudo abrir el dashboard: "
                    + e.getMessage()
            );

            return false;
        }
    }

    // =====================================================
    // T1.17 / T1.22 - VALIDAR PERMISO
    // =====================================================

    public static boolean validarPermiso(
            String permiso) {

        SesionUsuario sesion =
                SesionUsuario.getInstancia();

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
                    + " no tiene el permiso "
                    + permiso
                    + "."
            );

            return false;
        }

        return true;
    }

    // =====================================================
    // T1.22 - VALIDAR ACCESO A DASHBOARD
    // =====================================================

    public static boolean validarRol(
            Rol rolPermitido) {

        SesionUsuario sesion =
                SesionUsuario.getInstancia();

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