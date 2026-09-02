package org.esperanza.system;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * Punto de entrada del proyecto. Se mantiene como una clase normal
 * para poder trabajar con JavaFX desde el Classpath en NetBeans.
 */
public class main {

    public static void main(String[] args) {
        Application.launch(Ventana.class, args);
    }

    public static class Ventana extends Application {

        @Override
        public void start(Stage stage) {
            Pane root = new Pane();
            Scene scene = new Scene(root, 800, 600);

            stage.setTitle("Libreria La Esperanza");
            stage.setScene(scene);
            stage.show();
        }
    }
}
