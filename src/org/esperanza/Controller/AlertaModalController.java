package org.esperanza.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AlertaModalController {

    public enum TipoAlerta {
        WARNING("⚠️", "alerta-warning");

        private final String icono;
        private final String estiloCSS;

        TipoAlerta(String icono, String estiloCSS) {
            this.icono = icono;
            this.estiloCSS = estiloCSS;
        }

        public String getIcono() { return icono; }
        public String getEstiloCSS() { return estiloCSS; }
    }

    @FXML private VBox rootContainer;
    @FXML private Label lblIcono;
    @FXML private Label lblTitulo;
    @FXML private Label lblMensaje;
    @FXML private Button btnAceptar;

    public void configurarAlerta(String titulo, String mensaje, TipoAlerta tipo) {
        lblTitulo.setText(titulo);
        lblMensaje.setText(mensaje);
        lblIcono.setText(tipo.getIcono());
        rootContainer.getStyleClass().add(tipo.getEstiloCSS());
    }

    @FXML
    private void onAceptar() {
        Stage stage = (Stage) btnAceptar.getScene().getWindow();
        stage.close();
    }
}