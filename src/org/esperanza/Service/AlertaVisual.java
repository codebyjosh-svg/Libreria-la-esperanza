package org.esperanza.Service;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.esperanza.Controller.AlertaModalController;
import org.esperanza.Controller.AlertaModalController.TipoAlerta;

public class AlertaVisual {

    public static void mostrar(Stage ownerStage, String titulo, String mensaje, TipoAlerta tipo) {
        try {
            FXMLLoader loader = new FXMLLoader(AlertaVisual.class.getResource("/org/esperanza/view/AlertaModal.fxml"));
            Parent root = loader.load();

            AlertaModalController controller = loader.getController();
            controller.configurarAlerta(titulo, mensaje, tipo);

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            if (ownerStage != null) {
                modalStage.initOwner(ownerStage);
            }
            modalStage.initStyle(StageStyle.UNDECORATED);

            Scene scene = new Scene(root);
            modalStage.setScene(scene);
            modalStage.centerOnScreen();
            modalStage.showAndWait();

        } catch (IOException e) {
            System.err.println("Error al cargar el modal de alerta: " + e.getMessage());
        }
    }
}
