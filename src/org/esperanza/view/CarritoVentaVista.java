package org.esperanza.view;

import java.math.BigDecimal;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import org.esperanza.model.CarritoVenta;
import org.esperanza.model.DetalleVenta;

/**
 * Pantalla del carrito de venta.
 */
public class CarritoVentaVista extends BorderPane {

    private final CarritoVenta carrito = new CarritoVenta();

    private final TableView<DetalleVenta> tabla =
            new TableView<>();

    private final TextField isbn =
            new TextField();

    private final TextField precio =
            new TextField();

    private final TextField cantidad =
            new TextField("1");

    private final TextField nuevaCantidad =
            new TextField("1");

    private final Label total =
            new Label("Total: 0.00");

    private final Label mensaje =
            new Label();

    public CarritoVentaVista() {

        configurarVentana();
        configurarFormulario();
        configurarTabla();
        configurarAcciones();
    }

    // =====================================================
    // CONFIGURACION GENERAL
    // =====================================================

    private void configurarVentana() {

        setPadding(
                new Insets(20)
        );
    }

    // =====================================================
    // FORMULARIO
    // =====================================================

    private void configurarFormulario() {

        Label titulo =
                new Label(
                        "Carrito de venta · Libreria La Esperanza"
                );

        titulo.setStyle(
                "-fx-font-size: 22px;"
                + "-fx-font-weight: bold;"
        );

        isbn.setPromptText(
                "Ej. 978-0-124"
        );

        precio.setPromptText(
                "Ej. 25.50"
        );

        isbn.setPrefColumnCount(15);
        precio.setPrefColumnCount(10);
        cantidad.setPrefColumnCount(6);
        nuevaCantidad.setPrefColumnCount(6);

        Button agregar =
                new Button(
                        "Agregar libro"
                );

        agregar.setDefaultButton(true);

        agregar.setOnAction(e ->
                ejecutar(() -> {

                    // ===============================
                    // OBTENER ISBN
                    // ===============================

                    String isbnLibro =
                            isbn
                                    .getText()
                                    .trim();

                    if (isbnLibro.isEmpty()) {

                        throw new IllegalArgumentException(
                                "El ISBN es obligatorio"
                        );
                    }

                    // ===============================
                    // OBTENER CANTIDAD
                    // ===============================

                    int unidades =
                            enteroPositivo(
                                    cantidad.getText(),
                                    "La cantidad"
                            );

                    // ===============================
                    // OBTENER PRECIO
                    // ===============================

                    BigDecimal importe;

                    try {

                        importe =
                                new BigDecimal(
                                        precio
                                                .getText()
                                                .trim()
                                                .replace(',', '.')
                                );

                    } catch (NumberFormatException ex) {

                        throw new IllegalArgumentException(
                                "Escribe un precio valido, "
                                + "por ejemplo 25.50"
                        );
                    }

                    if (importe.compareTo(
                            BigDecimal.ZERO
                    ) <= 0) {

                        throw new IllegalArgumentException(
                                "El precio debe ser mayor que cero"
                        );
                    }

                    // ===============================
                    // AGREGAR AL CARRITO
                    // ===============================

                    carrito.agregarProducto(
                            isbnLibro,
                            unidades,
                            importe
                    );

                    refrescar(
                            isbnLibro
                    );

                    // ===============================
                    // LIMPIAR CAMPOS
                    // ===============================

                    isbn.clear();
                    precio.clear();
                    cantidad.setText("1");

                    isbn.requestFocus();

                }, "Libro agregado al carrito.")
        );

        FlowPane formulario =
                new FlowPane(
                        10,
                        10,
                        campo(
                                "ISBN",
                                isbn
                        ),
                        campo(
                                "Precio unitario",
                                precio
                        ),
                        campo(
                                "Cantidad",
                                cantidad
                        ),
                        agregar
                );

        Label ayuda =
                new Label(
                        "El mismo ISBN suma cantidades. "
                        + "Selecciona una fila para modificarla."
                );

        ayuda.setWrapText(true);

        VBox cabecera =
                new VBox(
                        12,
                        titulo,
                        formulario,
                        ayuda
                );

        cabecera.setPadding(
                new Insets(
                        0,
                        0,
                        15,
                        0
                )
        );

        setTop(
                cabecera
        );
    }

