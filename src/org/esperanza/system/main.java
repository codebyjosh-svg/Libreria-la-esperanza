package org.esperanza.system;

import javafx.application.Application;
import javafx.scene.Scene;
import org.esperanza.view.CarritoVentaVista;
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
            CarritoVentaVista root = new CarritoVentaVista();
            Scene scene = new Scene(root, 950, 620);

            stage.setTitle("Libreria La Esperanza");
            stage.setScene(scene);
            stage.setMinWidth(760);
            stage.setMinHeight(560);
            stage.show();
        }
    }
}


