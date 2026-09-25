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
        INFORMATION,
        WARNING,
        ERROR
    }

    @FXML
    private Label lblTitulo;

    @FXML
    private Label lblMensaje;

    @FXML
    private Button btnAceptar;

    @FXML
    private VBox rootContainer;

    private TipoAlerta tipoAlerta;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // No se necesita configuración inicial
    }

    /**
     * Configura el contenido y estilo de la alerta.
     */
    public void configurarAlerta(String titulo, String mensaje, TipoAlerta tipo) {

        lblTitulo.setText(
                titulo != null ? titulo : "Notificación"
        );

        lblMensaje.setText(
                mensaje != null ? mensaje : ""
        );

        // Si no se especifica tipo, usar INFORMATION
        this.tipoAlerta = tipo != null
                ? tipo
                : TipoAlerta.INFORMATION;

        // Limpiar estilos anteriores
        rootContainer.getStyleClass().removeAll(
                "alerta-info",
                "alerta-warning",
                "alerta-error"
        );

        // Agregar el estilo correspondiente
        switch (this.tipoAlerta) {

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

    @FXML
    private void onAceptar() {

        if (btnAceptar.getScene() != null) {

            Stage stage =
                    (Stage) btnAceptar.getScene().getWindow();

            stage.close();
        }
    }
}