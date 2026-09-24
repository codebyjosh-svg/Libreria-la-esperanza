package org.esperanza.controller;

import java.util.Objects;
import java.util.function.Function;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.esperanza.dao.EdicionClienteDao;
import org.esperanza.model.Cliente;
import org.esperanza.service.NavegacionRol;
import org.esperanza.service.Pantallas;

public final class EdicionClientes {

    private EdicionClientes() {
    }

    public static void mostrar(Stage ventana) {
        EdicionClienteDao dao = new EdicionClienteDao();

        try {
            dao.validarAcceso();
        } catch (Exception ex) {
            Pantallas.error(ex.getMessage());
            return;
        }

        TableView<Cliente> tabla = new TableView<>();
        tabla.setPlaceholder(
                new Label("No hay clientes para mostrar.")
        );

        columna(tabla, "CUI", 190,
                c -> Long.toString(c.getCui()));
        columna(tabla, "Nombre", 200, Cliente::getNombre);
        columna(tabla, "Apellido", 200, Cliente::getApellido);
        columna(tabla, "Correo", 290, Cliente::getCorreo);

        Label mensaje = new Label();

        Runnable cargar = () -> {
            try {
                tabla.getItems().setAll(dao.listar());
                mensaje.setText("");
            } catch (Exception ex) {
                mensaje.setText("No se pudo actualizar la lista.");
                Pantallas.error(ex.getMessage());
            }
        };

        Button actualizar = new Button("Actualizar");
        actualizar.setOnAction(e -> cargar.run());

        Button editar = new Button("Editar seleccionado");
        editar.setOnAction(e -> {
            Cliente cliente =
                    tabla.getSelectionModel().getSelectedItem();

            if (cliente == null) {
                Pantallas.error("Selecciona un cliente.");
                return;
            }

            editar(ventana, cliente, dao, () -> {
                cargar.run();
                mensaje.setText(
                        "Cliente actualizado correctamente."
                );
            });
        });

        Button regresar = new Button("Regresar");
        regresar.setOnAction(e -> volver(ventana));

        Label titulo = new Label("Editar clientes");
        titulo.setStyle(
                "-fx-font-size: 20px; -fx-font-weight: bold;"
        );

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        HBox barra = new HBox(
                12, titulo, espacio, regresar
        );

        VBox root = new VBox(
                14,
                barra,
                tabla,
                new HBox(10, actualizar, editar),
                mensaje
        );

        root.setPadding(new Insets(20));
        VBox.setVgrow(tabla, Priority.ALWAYS);

        Scene escena = new Scene(root, 950, 600);

        escena.getStylesheets().add(
                EdicionClientes.class.getResource(
                        "/org/esperanza/view/style/dashboard.css"
                ).toExternalForm()
        );

        ventana.setScene(escena);
        ventana.setTitle(
                "Editar clientes - Librería La Esperanza"
        );

        ventana.setOnCloseRequest(e -> {
            e.consume();
            volver(ventana);
        });

        ventana.sizeToScene();
        ventana.centerOnScreen();

        cargar.run();
    }

    private static void columna(
            TableView<Cliente> tabla,
            String titulo,
            double ancho,
            Function<Cliente, String> valor
    ) {
        TableColumn<Cliente, String> columna =
                new TableColumn<>(titulo);

        columna.setPrefWidth(ancho);

        columna.setCellValueFactory(c ->
                new SimpleStringProperty(
                        Objects.toString(
                                valor.apply(c.getValue()), ""
                        )
                )
        );

        tabla.getColumns().add(columna);
    }

    private static void volver(Stage ventana) {
        ventana.setOnCloseRequest(null);
        NavegacionRol.abrirDashboardSegunRol(ventana);
    }

    private static void editar(
            Stage ventana,
            Cliente cliente,
            EdicionClienteDao dao,
            Runnable actualizado
    ) {
        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.initOwner(ventana);
        dialogo.setTitle("Editar cliente");

        TextField cui = new TextField(
                Long.toString(cliente.getCui())
        );
        cui.setEditable(false);

        TextField nombre = new TextField(cliente.getNombre());
        TextField apellido = new TextField(cliente.getApellido());
        TextField correo = new TextField(cliente.getCorreo());

        GridPane campos = new GridPane();
        campos.setPadding(new Insets(20));
        campos.setHgap(12);
        campos.setVgap(12);

        campos.addRow(0, new Label("CUI (no editable)"), cui);
        campos.addRow(1, new Label("Nombre"), nombre);
        campos.addRow(2, new Label("Apellido"), apellido);
        campos.addRow(3, new Label("Correo"), correo);

        dialogo.getDialogPane().setContent(campos);

        ButtonType guardar = new ButtonType(
                "Guardar", ButtonBar.ButtonData.OK_DONE
        );

        dialogo.getDialogPane().getButtonTypes().addAll(
                guardar, ButtonType.CANCEL
        );

        dialogo.getDialogPane()
                .lookupButton(guardar)
                .addEventFilter(ActionEvent.ACTION, e -> {
                    try {
                        dao.actualizar(
                                cliente.getCui(),
                                nombre.getText(),
                                apellido.getText(),
                                correo.getText()
                        );
                    } catch (Exception ex) {
                        e.consume();
                        Pantallas.error(ex.getMessage());
                    }
                });

        dialogo.showAndWait()
                .filter(guardar::equals)
                .ifPresent(b -> actualizado.run());
    }
}