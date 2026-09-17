package org.esperanza.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import org.esperanza.service.NavegacionRol;
import org.esperanza.dao.VentaDao;
import org.esperanza.model.CarritoVenta;
import org.esperanza.model.DetalleVenta;
import org.esperanza.model.Venta;
import org.esperanza.view.ComprobanteVenta;

public class CarritoVentaController {

    // =====================================================
    // CAMPOS DE PRODUCTO
    // =====================================================
    @FXML
    private TextField txtIsbn;

    @FXML
    private TextField txtPrecio;

    @FXML
    private TextField txtCantidad;

    @FXML
    private TextField txtNuevaCantidad;

    @FXML
    private TextField txtCuiCliente;

    // =====================================================
    // TABLA
    // =====================================================
    @FXML
    private TableView<DetalleVenta> tabla;

    @FXML
    private TableColumn<DetalleVenta, String> colIsbn;

    @FXML
    private TableColumn<DetalleVenta, Integer> colCantidad;

    @FXML
    private TableColumn<DetalleVenta, BigDecimal> colPrecio;

    @FXML
    private TableColumn<DetalleVenta, BigDecimal> colSubtotal;

    // =====================================================
    // BOTONES
    // =====================================================
    @FXML
    private Button btnActualizar;

    @FXML
    private Button btnEliminar;

    @FXML
    private Button btnVaciar;

    @FXML
    private Button btnConfirmar;

    // =====================================================
    // LABELS
    // =====================================================
    @FXML
    private Label lblTotal;

    @FXML
    private Label lblMensaje;

    // =====================================================
    // OBJETOS
    // =====================================================
    private final CarritoVenta carrito
            = new CarritoVenta();

    private final VentaDao ventaDao
            = new VentaDao();

    /*
     * IMPORTANTE:
     * Este valor debe recibirse del usuario
     * que inició sesión.
     */
    private int idUsuario = 0;

    // =====================================================
    // INITIALIZE
    // =====================================================
    @FXML
    public void initialize() {

        configurarColumnas();

        configurarTabla();

        configurarBotones();

        configurarSeleccionTabla();

        configurarValoresIniciales();

        configurarCierre();
    }

    // =====================================================
    // CONFIGURAR COLUMNAS
    // =====================================================
    private void configurarColumnas() {

        colIsbn.setCellValueFactory(
                dato -> new ReadOnlyObjectWrapper<>(
                        dato.getValue().getIsbn()
                )
        );

        colCantidad.setCellValueFactory(
                dato -> new ReadOnlyObjectWrapper<>(
                        dato.getValue().getCantidad()
                )
        );

        colPrecio.setCellValueFactory(
                dato -> new ReadOnlyObjectWrapper<>(
                        dato.getValue().getPrecioUnitario()
                )
        );

        colSubtotal.setCellValueFactory(
                dato -> new ReadOnlyObjectWrapper<>(
                        dato.getValue().getSubtotal()
                )
        );
    }

    // =====================================================
    // CONFIGURAR TABLA
    // =====================================================
    private void configurarTabla() {

        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        tabla.setPlaceholder(
                new Label(
                        "Agrega libros para iniciar la venta."
                )
        );
    }

    // =====================================================
    // CONFIGURAR BOTONES
    // =====================================================
    private void configurarBotones() {

        btnActualizar
                .disableProperty()
                .bind(
                        tabla.getSelectionModel()
                                .selectedItemProperty()
                                .isNull()
                );

        btnEliminar
                .disableProperty()
                .bind(
                        tabla.getSelectionModel()
                                .selectedItemProperty()
                                .isNull()
                );

        btnVaciar
                .disableProperty()
                .bind(
                        Bindings.isEmpty(
                                tabla.getItems()
                        )
                );

        btnConfirmar
                .disableProperty()
                .bind(
                        Bindings.isEmpty(
                                tabla.getItems()
                        )
                );
    }

