package org.esperanza.Controller;

import java.math.BigDecimal;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class ActualizarPrecioController {

    @FXML
    private TextField txtIsbn;

    @FXML
    private TextField txtTitulo;

    @FXML
    private TextField txtPrecioActual;

    @FXML
    private TextField txtNuevoPrecio;

    @FXML
    private void onActualizarPrecioClick() {

        String textoPrecio = txtNuevoPrecio.getText();

        if (textoPrecio == null || textoPrecio.trim().isEmpty()) {
            mostrarAdvertencia(
                    "Debes ingresar el nuevo precio."
            );
            return;
        }

        BigDecimal nuevoPrecio;

        try {

            nuevoPrecio = new BigDecimal(
                    textoPrecio.trim()
            );

        } catch (NumberFormatException ex) {

            mostrarAdvertencia(
                    "El precio debe ser un valor numérico."
            );
            return;
        }

        if (nuevoPrecio.compareTo(BigDecimal.ZERO) <= 0) {

            mostrarAdvertencia(
                    "El nuevo precio debe ser mayor que Q0.00."
            );
            return;
        }

        mostrarInformacion(
                "El precio ingresado es válido."
        );
    }

    private void mostrarAdvertencia(String mensaje) {

        Alert alerta =
                new Alert(Alert.AlertType.WARNING);

        alerta.setTitle(
                "Actualización de Precio"
        );

        alerta.setHeaderText(
                "Valor no válido"
        );

        alerta.setContentText(mensaje);

        alerta.showAndWait();
    }

    private void mostrarInformacion(String mensaje) {

        Alert alerta =
                new Alert(Alert.AlertType.INFORMATION);

        alerta.setTitle(
                "Actualización de Precio"
        );

        alerta.setHeaderText(
                "Validación correcta"
        );

        alerta.setContentText(mensaje);

        alerta.showAndWait();
    }
}