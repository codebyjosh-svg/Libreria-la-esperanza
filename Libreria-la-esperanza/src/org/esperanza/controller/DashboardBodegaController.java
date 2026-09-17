package org.esperanza.controller;

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
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.esperanza.service.NavegacionRol;
import org.esperanza.service.SesionUsuario;
import org.esperanza.dao.LibroDAO;
import org.esperanza.dao.impl.LibroDAOImpl;
import org.esperanza.model.Libro;
import org.esperanza.model.Rol;

public class DashboardBodegaController {

    @FXML private Label lblBienvenida;
    @FXML private Label lblRol;
    @FXML private Label lblPermisos;
    @FXML private Button btnCerrarSesion;
    @FXML private Label lblCantidadStockCritico; // T3.5: Indicador numérico en el Dashboard

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
            int cantidadCritica = (librosCriticos != null) ? librosCriticos.size() : 0;

            // Actualizar el indicador numérico (Tarea 3.5)
            if (lblCantidadStockCritico != null) {
                lblCantidadStockCritico.setText(String.valueOf(cantidadCritica));
                if (cantidadCritica > 0) {
                    lblCantidadStockCritico.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;"); // Rojo alerta
                } else {
                    lblCantidadStockCritico.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;"); // Verde OK
                }
            }

            // Alerta emergente detallada (Tarea 3.4)
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
            if (lblCantidadStockCritico != null) {
                lblCantidadStockCritico.setText("No disponible");
            }
            mostrarError("No se pudo consultar el stock crítico.\n" + e.getMessage());
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
    private void onVerLibrosCriticos() {
        if (!NavegacionRol.validarRol(Rol.BODEGA)) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/org/esperanza/view/LibrosCriticos.fxml"));
            Parent root = loader.load();
            Stage ventana = new Stage();
            ventana.setTitle("Libros con stock crítico");
            ventana.setScene(new Scene(root));
            ventana.initOwner(lblBienvenida.getScene().getWindow());
            ventana.initModality(Modality.WINDOW_MODAL);
            ventana.showAndWait();
        } catch (IOException e) {
            mostrarError("No se pudo abrir la lista de libros críticos.\n" + e.getMessage());
        }
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
        
        try {
            // Cargar la vista de Registro de Entradas que construimos antes
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/esperanza/view/RegistroEntrada.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Registro de Entradas de Inventario");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Bloquea la ventana principal mientras está abierto
            stage.setResizable(false);
            stage.showAndWait();
            
            // Al cerrar la ventana de entradas, refrescamos el indicador de stock por si se reabasteció algo
            verificarAlertaStock();
            
        } catch (IOException e) {
            e.printStackTrace();
            mostrarError("No se pudo abrir la ventana de Registro de Entradas.\n" + e.getMessage());
        }
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