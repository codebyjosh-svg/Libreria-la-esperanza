package org.esperanza.controller;

import java.util.Objects;
import java.util.function.Function;
import java.time.format.DateTimeFormatter;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import org.esperanza.dao.CajeroDao;
import org.esperanza.dao.DetalleVentaDao;
import org.esperanza.model.Libro;
import org.esperanza.model.Usuario;
import org.esperanza.model.Venta;
import org.esperanza.service.NavegacionRol;
import org.esperanza.service.Pantallas;
import org.esperanza.view.ComprobanteVenta;

public final class ModulosCajero {

    private ModulosCajero() {
    }

    private interface Accion {
        void ejecutar() throws Exception;
    }

    private static void intentar(Accion accion) {
        try {
            accion.ejecutar();
        } catch (Exception ex) {
            Pantallas.error(ex.getMessage());
        }
    }

    private static <T> void columna(
            TableView<T> tabla,
            String titulo,
            double ancho,
            Function<T, Object> valor) {

        TableColumn<T, String> columna = new TableColumn<>(titulo);

        columna.setPrefWidth(ancho);

        columna.setCellValueFactory(datos ->
                new SimpleStringProperty(
                        Objects.toString(
                                valor.apply(datos.getValue()), ""
                        )
                )
        );

        tabla.getColumns().add(columna);
    }

    private static void volver(Stage ventana) {
        ventana.setOnCloseRequest(null);
        NavegacionRol.abrirDashboardSegunRol(ventana);
    }

    private static void mostrar(
            Stage ventana,
            String titulo,
            Node... contenido) {

        Label encabezado = new Label(titulo);

        encabezado.setStyle(
                "-fx-font-size: 20px; -fx-font-weight: bold;"
        );

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        Button regresar = new Button("Regresar");
        regresar.setOnAction(event -> volver(ventana));

        HBox barra = new HBox(
                12, encabezado, espacio, regresar
        );

        VBox root = new VBox(14, barra);

        root.setPadding(new Insets(20));
        root.getChildren().addAll(contenido);

        for (Node nodo : contenido) {
            if (nodo instanceof TableView<?>) {
                VBox.setVgrow(nodo, Priority.ALWAYS);
            }
        }

        Scene escena = new Scene(root, 950, 600);

        escena.getStylesheets().add(
                ModulosCajero.class.getResource(
                        "/org/esperanza/view/style/dashboard.css"
                ).toExternalForm()
        );

        ventana.setScene(escena);
        ventana.setTitle(titulo + " - Librería La Esperanza");

        ventana.setOnCloseRequest(event -> {
            event.consume();
            volver(ventana);
        });

        ventana.sizeToScene();
        ventana.centerOnScreen();
    }

    public static void stock(Stage ventana) {
        CajeroDao dao = new CajeroDao();

        TableView<Libro> tabla = new TableView<>();
        tabla.setPlaceholder(new Label("No hay libros para mostrar."));

        columna(tabla, "ISBN", 180, Libro::getIsbn);
        columna(tabla, "Título", 400, Libro::getTitulo);

        columna(
                tabla, "Precio", 130,
                libro -> String.format(
                        java.util.Locale.US,
                        "Q%.2f",
                        libro.getPrecio()
                )
        );

        columna(tabla, "Stock", 110, Libro::getStockActual);

        TextField buscar = new TextField();
        buscar.setPromptText("ISBN o título");

        HBox.setHgrow(buscar, Priority.ALWAYS);

        Button consultar = new Button("Buscar / Actualizar");

        Runnable cargar = () -> intentar(() ->
                tabla.getItems().setAll(
                        dao.stock(buscar.getText())
                )
        );

        consultar.setOnAction(event -> cargar.run());
        buscar.setOnAction(event -> cargar.run());

        mostrar(
                ventana,
                "Consultar stock",
                new HBox(10, buscar, consultar),
                tabla
        );

        cargar.run();
    }

    public static void ventas(Stage ventana) {
        CajeroDao dao = new CajeroDao();

        TableView<Venta> tabla = new TableView<>();
        tabla.setPlaceholder(new Label("No hay ventas para mostrar."));

        columna(tabla, "Número", 100, Venta::getIdVenta);

        columna(
                tabla, "Fecha", 240,
                venta -> venta.getFechaVenta().format(
                        DateTimeFormatter.ofPattern(
                                "dd/MM/yyyy HH:mm"
                        )
                )
        );

        columna(tabla, "CUI del cliente", 290, Venta::getCuiCliente);

        columna(
                tabla, "Total", 160,
                venta -> "Q" + venta.getTotal().toPlainString()
        );

        Button actualizar = new Button("Actualizar");

        actualizar.setOnAction(event -> intentar(() ->
                tabla.getItems().setAll(dao.ventas())
        ));

        Button detalle = new Button("Ver detalle / Abrir factura");

        detalle.setOnAction(event ->
                abrirFactura(ventana, tabla, false)
        );

        Button reimprimir = new Button("Reimprimir");

        reimprimir.setOnAction(event ->
                abrirFactura(ventana, tabla, true)
        );

        mostrar(
                ventana,
                "Mis ventas",
                tabla,
                new HBox(10, actualizar, detalle, reimprimir)
        );

        intentar(() -> tabla.getItems().setAll(dao.ventas()));
    }

