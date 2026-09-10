package org.esperanza.system;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/esperanza/view/CarritoVenta.fxml")
        );

        Parent root = loader.load();
        Scene scene = new Scene(root, 900, 600);

        stage.setTitle("Librería La Esperanza");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}