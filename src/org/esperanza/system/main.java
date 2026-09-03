package org.esperanza.system;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class main {

    public static class Ventana extends Application {
        @Override
        public void start(Stage stage) throws Exception {
            Parent root = FXMLLoader.load(getClass().getResource("/org/esperanza/view/login.fxml"));
            Scene scene = new Scene(root, 420, 520);
            stage.setTitle("Librería La Esperanza");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        }
    }

    public static void main(String[] args) {
        Application.launch(Ventana.class, args);
    }
}