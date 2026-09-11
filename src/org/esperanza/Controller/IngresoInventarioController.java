package org.esperanza.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

public class IngresoInventarioController {

    @FXML
    private TextField txtCantidad;

    @FXML
    public void initialize() {
        txtCantidad.setTextFormatter(
                new TextFormatter<String>(cambio ->
                        cambio.getControlNewText().matches("\\d*")
                                ? cambio
                                : null
                )
        );
    }

    public int obtenerCantidadIngresada() {
        String texto = txtCantidad.getText();

        if (texto == null || texto.isBlank()) {
            return 0;
        }

        return Integer.parseInt(texto);
    }
}