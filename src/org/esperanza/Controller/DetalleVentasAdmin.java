package org.esperanza.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import org.esperanza.dao.CajeroDao;
import org.esperanza.dao.DatosVentaDao;
import org.esperanza.dao.DetalleVentaDao;
import org.esperanza.model.DetalleVenta;
import org.esperanza.model.Venta;
import org.esperanza.service.NavegacionRol;
import org.esperanza.service.Pantallas;
import org.esperanza.service.SesionUsuario;
import org.esperanza.view.ComprobanteVenta;

public final class DetalleVentasAdmin {

    private final Stage ventana;
    private final CajeroDao ventasDao = new CajeroDao();

    private final ObservableList<Venta> ventas =
            FXCollections.observableArrayList();

    private final TableView<Venta> tablaVentas =
            new TableView<>();

    private final TableView<DetalleVenta> tablaDetalles =
            new TableView<>();

    private final Label informacion = new Label(
            "Selecciona una venta para ver sus productos."
    );

    private final Label totales = new Label();
    private final Button factura = new Button("Abrir factura");

    private Map<String, String> titulos = Map.of();

    private DetalleVentasAdmin(Stage ventana) {
        this.ventana = ventana;
    }

    public static void mostrar(Stage ventana) {
        if (!SesionUsuario.getInstancia().esAdmin()) {
            Pantallas.error(
                    "Solo Admin puede consultar este historial."
            );
            return;
        }

        new DetalleVentasAdmin(ventana).abrir();
    }

    private void abrir() {
        columna(
                tablaVentas, "Número", 85,
                Venta::getIdVenta
        );

        columna(
                tablaVentas, "Fecha", 190,
                v -> v.getFechaVenta().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                )
        );

        columna(
                tablaVentas, "CUI cliente", 190,
                Venta::getCuiCliente
        );

        columna(
                tablaVentas, "ID cajero", 100,
                Venta::getIdUsuario
        );

        columna(
                tablaVentas, "Total", 120,
                v -> moneda(v.getTotal())
        );

        columna(
                tablaDetalles, "ISBN", 140,
                DetalleVenta::getIsbn
        );

        columna(
                tablaDetalles, "Producto", 300,
                d -> titulos.getOrDefault(d.getIsbn(), d.getIsbn())
        );

        columna(
                tablaDetalles, "Cantidad", 90,
                DetalleVenta::getCantidad
        );

        columna(
                tablaDetalles, "Precio unitario", 130,
                d -> moneda(d.getPrecioUnitario())
        );

        columna(
                tablaDetalles, "Subtotal", 120,
                d -> moneda(d.getSubtotal())
        );

        tablaVentas.setPlaceholder(
                new Label("No hay ventas para mostrar.")
        );

        tablaDetalles.setPlaceholder(
                new Label("Sin productos para mostrar.")
        );

        tablaVentas.setPrefHeight(180);
        tablaDetalles.setPrefHeight(170);