    // =====================================================
    // TABLA
    // =====================================================

    private void configurarTabla() {

        // ===============================
        // ISBN
        // ===============================

        TableColumn<DetalleVenta, String> columnaIsbn =
                new TableColumn<>(
                        "ISBN"
                );

        columnaIsbn.setCellValueFactory(c ->
                new ReadOnlyObjectWrapper<>(
                        c
                                .getValue()
                                .getIsbn()
                )
        );

        // ===============================
        // CANTIDAD
        // ===============================

        TableColumn<DetalleVenta, Integer> columnaCantidad =
                new TableColumn<>(
                        "Cantidad"
                );

        columnaCantidad.setCellValueFactory(c ->
                new ReadOnlyObjectWrapper<>(
                        c
                                .getValue()
                                .getCantidad()
                )
        );

        // ===============================
        // PRECIO
        // ===============================

        TableColumn<DetalleVenta, BigDecimal> columnaPrecio =
                new TableColumn<>(
                        "Precio unitario"
                );

        columnaPrecio.setCellValueFactory(c ->
                new ReadOnlyObjectWrapper<>(
                        c
                                .getValue()
                                .getPrecioUnitario()
                )
        );

        // ===============================
        // SUBTOTAL
        // ===============================

        TableColumn<DetalleVenta, BigDecimal> columnaSubtotal =
                new TableColumn<>(
                        "Subtotal"
                );

        columnaSubtotal.setCellValueFactory(c ->
                new ReadOnlyObjectWrapper<>(
                        c
                                .getValue()
                                .getSubtotal()
                )
        );

        /*
         * Se agregan individualmente
         * para evitar warnings unchecked.
         */

        tabla.getColumns().add(
                columnaIsbn
        );

        tabla.getColumns().add(
                columnaCantidad
        );

        tabla.getColumns().add(
                columnaPrecio
        );

        tabla.getColumns().add(
                columnaSubtotal
        );

        tabla.setColumnResizePolicy(
                TableView
                        .CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        tabla.setPlaceholder(
                new Label(
                        "Agrega libros para iniciar la venta."
                )
        );

        tabla
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (
                                observable,
                                anterior,
                                seleccionado
                        ) -> {

                            if (seleccionado != null) {

                                nuevaCantidad.setText(
                                        Integer.toString(
                                                seleccionado
                                                        .getCantidad()
                                        )
                                );
                            }
                        }
                );

        setCenter(
                tabla
        );
    }

    // =====================================================
    // ACCIONES
    // =====================================================

