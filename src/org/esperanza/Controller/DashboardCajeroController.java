package org.esperanza.Controller;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.esperanza.Service.SesionUsuario;

/**
 * Dashboard del Cajero.
 * Autor: mi nombre
 */
public class DashboardCajeroController {

    @FXML private Label lblBienvenida;
    @FXML private Label lblRol;
    @FXML private Label lblPermisos;
    @FXML private Button btnCerrarSesion;

    @FXML
    private void initialize() {
        SesionUsuario sesion = SesionUsuario.getInstancia();
        if (sesion.haySesionActiva()) {
            lblBienvenida.setText("Bienvenido, " + sesion.getNombreCompleto());
            lblRol.setText("Rol: " + (sesion.getRolActual() != null ? sesion.getRolActual().getNombreVisible() : "N/A"));
            StringBuilder perms = new StringBuilder("Permisos: ");
            if (sesion.getRolActual() != null) {
                for (String p : sesion.getRolActual().getPermisos()) {
                    perms.append(p).append("  ");
                }
            }
            lblPermisos.setText(perms.toString());
        }
    }

    @FXML
    private void onNuevaVenta(ActionEvent event) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Ventas");
        a.setHeaderText(null);
        a.setContentText("Módulo de ventas (próximamente). Permiso: VENTAS");
        a.showAndWait();
    }

    @FXML
    private void onConsultarProductos(ActionEvent event) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Productos");
        a.setHeaderText(null);
        a.setContentText("Consulta de productos (próximamente). Permiso: CONSULTAR_PRODUCTOS");
        a.showAndWait();
    }

    @FXML
    private void onCerrarSesion(ActionEvent event) {
        SesionUsuario.getInstancia().cerrarSesion();
        Stage stage = (Stage) btnCerrarSesion.getScene().getWindow();
        stage.close();
    }

}
