package org.esperanza.controller;

import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.Map;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import org.esperanza.dao.CatalogoDao;
import org.esperanza.service.Pantallas;

public class CatalogoController {

    @FXML private Label lblTitulo;
    @FXML private Label lblMensaje;

    @FXML private GridPane formulario;

    @FXML
    private TableView<Map<String, String>> tabla;

    private CatalogoDao dao;

    private final Map<String, TextField> entradas =
            new LinkedHashMap<>();

    public void configurar(
            String nombreTabla,
            String titulo) throws Exception {

        dao = new CatalogoDao(nombreTabla);
        lblTitulo.setText(titulo);

        int fila = 0;

        for (CatalogoDao.Campo campo : dao.campos()) {
            String nombreVisible = etiqueta(campo.nombre());

            TableColumn<Map<String, String>, String> columna =
                    new TableColumn<>(nombreVisible);

            columna.setPrefWidth(150);

            columna.setCellValueFactory(datos ->
                    new SimpleStringProperty(
                            datos.getValue().get(campo.nombre())
                    )
            );

            tabla.getColumns().add(columna);

            if (campo.generado()) {
                continue;
            }

            // La base de datos conserva sus valores automáticos.
            if (campo.defecto() != null
                    && (campo.nombre().equalsIgnoreCase("activo")
                    || campo.nombre().startsWith("fecha_"))) {
                continue;
            }

            TextField entrada = new TextField();
            entrada.setPrefWidth(260);

            entrada.setPromptText(
                    campo.obligatorio() && campo.defecto() == null
                            ? "Obligatorio"
                            : "Opcional"
            );

            if (campo.tipo() == Types.DATE) {
                entrada.setPromptText("AAAA-MM-DD");
            }

            Label rotulo = new Label(nombreVisible);
            rotulo.setWrapText(true);

            formulario.add(rotulo, 0, fila++, 2, 1);
            formulario.add(entrada, 0, fila++, 2, 1);

            entradas.put(campo.nombre(), entrada);
        }

        actualizar();
    }

    private String etiqueta(String nombre) {
        String texto = nombre.replace('_', ' ');

        return texto.substring(0, 1).toUpperCase()
                + texto.substring(1);
    }

    @FXML
    private void guardar() {
        try {
            Map<String, String> valores = new LinkedHashMap<>();

            entradas.forEach((nombre, entrada) ->
                    valores.put(nombre, entrada.getText())
            );

            dao.insertar(valores);

            entradas.values().forEach(TextField::clear);

            lblMensaje.setText(
                    "Registro guardado correctamente."
            );

            actualizar();

        } catch (SQLIntegrityConstraintViolationException ex) {
            Pantallas.error(
                    "El registro ya existe o tiene una referencia inválida."
            );

        } catch (Exception ex) {
            Pantallas.error(ex.getMessage());
        }
    }

    @FXML
    private void actualizar() {
        try {
            tabla.getItems().setAll(dao.listar());

        } catch (Exception ex) {
            Pantallas.error(ex.getMessage());
        }
    }

    @FXML
    private void volver() {
        Pantallas.volver(tabla);
    }
}