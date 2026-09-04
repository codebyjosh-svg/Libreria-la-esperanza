package org.esperanza.Controller;

import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.esperanza.dao.UsuarioDao;
import org.esperanza.Model.Usuario;
import org.esperanza.Service.SesionUsuario;

/**
 * Dashboard principal. El login fue eliminado.
 */
public class DashboardAdminController {
    @FXML private Label lblBienvenida;
    @FXML private Label lblRol;
    @FXML private Label lblPermisos;
    @FXML private ComboBox<Usuario> cmbUsuarioActivo;

    private final UsuarioDao usuarioDao = new UsuarioDao();

    @FXML
    private void initialize() {
        cargarUsuariosActivos();
        actualizarEncabezado();
    }

    private void cargarUsuariosActivos() {
        cmbUsuarioActivo.setItems(FXCollections.observableArrayList(usuarioDao.listarUsuariosActivos()));
        Usuario actual = SesionUsuario.getInstancia().getUsuarioActual();
        if (actual != null) cmbUsuarioActivo.getSelectionModel().select(actual);
        cmbUsuarioActivo.setOnAction(e -> {
            Usuario seleccionado = cmbUsuarioActivo.getSelectionModel().getSelectedItem();
            if (seleccionado != null) {
                SesionUsuario.getInstancia().iniciarSesion(seleccionado);
                actualizarEncabezado();
            }
        });
    }

    private void actualizarEncabezado() {
        SesionUsuario sesion = SesionUsuario.getInstancia();
        lblBienvenida.setText("Usuario activo: " + sesion.getNombreCompleto());
        lblRol.setText("Rol: " + (sesion.getRolActual() != null ? sesion.getRolActual().getNombreVisible() : "N/A"));
        StringBuilder perms = new StringBuilder("Permisos: ");
        if (sesion.getRolActual() != null) {
            for (String p : sesion.getRolActual().getPermisos()) perms.append(p).append("  ");
        }
        lblPermisos.setText(perms.toString());
    }

    @FXML private void onGestionUsuarios(ActionEvent event) {
        abrir("/org/esperanza/view/Usuarios.fxml", "Gestión de Usuarios - Librería La Esperanza", 950, 600);
    }

    @FXML private void onReportes(ActionEvent event) {
        abrir("/org/esperanza/view/Reportes.fxml", "Reportes de uso y cambios - Librería La Esperanza", 1100, 650);
    }

    @FXML private void onConfiguracion(ActionEvent event) {
        abrir("/org/esperanza/view/Configuracion.fxml", "Configuración - Librería La Esperanza", 600, 480);
    }

    @FXML private void onCambiarUsuario(ActionEvent event) {
        cmbUsuarioActivo.requestFocus();
        cmbUsuarioActivo.show();
    }

    private void abrir(String fxml, String titulo, double ancho, double alto) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = new Stage();
            stage.setTitle(titulo);
            stage.setScene(new Scene(root, ancho, alto));
            stage.show();
        } catch (IOException e) {
            Alert a = new Alert(Alert.AlertType.ERROR, "No se pudo abrir el módulo: " + e.getMessage());
            a.showAndWait();
        }
    }
}