        tablaVentas.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (obs, anterior, nueva) -> cargarDetalle(nueva)
                );

        FilteredList<Venta> filtradas =
                new FilteredList<>(ventas, v -> true);

        tablaVentas.setItems(filtradas);

        TextField buscar = new TextField();

        buscar.setPromptText(
                "Buscar por número de venta o CUI del cliente"
        );

        buscar.textProperty().addListener((obs, anterior, nuevo) -> {
            String texto = nuevo.trim();

            filtradas.setPredicate(v ->
                    texto.isEmpty()
                    || String.valueOf(v.getIdVenta()).contains(texto)
                    || String.valueOf(v.getCuiCliente()).contains(texto)
            );
        });

        HBox.setHgrow(buscar, Priority.ALWAYS);

        Button actualizar = new Button("Actualizar");
        actualizar.setOnAction(e -> cargarVentas());

        Button regresar = new Button("Regresar");
        regresar.setOnAction(e -> volver());

        factura.setDisable(true);
        factura.setOnAction(e -> abrirFactura());

        Label titulo = new Label("Detalle de ventas");

        titulo.setStyle(
                "-fx-font-size:20px;-fx-font-weight:bold;"
        );

        informacion.setWrapText(true);

        VBox root = new VBox(
                10,
                titulo,
                new HBox(10, buscar, actualizar, regresar),
                tablaVentas,
                informacion,
                tablaDetalles,
                totales,
                factura
        );

        root.setPadding(new Insets(20));

        VBox.setVgrow(tablaVentas, Priority.ALWAYS);
        VBox.setVgrow(tablaDetalles, Priority.ALWAYS);

        Scene escena = new Scene(root, 950, 620);

        ventana.setOnCloseRequest(e -> {
            e.consume();
            volver();
        });

        ventana.setScene(escena);

        ventana.setTitle(
                "Detalle de ventas - Librería La Esperanza"
        );

        ventana.sizeToScene();
        ventana.centerOnScreen();

        cargarVentas();
    }

    private void cargarVentas() {
        limpiarDetalle();
        tablaVentas.getSelectionModel().clearSelection();

        try {
            validarAdmin();
            ventas.setAll(ventasDao.ventas());

        } catch (Exception ex) {
            ventas.clear();
            Pantallas.error(ex.getMessage());
        }
    }

    private void limpiarDetalle() {
        tablaDetalles.getItems().clear();
        titulos = Map.of();

        informacion.setText(
                "Selecciona una venta para ver sus productos."
        );

        totales.setText("");
        factura.setDisable(true);
    }

    private void cargarDetalle(Venta seleccionada) {
        limpiarDetalle();

        if (seleccionada == null) {
            return;
        }

        try {
            validarAdmin();

            Venta venta = ventasDao.buscarVenta(
                    seleccionada.getIdVenta()
            );

            List<DetalleVenta> detalles =
                    new DetalleVentaDao().listarPorVenta(
                            venta.getIdVenta()
                    );

            DatosVentaDao datos = new DatosVentaDao();

            titulos = datos.obtenerTitulos(detalles);

            String cliente = datos.obtenerNombreCliente(
                    venta.getCuiCliente()
            );

            String cajero = ventasDao.nombreCajero(
                    venta.getIdUsuario()
            );

            tablaDetalles.getItems().setAll(detalles);

            informacion.setText(
                    "Venta #" + venta.getIdVenta()
                    + " | Cliente: " + cliente
                    + " | Cajero: " + cajero
            );

            totales.setText(
                    "Subtotal: " + moneda(venta.getSubtotal())
                    + "    Descuento: " + moneda(venta.getDescuento())
                    + "    Total: " + moneda(venta.getTotal())
            );

            factura.setDisable(false);

        } catch (Exception ex) {
            limpiarDetalle();
            Pantallas.error(ex.getMessage());
        }
    }

    private void abrirFactura() {
        Venta seleccionada =
                tablaVentas.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            return;
        }

        try {
            validarAdmin();

            Venta venta = ventasDao.buscarVenta(
                    seleccionada.getIdVenta()
            );

            var detalles = new DetalleVentaDao().listarPorVenta(
                    venta.getIdVenta()
            );

            int idVenta = venta.getIdVenta();

            TicketVentaController controller = ComprobanteVenta.mostrar(
                    venta,
                    detalles,
                    ventana
            );

            controller.setAlVolver(() -> {
                if (!SesionUsuario.getInstancia().esAdmin()) {
                    volver();
                    return;
                }

                // Reconstruye el detalle en la misma ventana.
                DetalleVentasAdmin pantalla =
                        new DetalleVentasAdmin(ventana);

                pantalla.abrir();

                // Recupera la venta seleccionada y sus productos.
                for (Venta item : pantalla.ventas) {
                    if (item.getIdVenta() == idVenta) {
                        pantalla.tablaVentas
                                .getSelectionModel()
                                .select(item);

                        pantalla.tablaVentas.scrollTo(item);
                        break;
                    }
                }
            });

        } catch (Exception ex) {
            Pantallas.error(ex.getMessage());
        }
    }

    private void volver() {
        ventana.setOnCloseRequest(null);
        NavegacionRol.abrirDashboardSegunRol(ventana);
    }

    private void validarAdmin() {
        if (!SesionUsuario.getInstancia().esAdmin()) {
            throw new SecurityException(
                    "Solo Admin puede consultar este historial."
            );
        }
    }

    private static String moneda(BigDecimal valor) {
        return "Q" + (valor == null ? BigDecimal.ZERO : valor)
                .setScale(2, RoundingMode.HALF_UP)
                .toPlainString();
    }

    private static <T> void columna(
            TableView<T> tabla,
            String titulo,
            double ancho,
            Function<T, Object> valor
    ) {
        TableColumn<T, String> columna =
                new TableColumn<>(titulo);

        columna.setPrefWidth(ancho);

        columna.setCellValueFactory(c ->
                new SimpleStringProperty(
                        Objects.toString(valor.apply(c.getValue()), "")
                )
        );

        tabla.getColumns().add(columna);
    }
}