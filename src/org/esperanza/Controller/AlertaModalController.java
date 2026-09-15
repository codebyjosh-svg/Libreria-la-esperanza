package org.esperanza.Controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AlertaModalController implements Initializable {

    public enum TipoAlerta {
        INFORMATION, WARNING, ERROR
    }

    @FXML private Label lblTitulo;
    @FXML private Label lblMensaje;
    @FXML private Button btnAceptar;
    @FXML private VBox rootContainer;

    private TipoAlerta tipoAlerta;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Configuración inicial si se requiere
    }

    /**
     * Configura el contenido y el estilo visual de la alerta modal.
     */
    public void configurarAlerta(String titulo, String mensaje, TipoAlerta tipo) {
        if (lblTitulo != null) {
            lblTitulo.setText(titulo != null ? titulo : "Notificación");
        }
        if (lblMensaje != null) {
            lblMensaje.setText(mensaje != null ? mensaje : "");
        }
        
        this.tipoAlerta = tipo;

        // CORRECCIÓN: Usar removeAll en lugar del método anterior inexistente
        if (rootContainer != null && tipo != null) {
            rootContainer.getStyleClass().removeAll("alerta-info", "alerta-warning", "alerta-error");
            switch (tipo) {
                case INFORMATION:
                    rootContainer.getStyleClass().add("alerta-info");
                    break;
                case WARNING:
                    rootContainer.getStyleClass().add("alerta-warning");
                    break;
                case ERROR:
                    rootContainer.getStyleClass().add("alerta-error");
                    break;
            }
        }
    }

    @FXML
    private void onAceptar() {
        if (btnAceptar != null && btnAceptar.getScene() != null) {
            Stage stage = (Stage) btnAceptar.getScene().getWindow();
            stage.close();
        }
    }
}