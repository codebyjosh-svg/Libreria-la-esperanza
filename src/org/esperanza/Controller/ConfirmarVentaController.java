package org.esperanza.Controller;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import org.esperanza.Model.Cliente;
import org.esperanza.Model.DetalleVenta;
import org.esperanza.Model.DescuentoVenta;
import org.esperanza.view.ComprobanteVenta;
import org.esperanza.Model.Venta;

import org.esperanza.Service.NavegacionRol;
import org.esperanza.Service.Pantallas;

import org.esperanza.dao.ClienteDao;
import org.esperanza.dao.VentaDao;

public class ConfirmarVentaController {

    @FXML
    private TextField txtBuscarCliente;

    @FXML
    private TableView<Cliente> tablaClientes;

    @FXML
    private TableColumn<Cliente, Long> colCui;

    @FXML
    private TableColumn<Cliente, String> colNombre;

    @FXML
    private TableColumn<Cliente, String> colApellido;

    @FXML
    private Label lblCliente;

    @FXML
    private Label lblTotal;

    @FXML
    private TableView<DetalleVenta> tablaDetalle;

    @FXML
    private TableColumn<DetalleVenta, String> colIsbn;

    @FXML
    private TableColumn<DetalleVenta, Integer> colCantidad;

    @FXML
    private TableColumn<DetalleVenta, BigDecimal> colPrecio;

    @FXML
    private TableColumn<DetalleVenta, BigDecimal> colSubtotal;

    private final ClienteDao clienteDao
            = new ClienteDao();

    private final VentaDao ventaDao
            = new VentaDao();

    private final ObservableList<Cliente> clientes
            = FXCollections.observableArrayList();

    private String cuiCliente;

    private int idUsuario;

    private List<DetalleVenta> detalles;

    private BigDecimal total;

    private DescuentoVenta descuento;

    @FXML
    public void initialize() {

        configurarTablaClientes();

        configurarTablaDetalle();

        cargarClientes();

        buscarCliente();

    }

    public void cargarDatos(
            String cuiCliente,
            int idUsuario,
            List<DetalleVenta> detalles,
            BigDecimal total,
            DescuentoVenta descuento) {

        this.cuiCliente = cuiCliente;

        this.idUsuario = idUsuario;

        this.detalles = detalles;

        this.total = total;

        this.descuento = descuento;

        lblCliente.setText(
                "Cliente CUI: "
                + cuiCliente
        );

        lblTotal.setText(
                "Total: Q"
                + total.toPlainString()
        );

        tablaDetalle
                .getItems()
                .setAll(detalles);

    }

    private void configurarTablaClientes() {

        colCui.setCellValueFactory(
                dato
                -> new ReadOnlyObjectWrapper<>(
                        dato.getValue()
                                .getCui()
                )
        );

        colNombre.setCellValueFactory(
                dato
                -> new ReadOnlyObjectWrapper<>(
                        dato.getValue()
                                .getNombre()
                )
        );

        colApellido.setCellValueFactory(
                dato
                -> new ReadOnlyObjectWrapper<>(
                        dato.getValue()
                                .getApellido()
                )
        );

        tablaClientes.setItems(clientes);

        tablaClientes
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (obs, viejo, nuevo) -> {

                            if (nuevo != null) {

                                cuiCliente
                                = String.valueOf(
                                        nuevo.getCui()
                                );

                                lblCliente.setText(
                                        "Cliente: "
                                        + nuevo.getNombreCompleto()
                                        + " | CUI: "
                                        + cuiCliente
                                );

                            }

                        });

    }

    private void configurarTablaDetalle() {

        colIsbn.setCellValueFactory(
                dato
                -> new ReadOnlyObjectWrapper<>(
                        dato.getValue()
                                .getIsbn()
                )
        );

        colCantidad.setCellValueFactory(
                dato
                -> new ReadOnlyObjectWrapper<>(
                        dato.getValue()
                                .getCantidad()
                )
        );

        colPrecio.setCellValueFactory(
                dato
                -> new ReadOnlyObjectWrapper<>(
                        dato.getValue()
                                .getPrecioUnitario()
                )
        );

        colSubtotal.setCellValueFactory(
                dato
                -> new ReadOnlyObjectWrapper<>(
                        dato.getValue()
                                .getSubtotal()
                )
        );

    }

    private void cargarClientes() {

        try {

            clientes.setAll(
                    clienteDao.listar()
            );

        } catch (SQLException e) {

            mostrarError(
                    "No se pudieron cargar los clientes."
            );

        }

    }

    private void buscarCliente() {

        txtBuscarCliente.textProperty()
                .addListener(
                        (obs, viejo, texto) -> {

                            if (texto == null || texto.isBlank()) {

                                tablaClientes.setItems(
                                        clientes
                                );

                                return;

                            }

                            ObservableList<Cliente> filtrados
                            = FXCollections.observableArrayList();

                            String filtro
                            = texto.toLowerCase();

                            for (Cliente cliente : clientes) {

                                if (String.valueOf(
                                        cliente.getCui()
                                )
                                        .contains(filtro)
                                || cliente.getNombre()
                                        .toLowerCase()
                                        .contains(filtro)
                                || cliente.getApellido()
                                        .toLowerCase()
                                        .contains(filtro)) {

                                    filtrados.add(cliente);

                                }

                            }

                            tablaClientes.setItems(
                                    filtrados
                            );

                        });

    }

    @FXML
    private void finalizarVenta() {

        if (cuiCliente == null || cuiCliente.isBlank()) {

            mostrarError(
                    "Debe seleccionar un cliente antes de finalizar la venta."
            );

            return;

        }

        try {

            boolean registrado
                    = ventaDao.registrarVenta(
                            cuiCliente,
                            idUsuario,
                            detalles,
                            descuento
                    );

            if (!registrado) {

                throw new SQLException(
                        "No se pudo registrar la venta."
                );

            }

            Stage stage
                    = obtenerStage();

            Alert alerta = new Alert(Alert.AlertType.INFORMATION);

            alerta.setTitle("Venta registrada");
            alerta.setHeaderText(null);
            alerta.setContentText("La venta fue registrada correctamente.");

            alerta.showAndWait();

            Venta venta = new Venta();

            venta.setCuiCliente(
                    Long.parseLong(cuiCliente)
            );

            venta.setIdUsuario(
                    idUsuario
            );

            venta.setTotal(
                    total
            );

            venta.setSubtotal(
                    total);

            venta.setDescuento(
                    BigDecimal.ZERO
            );

            ComprobanteVenta.mostrar(
                    venta,
                    detalles,
                    stage
            );

        } catch (Exception e) {

            mostrarError(
                    e.getMessage()
            );

        }

    }

    @FXML
    private void regresar() {

        Stage stage
                = obtenerStage();

        NavegacionRol
                .abrirDashboardSegunRol(
                        stage
                );

    }

    private Stage obtenerStage() {

        return (Stage) lblCliente
                .getScene()
                .getWindow();

    }

    private void mostrarError(String mensaje) {

        Alert alerta
                = new Alert(
                        Alert.AlertType.ERROR
                );

        alerta.setTitle(
                "Error"
        );

        alerta.setHeaderText(
                null
        );

        alerta.setContentText(
                mensaje
        );

        alerta.showAndWait();

    }

}