    // =====================================================
    // SELECCIÓN DE TABLA
    // =====================================================
    private void configurarSeleccionTabla() {

        tabla.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, anterior, seleccionado) -> {

                            if (seleccionado != null) {

                                txtNuevaCantidad.setText(
                                        String.valueOf(
                                                seleccionado.getCantidad()
                                        )
                                );

                            } else {

                                txtNuevaCantidad.clear();
                            }
                        }
                );
    }

    // =====================================================
    // VALORES INICIALES
    // =====================================================
    private void configurarValoresIniciales() {

        txtCantidad.setText("1");

        lblTotal.setText(
                "Total: Q0.00"
        );

        lblMensaje.setText("");
    }

    // =====================================================
    // AGREGAR PRODUCTO
    // =====================================================
    @FXML
    private void agregarProducto() {

        ejecutar(() -> {

            String isbn
                    = obtenerTexto(txtIsbn);

            if (isbn.isEmpty()) {

                throw new IllegalArgumentException(
                        "El ISBN es obligatorio."
                );
            }

            int cantidad
                    = enteroPositivo(
                            obtenerTexto(txtCantidad),
                            "La cantidad"
                    );

            String precioTexto
                    = obtenerTexto(txtPrecio)
                            .replace(',', '.');

            if (precioTexto.isEmpty()) {

                throw new IllegalArgumentException(
                        "El precio es obligatorio."
                );
            }

            BigDecimal precio;

            try {

                precio
                        = new BigDecimal(
                                precioTexto
                        );

            } catch (NumberFormatException e) {

                throw new IllegalArgumentException(
                        "Escribe un precio válido."
                );
            }

            if (precio.compareTo(
                    BigDecimal.ZERO
            ) <= 0) {

                throw new IllegalArgumentException(
                        "El precio debe ser mayor que cero."
                );
            }

            /*
             * Evitar precios con demasiados decimales.
             */
            if (precio.scale() > 2) {

                throw new IllegalArgumentException(
                        "El precio puede tener máximo dos decimales."
                );
            }

            carrito.agregarProducto(
                    isbn,
                    cantidad,
                    precio
            );

            refrescar(
                    isbn
            );

            limpiarCamposProducto();

        }, "Libro agregado al carrito.");
    }

    // =====================================================
    // ACTUALIZAR CANTIDAD
    // =====================================================
    @FXML
    private void actualizarCantidad() {

        ejecutar(() -> {

            DetalleVenta detalle
                    = tabla
                            .getSelectionModel()
                            .getSelectedItem();

            if (detalle == null) {

                throw new IllegalArgumentException(
                        "Selecciona un libro."
                );
            }

            int cantidad
                    = enteroPositivo(
                            obtenerTexto(
                                    txtNuevaCantidad
                            ),
                            "La cantidad"
                    );

            carrito.cambiarCantidad(
                    detalle.getIsbn(),
                    cantidad
            );

            refrescar(
                    detalle.getIsbn()
            );

        }, "Cantidad actualizada.");
    }

    // =====================================================
    // ELIMINAR PRODUCTO
    // =====================================================
    @FXML
    private void eliminarProducto() {

        ejecutar(() -> {

            DetalleVenta detalle
                    = tabla
                            .getSelectionModel()
                            .getSelectedItem();

            if (detalle == null) {

                throw new IllegalArgumentException(
                        "Selecciona un libro."
                );
            }

            carrito.quitarProducto(
                    detalle.getIsbn()
            );

            refrescar(null);

            txtNuevaCantidad.clear();

        }, "Libro eliminado.");
    }

    // =====================================================
    // VACIAR CARRITO
    // =====================================================
    @FXML
    private void vaciarCarrito() {

        ejecutar(() -> {

            carrito.vaciar();

            refrescar(null);

            txtNuevaCantidad.clear();

        }, "Carrito vacío.");
    }

    // =====================================================
    // CONFIRMAR VENTA
    // =====================================================
    @FXML
    private void confirmarVenta() {

        try {

            /*
             * Verificar usuario.
             */
            if (idUsuario <= 0) {

                throw new IllegalArgumentException(
                        "No se ha identificado al usuario de la sesión."
                );
            }

            /*
             * Verificar carrito.
             */
            if (carrito.getDetalles() == null
                    || carrito.getDetalles().isEmpty()) {

                throw new IllegalArgumentException(
                        "No hay productos en el carrito."
                );
            }

            String cuiTexto
                    = obtenerTexto(
                            txtCuiCliente
                    );

            if (cuiTexto.isEmpty()) {

                throw new IllegalArgumentException(
                        "Ingresa el CUI del cliente."
                );
            }

            /*
             * El CUI debe contener únicamente números.
             */
            if (!cuiTexto.matches("\\d+")) {

                throw new IllegalArgumentException(
                        "El CUI solo puede contener números."
                );
            }

            long cuiCliente;

            try {

                cuiCliente
                        = Long.parseLong(
                                cuiTexto
                        );

            } catch (NumberFormatException e) {

                throw new IllegalArgumentException(
                        "Ingresa un CUI válido."
                );
            }

            if (cuiCliente <= 0) {

                throw new IllegalArgumentException(
                        "El CUI del cliente no es válido."
                );
            }

            /*
             * Copiar los detalles ANTES de confirmar.
             *
             * Esto evita que el comprobante quede vacío
             * si confirmarVenta limpia el carrito.
             */
            List<DetalleVenta> detalles
                    = new ArrayList<>(
                            carrito.getDetalles()
                    );

            Venta venta
                    = carrito.confirmarVenta(
                            cuiCliente,
                            idUsuario,
                            ventaDao
                    );

            if (venta == null) {

                throw new IllegalArgumentException(
                        "No fue posible registrar la venta."
                );
            }

            Stage stage
                    = obtenerStage();

            if (stage == null) {

                throw new IllegalStateException(
                        "No fue posible obtener la ventana actual."
                );
            }

            ComprobanteVenta.mostrar(
                    venta,
                    detalles,
                    stage
            );

        } catch (IllegalArgumentException e) {

            mostrarError(
                    e.getMessage()
            );

        } catch (SQLException e) {

            mostrarError(
                    "No fue posible registrar la venta en la base de datos."
            );

            e.printStackTrace();

        } catch (IOException e) {

            mostrarError(
                    "La venta fue procesada, pero no se pudo mostrar el comprobante."
            );

            e.printStackTrace();

        } catch (Exception e) {

            mostrarError(
                    "Ocurrió un error inesperado al confirmar la venta."
            );

            e.printStackTrace();
        }
    }

    // =====================================================
    // REFRESCAR TABLA
    // =====================================================
    private void refrescar(
            String isbnSeleccionado) {

        tabla.getItems().setAll(
                carrito.getDetalles()
        );

        lblTotal.setText(
                "Total: Q"
                + carrito
                        .getTotal()
                        .toPlainString()
        );

        if (isbnSeleccionado == null) {

            tabla.getSelectionModel()
                    .clearSelection();

            return;
        }

        for (DetalleVenta detalle
                : tabla.getItems()) {

            if (isbnSeleccionado.equals(
                    detalle.getIsbn()
            )) {

                tabla.getSelectionModel()
                        .select(
                                detalle
                        );

                tabla.scrollTo(
                        detalle
                );

                break;
            }
        }
    }

    // =====================================================
    // OBTENER TEXTO SEGURO
    // =====================================================
    private String obtenerTexto(
            TextField campo) {

        if (campo == null
                || campo.getText() == null) {

            return "";
        }

        return campo
                .getText()
                .trim();
    }

    // =====================================================
    // LIMPIAR CAMPOS
    // =====================================================
    private void limpiarCamposProducto() {

        txtIsbn.clear();

        txtPrecio.clear();

        txtCantidad.setText("1");

        txtNuevaCantidad.clear();

        txtIsbn.requestFocus();
    }

    // =====================================================
    // VALIDAR ENTERO POSITIVO
    // =====================================================
    private int enteroPositivo(
            String texto,
            String nombre) {

        if (texto == null
                || texto.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    nombre
                    + " es obligatoria."
            );
        }

        try {

            int valor
                    = Integer.parseInt(
                            texto.trim()
                    );

            if (valor <= 0) {

                throw new IllegalArgumentException(
                        nombre
                        + " debe ser mayor que cero."
                );
            }

            return valor;

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    nombre
                    + " debe ser un número entero."
            );
        }
    }

    // =====================================================
    // EJECUTAR ACCIÓN
    // =====================================================
    private void ejecutar(
            Runnable accion,
            String mensajeExito) {

        try {

            accion.run();

            mostrarExito(
                    mensajeExito
            );

        } catch (IllegalArgumentException e) {

            mostrarError(
                    e.getMessage()
            );

        } catch (ArithmeticException e) {

            mostrarError(
                    "Revisa los valores numéricos ingresados."
            );

        } catch (Exception e) {

            mostrarError(
                    "Ocurrió un error inesperado."
            );

        }
    }

    // =====================================================
    // MENSAJE ÉXITO
    // =====================================================
    private void mostrarExito(
            String mensaje) {

        if (lblMensaje == null) {
            return;
        }

        lblMensaje.setStyle(
                "-fx-text-fill: #166534;"
        );

        lblMensaje.setText(
                mensaje == null
                        ? ""
                        : mensaje
        );
    }

    // =====================================================
    // MENSAJE ERROR
    // =====================================================
    private void mostrarError(
            String mensaje) {

        if (lblMensaje == null) {
            return;
        }

        lblMensaje.setStyle(
                "-fx-text-fill: #b91c1c;"
        );

        lblMensaje.setText(
                mensaje == null
                || mensaje.isBlank()
                ? "Ocurrió un error."
                : mensaje
        );
    }

    // =====================================================
    // CONFIGURAR CIERRE
    // =====================================================
    private void configurarCierre() {

        Platform.runLater(() -> {

            Stage stage
                    = obtenerStage();

            if (stage == null) {
                return;
            }

            stage.setOnCloseRequest(
                    event -> {

                        event.consume();

                        regresarDashboard(
                                stage
                        );
                    }
            );
        });
    }

    // =====================================================
    // OBTENER STAGE
    // =====================================================
    private Stage obtenerStage() {

        if (tabla == null) {
            return null;
        }

        if (tabla.getScene() == null) {
            return null;
        }

        if (tabla.getScene().getWindow() == null) {
            return null;
        }

        if (!(tabla.getScene().getWindow() instanceof Stage)) {

            return null;
        }

        return (Stage) tabla
                .getScene()
                .getWindow();
    }

    // =====================================================
    // REGRESAR AL DASHBOARD
    // =====================================================
    private void regresarDashboard(
            Stage stage) {

        if (stage == null) {
            return;
        }

        /*
         * Quitamos el evento para evitar
         * que se vuelva a ejecutar al cambiar
         * de ventana.
         */
        stage.setOnCloseRequest(null);

        NavegacionRol
                .abrirDashboardSegunRol(
                        stage
                );
    }

    // =====================================================
    // USUARIO ACTUAL
    // =====================================================
    public void setIdUsuario(
            int idUsuario) {

        if (idUsuario <= 0) {

            throw new IllegalArgumentException(
                    "El usuario no es válido."
            );
        }

        this.idUsuario
                = idUsuario;
    }

    public int getIdUsuario() {

        return idUsuario;
    }
}
