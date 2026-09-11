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
       throw new IllegalArgumentException(
                    "Debe ingresar una cantidad."
            );
        }
        
        int cantidad;
        
        try{
            cantidad = Integer.parseInt(texto);
        }catch (NumberFormatException e){
            throw new IllegalArgumentException(
            "La cantidad ingresada no es válida."
            );
        }
        
        if (cantidad <= 0){
            throw new IllegalArgumentException(
            "La cantidad debe ser mayor que 0.");
        }
        
        return cantidad;
    }
}