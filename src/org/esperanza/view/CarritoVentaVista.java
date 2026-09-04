package org.esperanza.view;

import java.math.BigDecimal;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.esperanza.model.DetalleVenta;
import org.esperanza.model.CarritoVenta;

/** Pantalla del carrito; puede utilizarse sin conexion a MySQL. */
public class CarritoVentaVista extends BorderPane {
    private final CarritoVenta carrito = new CarritoVenta();
    private final TableView<DetalleVenta> tabla = new TableView<>();
    private final TextField producto = new TextField();
    private final TextField precio = new TextField();
    private final TextField cantidad = new TextField("1");
    private final TextField nuevaCantidad = new TextField("1");
    private final Label total = new Label("Total: 0.00");
    private final Label mensaje = new Label();

    public CarritoVentaVista() {
        setPadding(new Insets(20));
        Label titulo = new Label("Carrito de venta · Libreria La Esperanza");
        titulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        producto.setPromptText("Ej. 5");
        precio.setPromptText("Ej. 25.50");
        producto.setPrefColumnCount(8);
        precio.setPrefColumnCount(10);
        cantidad.setPrefColumnCount(6);
        nuevaCantidad.setPrefColumnCount(6);
        Button agregar = new Button("Agregar producto");
        agregar.setDefaultButton(true);
        agregar.setOnAction(e -> ejecutar(() -> {
            int id = enteroPositivo(producto.getText(), "El ID del producto");
            int unidades = enteroPositivo(cantidad.getText(), "La cantidad");
            BigDecimal importe;
            try {
                importe = new BigDecimal(precio.getText().trim().replace(',', '.'));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Escribe un precio valido, por ejemplo 25.50");
            }
            carrito.agregarProducto(id, unidades, importe);
            refrescar(id);
            producto.clear();
            precio.clear();
            cantidad.setText("1");
            producto.requestFocus();
        }, "Producto agregado."));
        FlowPane formulario = new FlowPane(10, 10,
                campo("ID del producto", producto), campo("Precio unitario", precio),
                campo("Cantidad", cantidad), agregar);
        Label ayuda = new Label("El mismo producto al mismo precio suma cantidades. Selecciona una fila para modificarla.");
        ayuda.setWrapText(true);
        VBox cabecera = new VBox(12, titulo, formulario, ayuda);
        cabecera.setPadding(new Insets(0, 0, 15, 0));
        setTop(cabecera);

        TableColumn<DetalleVenta, Integer> id = new TableColumn<>("Producto (ID)");
        id.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getIdProducto()));
        TableColumn<DetalleVenta, Integer> unidades = new TableColumn<>("Cantidad");
        unidades.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getCantidad()));
        TableColumn<DetalleVenta, BigDecimal> unitario = new TableColumn<>("Precio unitario");
        unitario.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getPrecioUnitario()));
        TableColumn<DetalleVenta, BigDecimal> subtotal = new TableColumn<>("Subtotal");
        subtotal.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getSubtotal()));
        tabla.getColumns().add(id);
        tabla.getColumns().add(unidades);
        tabla.getColumns().add(unitario);
        tabla.getColumns().add(subtotal);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPlaceholder(new Label("Agrega productos para iniciar la venta."));
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, antes, seleccionado) -> {
            if (seleccionado != null) nuevaCantidad.setText(Integer.toString(seleccionado.getCantidad()));
        });
        setCenter(tabla);

        Button actualizar = new Button("Actualizar cantidad");
        actualizar.disableProperty().bind(tabla.getSelectionModel().selectedItemProperty().isNull());
        actualizar.setOnAction(e -> ejecutar(() -> {
            int seleccionado = tabla.getSelectionModel().getSelectedItem().getIdProducto();
            carrito.cambiarCantidad(seleccionado, enteroPositivo(nuevaCantidad.getText(), "La cantidad"));
            refrescar(seleccionado);
        }, "Cantidad actualizada."));
        Button eliminar = new Button("Eliminar producto");
        eliminar.disableProperty().bind(tabla.getSelectionModel().selectedItemProperty().isNull());
        eliminar.setOnAction(e -> ejecutar(() -> {
            carrito.quitarProducto(tabla.getSelectionModel().getSelectedItem().getIdProducto());
            refrescar(null);
        }, "Producto eliminado."));
        Button vaciar = new Button("Vaciar carrito");
        vaciar.disableProperty().bind(javafx.beans.binding.Bindings.isEmpty(tabla.getItems()));
        vaciar.setOnAction(e -> ejecutar(() -> {
            carrito.vaciar();
            refrescar(null);
        }, "Carrito vacio."));
        total.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        mensaje.setWrapText(true);
        FlowPane acciones = new FlowPane(10, 10,
                campo("Nueva cantidad", nuevaCantidad), actualizar, eliminar, vaciar);
        VBox pie = new VBox(12, acciones, total, mensaje);
        pie.setPadding(new Insets(15, 0, 0, 0));
        setBottom(pie);
    }

    private VBox campo(String etiqueta, TextField entrada) {
        return new VBox(5, new Label(etiqueta), entrada);
    }

    private int enteroPositivo(String texto, String nombre) {
        try {
            int valor = Integer.parseInt(texto.trim());
            if (valor > 0) return valor;
        } catch (NumberFormatException ex) {
            // Se muestra el mismo mensaje para texto invalido o fuera de rango.
        }
        throw new IllegalArgumentException(nombre + " debe ser un entero mayor que cero");
    }

    private void refrescar(Integer seleccionado) {
        tabla.getItems().setAll(carrito.getDetalles());
        total.setText("Total: " + carrito.getTotal().toPlainString());
        if (seleccionado != null) {
            for (DetalleVenta d : tabla.getItems()) {
                if (d.getIdProducto() == seleccionado) {
                    tabla.getSelectionModel().select(d);
                    break;
                }
            }
        }
    }

    private void ejecutar(Runnable accion, String confirmacion) {
        try {
            accion.run();
            mensaje.setStyle("-fx-text-fill: #166534;");
            mensaje.setText(confirmacion);
        } catch (IllegalArgumentException | ArithmeticException ex) {
            mensaje.setStyle("-fx-text-fill: #b91c1c;");
            mensaje.setText(ex instanceof ArithmeticException
                    ? "Usa precios de hasta dos decimales y cantidades dentro del rango permitido."
                    : ex.getMessage());
        }
    }
}

