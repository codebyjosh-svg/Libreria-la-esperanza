package org.esperanza.Controller;



import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class BodegaDashboardController implements Initializable {

    @FXML
    private Label lblUsuario;
    @FXML
    private Button btnCerrarSesion;
    @FXML
    private Button btnNavLibros;
    @FXML
    private Button btnNavIngreso;
    @FXML
    private Button btnNavSalida;
    @FXML
    private Button btnNavMovimientos;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
}