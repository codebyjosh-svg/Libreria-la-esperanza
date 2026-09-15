package org.esperanza.Controller;

import java.io.IOException;
import java.util.List;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import org.esperanza.Model.Rol;
import org.esperanza.Service.NavegacionRol;
import org.esperanza.Service.SesionUsuario;
import org.esperanza.dao.LibroDAO;
import org.esperanza.dao.impl.LibroDAOImpl;
import org.esperanza.model.Libro;

public class DashboardBodegaController {

    @FXML private Label lblBienvenida;
    @FXML private Label lblRol;
    @FXML private Label lblPermisos;
    @FXML private Button btnCerrarSesion;

    private LibroDAO libroDAO;

    @FXML
    private void initialize() {
        try {
            this.libroDAO = new LibroDAOImpl();
        } catch (Exception e) {
            System.err.println("Error al instanciar LibroDAO: " + e.getMessage());
        }

        if (!NavegacionRol.validarRol(Rol.BODEGA)) {
            Platform.runLater(this::redirigirDashboardCorrecto);
            return;
        }

        actualizarEncabezado();
        configurarCierre();
        
        Platform.runLater(this::verificarAlertaStock);
    }

    private void verificarAlertaStock() {
        if (libroDAO == null) return;

        try {
            List<Libro> librosCriticos = libroDAO.obtenerStockCritico();

            if (librosCriticos != null && !librosCriticos.isEmpty()) {
                StringBuilder mensaje = new StringBuilder();
                mensaje.append("Los siguientes libros alcanzaron o cayeron por debajo del stock mínimo:\n\n");

                for (Libro libro : librosCriticos) {
                    mensaje.append("• ").append(libro.getTitulo())
                           .append(" | Stock actual: ").append(libro.getStockActual())
                           .append(" (Mínimo: ").append(libro.getStockMinimo()).append(")\n");
                }

                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Alerta de Inventario");
                alert.setHeaderText("¡Atención! Reabastecimiento Requerido");
                alert.setContentText(mensaje.toString());
                alert.showAndWait();
            }
        } catch (Exception e) {
            System.err.println("Error al verificar stock crítico: " + e.getMessage());
        }
    }

    private void actualizarEncabezado() {
        SesionUsuario sesion = SesionUsuario.getInstancia();

        if (lblBienvenida != null) {
            lblBienvenida.setText("Bienvenido, " + sesion.getNombreCompleto());
        }
        if (lblRol != null && sesion.getRolActual() != null) {
            lblRol.setText("Rol: " + sesion.getRolActual().getNombreVisible());
        }
        if (lblPermisos != null) {
            lblPermisos.setText("Permisos: GESTION_INVENTARIO | CONSULTAR_PRODUCTOS | ENTRADAS_SALIDAS");
        }
    }

    private void configurarCierre() {
        Platform.runLater(() -> {
            if (lblBienvenida != null && lblBienvenida.getScene() != null && lblBienvenida.getScene().getWindow() != null) {
                Stage stage = (Stage) lblBienvenida.getScene().getWindow();
                stage.setOnCloseRequest(event -> {
                    event.consume();
                    SesionUsuario.getInstancia().cerrarSesion();
                    volverLogin();
                });
            }
        });
    }

    @FXML
    private void onInventario() {
        if (!NavegacionRol.validarPermiso("GESTION_INVENTARIO")) {
            return;
        }
        mostrarInfo("Inventario", "Acceso a Gestión de Inventario autorizado.");
    }

    @FXML
    private void onEntradasSalidas() {
        if (!NavegacionRol.validarPermiso("ENTRADAS_SALIDAS")) {
            return;
        }
        mostrarInfo("Entradas / Salidas", "Acceso al registro de entradas y salidas autorizado.");
    }

    @FXML
    private void onCerrarSesion() {
        SesionUsuario.getInstancia().cerrarSesion();
        volverLogin();
    }

    private void volverLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/esperanza/view/Login.fxml"));
            Parent root = loader.load();

            if (lblBienvenida != null && lblBienvenida.getScene() != null) {
                Stage stage = (Stage) lblBienvenida.getScene().getWindow();
                stage.setOnCloseRequest(null);
                stage.setScene(new Scene(root));
                stage.setTitle("Librería La Esperanza");
                stage.centerOnScreen();
            }
        } catch (IOException e) {
            e.printStackTrace();
            mostrarError("No se pudo regresar al login.\n" + e.getMessage());
        }
    }

    private void redirigirDashboardCorrecto() {
        if (lblBienvenida == null || lblBienvenida.getScene() == null || lblBienvenida.getScene().getWindow() == null) {
            return;
        }

        Stage stage = (Stage) lblBienvenida.getScene().getWindow();

        if (SesionUsuario.getInstancia().haySesionActiva()) {
            NavegacionRol.abrirDashboardSegunRol(stage);
        } else {
            stage.close();
        }
    }

    private void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}