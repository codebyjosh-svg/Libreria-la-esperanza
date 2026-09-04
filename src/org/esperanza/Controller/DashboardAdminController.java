package org.esperanza.Controller;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class DashboardAdminController {

    @FXML
    private Label lblUsuario;

    @FXML
    private void onUsuariosClick() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/esperanza/view/Usuarios.fxml")
            );

            Parent root = loader.load();

            Stage stage = (Stage) lblUsuario.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Gestión de Usuarios - Librería La Esperanza");
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo abrir Gestión de Usuarios");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void onLibrosClick() {
        mostrarEnConstruccion("Libros");
    }

    @FXML
    private void onAutoresClick() {
        mostrarEnConstruccion("Autores");
    }

    @FXML
    private void onCategoriasClick() {
        mostrarEnConstruccion("Categorías");
    }

    @FXML
    private void onEditorialesClick() {
        mostrarEnConstruccion("Editoriales");
    }

    @FXML
    private void onVentasClick() {
        mostrarEnConstruccion("Ventas");
    }

    @FXML
    private void onAutoresLibroClick() {
        mostrarEnConstruccion("Autores-Libro");
    }

    @FXML
    private void onDetalleVentasClick() {
        mostrarEnConstruccion("Detalle Ventas");
    }

    @FXML
    private void onClientesClick() {
        mostrarEnConstruccion("Clientes");
    }

    @FXML
    private void onCerrarSesionClick() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/esperanza/view/Login.fxml")
            );

            Parent root = loader.load();

            Stage stage = (Stage) lblUsuario.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Librería La Esperanza");
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo cerrar la sesión");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void mostrarEnConstruccion(String modulo) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(modulo);
        alert.setHeaderText(null);
        alert.setContentText(
                "Módulo de " + modulo + " en construcción."
        );
        alert.showAndWait();
    }
}