    private static void abrirFactura(
            Stage ventana,
            TableView<Venta> tabla,
            boolean imprimir) {

        intentar(() -> {
            Venta seleccionada = tabla
                    .getSelectionModel()
                    .getSelectedItem();

            if (seleccionada == null) {
                throw new IllegalArgumentException(
                        "Selecciona una venta."
                );
            }

            Venta venta = new CajeroDao().buscarVenta(
                    seleccionada.getIdVenta()
            );

            var detalles = new DetalleVentaDao().listarPorVenta(
                    venta.getIdVenta()
            );

            TicketVentaController controller =
                    ComprobanteVenta.mostrar(
                            venta,
                            detalles,
                            ventana
                    );

            controller.setAlVolver(() -> ventas(ventana));

            if (imprimir) {
                controller.imprimir();
            }
        });
    }

    public static void usuarios(Stage ventana) {
        CajeroDao dao = new CajeroDao();

        TableView<Usuario> tabla = new TableView<>();

        tabla.setPlaceholder(
                new Label("No hay usuarios para mostrar.")
        );

        columna(tabla, "Usuario", 140, Usuario::getUsrname);
        columna(tabla, "Nombre", 150, Usuario::getNombre);
        columna(tabla, "Apellido", 150, Usuario::getApellido);
        columna(tabla, "Correo", 240, Usuario::getCorreo);
        columna(tabla, "Rol", 100, Usuario::getRol);

        columna(
                tabla, "Estado", 90,
                usuario -> usuario.isActivo()
                        ? "Activo"
                        : "Inactivo"
        );

        Button actualizar = new Button("Actualizar");

        actualizar.setOnAction(event -> intentar(() ->
                tabla.getItems().setAll(dao.usuarios())
        ));

        Button editar = new Button("Editar seleccionado");

        editar.setOnAction(event -> {
            Usuario usuario = tabla
                    .getSelectionModel()
                    .getSelectedItem();

            if (usuario == null) {
                Pantallas.error("Selecciona un usuario.");
                return;
            }

            editarUsuario(
                    ventana,
                    usuario,
                    () -> intentar(() ->
                            tabla.getItems().setAll(dao.usuarios())
                    )
            );
        });

        mostrar(
                ventana,
                "Editar usuarios",
                tabla,
                new HBox(10, actualizar, editar)
        );

        intentar(() -> tabla.getItems().setAll(dao.usuarios()));
    }

    private static void editarUsuario(
            Stage ventana,
            Usuario usuario,
            Runnable actualizar) {

        Dialog<ButtonType> dialogo = new Dialog<>();

        dialogo.initOwner(ventana);
        dialogo.setTitle("Editar usuario");

        TextField username = new TextField(usuario.getUsrname());
        TextField nombre = new TextField(usuario.getNombre());
        TextField apellido = new TextField(usuario.getApellido());
        TextField correo = new TextField(usuario.getCorreo());

        GridPane campos = new GridPane();

        campos.setPadding(new Insets(20));
        campos.setHgap(12);
        campos.setVgap(12);

        campos.addRow(0, new Label("Usuario"), username);
        campos.addRow(1, new Label("Nombre"), nombre);
        campos.addRow(2, new Label("Apellido"), apellido);
        campos.addRow(3, new Label("Correo"), correo);

        dialogo.getDialogPane().setContent(campos);

        ButtonType guardar = new ButtonType(
                "Guardar",
                ButtonBar.ButtonData.OK_DONE
        );

        dialogo.getDialogPane()
                .getButtonTypes()
                .addAll(guardar, ButtonType.CANCEL);

        dialogo.getDialogPane()
                .lookupButton(guardar)
                .addEventFilter(ActionEvent.ACTION, event -> {
                    try {
                        new CajeroDao().editarUsuario(
                                usuario.getId(),
                                username.getText().trim(),
                                nombre.getText().trim(),
                                apellido.getText().trim(),
                                correo.getText().trim()
                        );

                        actualizar.run();

                    } catch (Exception ex) {
                        event.consume();
                        Pantallas.error(ex.getMessage());
                    }
                });

        dialogo.showAndWait();
    }
}