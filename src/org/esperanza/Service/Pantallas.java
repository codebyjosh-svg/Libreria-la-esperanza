package org.esperanza.Service;

import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import org.esperanza.Controller.CatalogoController;

public final class Pantallas {

    private Pantallas() {
    }

    public static void abrir(
            Node origen,
            String vista,
            String titulo) {

        try {
            URL recurso = Pantallas.class.getResource(
                    "/org/esperanza/view/" + vista + ".fxml"
            );

            if (recurso == null) {
                throw new IllegalStateException(
                        "No se encontró la pantalla " + titulo
                );
            }

            Parent contenido = FXMLLoader.load(recurso);

            Stage ventana = (Stage) origen.getScene().getWindow();

            ventana.setOnCloseRequest(null);
            ventana.setScene(new Scene(contenido));
            ventana.setTitle(titulo + " - Librería La Esperanza");
            ventana.centerOnScreen();

        } catch (Exception ex) {
            error(ex.getMessage());
        }
    }

    public static void catalogo(
            Node origen,
            String tabla,
            String titulo) {

        try {
            FXMLLoader loader = new FXMLLoader(
                    Pantallas.class.getResource(
                            "/org/esperanza/view/Catalogo.fxml"
                    )
            );

            Parent contenido = loader.load();

            CatalogoController controller = loader.getController();
            controller.configurar(tabla, titulo);

            Stage ventana = (Stage) origen.getScene().getWindow();

            ventana.setOnCloseRequest(event -> {
                event.consume();
                NavegacionRol.abrirDashboardSegunRol(ventana);
            });
            ventana.setScene(new Scene(contenido));
            ventana.setTitle(titulo + " - Librería La Esperanza");
            ventana.sizeToScene();
            ventana.centerOnScreen();

        } catch (Exception ex) {
            error(ex.getMessage());
        }
    }

    public static void volver(Node origen) {
        Stage ventana = (Stage) origen.getScene().getWindow();

        ventana.setOnCloseRequest(null);
        NavegacionRol.abrirDashboardSegunRol(ventana);
    }

    public static void error(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);

        alerta.setTitle("Error");
        alerta.setHeaderText(null);
        alerta.setContentText(
                mensaje == null
                        ? "No se pudo completar la operación."
                        : mensaje
        );

        alerta.showAndWait();
    }
}