    private void configurarAcciones() {

        // =================================================
        // ACTUALIZAR CANTIDAD
        // =================================================

        Button actualizar =
                new Button(
                        "Actualizar cantidad"
                );

        actualizar
                .disableProperty()
                .bind(
                        tabla
                                .getSelectionModel()
                                .selectedItemProperty()
                                .isNull()
                );

        actualizar.setOnAction(e ->
                ejecutar(() -> {

                    DetalleVenta seleccionado =
                            tabla
                                    .getSelectionModel()
                                    .getSelectedItem();

                    if (seleccionado == null) {

                        throw new IllegalArgumentException(
                                "Selecciona un libro"
                        );
                    }

                    int cantidadNueva =
                            enteroPositivo(
                                    nuevaCantidad.getText(),
                                    "La cantidad"
                            );

                    carrito.cambiarCantidad(
                            seleccionado.getIsbn(),
                            cantidadNueva
                    );

                    refrescar(
                            seleccionado.getIsbn()
                    );

                }, "Cantidad actualizada.")
        );

        // =================================================
        // ELIMINAR
        // =================================================

        Button eliminar =
                new Button(
                        "Eliminar libro"
                );

        eliminar
                .disableProperty()
                .bind(
                        tabla
                                .getSelectionModel()
                                .selectedItemProperty()
                                .isNull()
                );

        eliminar.setOnAction(e ->
                ejecutar(() -> {

                    DetalleVenta seleccionado =
                            tabla
                                    .getSelectionModel()
                                    .getSelectedItem();

                    if (seleccionado == null) {

                        throw new IllegalArgumentException(
                                "Selecciona un libro"
                        );
                    }

                    carrito.quitarProducto(
                            seleccionado.getIsbn()
                    );

                    refrescar(
                            null
                    );

                }, "Libro eliminado.")
        );

        // =================================================
        // VACIAR CARRITO
        // =================================================

        Button vaciar =
                new Button(
                        "Vaciar carrito"
                );

        vaciar
                .disableProperty()
                .bind(
                        Bindings.isEmpty(
                                tabla.getItems()
                        )
                );

        vaciar.setOnAction(e ->
                ejecutar(() -> {

                    carrito.vaciar();

                    refrescar(
                            null
                    );

                }, "Carrito vacio.")
        );

        // =================================================
        // TOTAL
        // =================================================

        total.setStyle(
                "-fx-font-size: 24px;"
                + "-fx-font-weight: bold;"
        );

        mensaje.setWrapText(
                true
        );

        FlowPane acciones =
                new FlowPane(
                        10,
                        10,
                        campo(
                                "Nueva cantidad",
                                nuevaCantidad
                        ),
                        actualizar,
                        eliminar,
                        vaciar
                );

        VBox pie =
                new VBox(
                        12,
                        acciones,
                        total,
                        mensaje
                );

        pie.setPadding(
                new Insets(
                        15,
                        0,
                        0,
                        0
                )
        );

        setBottom(
                pie
        );
    }

    // =====================================================
    // CREAR CAMPO
    // =====================================================

    private VBox campo(
            String etiqueta,
            TextField entrada) {

        return new VBox(
                5,
                new Label(
                        etiqueta
                ),
                entrada
        );
    }

    // =====================================================
    // VALIDAR ENTERO POSITIVO
    // =====================================================

    private int enteroPositivo(
            String texto,
            String nombre) {

        try {

            int valor =
                    Integer.parseInt(
                            texto.trim()
                    );

            if (valor > 0) {

                return valor;
            }

        } catch (NumberFormatException ex) {

            // Se utiliza el mensaje general.
        }

        throw new IllegalArgumentException(
                nombre
                + " debe ser un entero mayor que cero"
        );
    }

    // =====================================================
    // REFRESCAR TABLA
    // =====================================================

    private void refrescar(
            String isbnSeleccionado) {

        tabla.getItems().setAll(
                carrito.getDetalles()
        );

        total.setText(
                "Total: Q"
                + carrito
                        .getTotal()
                        .toPlainString()
        );

        if (isbnSeleccionado != null) {

            for (DetalleVenta detalle
                    : tabla.getItems()) {

                if (detalle
                        .getIsbn()
                        .equals(
                                isbnSeleccionado
                        )) {

                    tabla
                            .getSelectionModel()
                            .select(
                                    detalle
                            );

                    break;
                }
            }
        }
    }

    // =====================================================
    // MOSTRAR MENSAJES
    // =====================================================

    private void ejecutar(
            Runnable accion,
            String confirmacion) {

        try {

            accion.run();

            mensaje.setStyle(
                    "-fx-text-fill: #166534;"
            );

            mensaje.setText(
                    confirmacion
            );

        } catch (
                IllegalArgumentException
                | ArithmeticException ex
        ) {

            mensaje.setStyle(
                    "-fx-text-fill: #b91c1c;"
            );

            if (ex instanceof ArithmeticException) {

                mensaje.setText(
                        "Usa precios de hasta dos decimales "
                        + "y cantidades dentro del rango permitido."
                );

            } else {

                mensaje.setText(
                        ex.getMessage()
                );
            }
        }
    }
}