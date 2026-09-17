package org.esperanza.controller;

import java.io.IOException;
import java.util.List;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.esperanza.service.NavegacionRol;
import org.esperanza.service.SesionUsuario;
import org.esperanza.dao.LibroDAO;
import org.esperanza.dao.impl.LibroDAOImpl;
import org.esperanza.model.Libro;
import org.esperanza.model.Rol;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class DashboardBodegaController {

    @FXML
    private Label lblBienvenida;

    @FXML
    private Label lblRol;

    @FXML
    private Label lblPermisos;

    @FXML
    private Button btnCerrarSesion;

    @FXML
    private Label lblCantidadStockCritico;

    private LibroDAO libroDAO;

    @FXML
    private Label lblContadorCritico;

    @FXML
    private TableView<Libro> tablaStockCritico;

    @FXML
    private TableColumn<Libro, String> colIsbn;

    @FXML
    private TableColumn<Libro, String> colTitulo;

    @FXML
    private TableColumn<Libro, Integer> colStock;

    @FXML
    private void initialize() {
        try {
            libroDAO = new LibroDAOImpl();
        } catch (Exception e) {
            System.err.println(
                    "Error al instanciar LibroDAO: "
                    + e.getMessage()
            );
        }

        if (!NavegacionRol.validarRol(Rol.BODEGA)) {
            Platform.runLater(this::redirigirDashboardCorrecto);
            return;
        }

        actualizarEncabezado();
        configurarCierre();
        configurarTablaStockCritico();

        Platform.runLater(this::verificarAlertaStock);
    }

    private void configurarTablaStockCritico() {

        if (tablaStockCritico == null) {
            return;
        }

        colIsbn.setCellValueFactory(
                new PropertyValueFactory<>("isbn")
        );

        colTitulo.setCellValueFactory(
                new PropertyValueFactory<>("titulo")
        );

        colStock.setCellValueFactory(
                new PropertyValueFactory<>("stockActual")
        );
    }

    private void verificarAlertaStock() {
        if (libroDAO == null) {
            return;
        }

        try {
            List<Libro> librosCriticos
                    = libroDAO.obtenerStockCritico();

            ObservableList<Libro> datosCriticos
                    = FXCollections.observableArrayList(
                            librosCriticos != null
                                    ? librosCriticos
                                    : List.of()
                    );

            if (tablaStockCritico != null) {
                tablaStockCritico.setItems(datosCriticos);
            }

            if (lblContadorCritico != null) {
                lblContadorCritico.setText(
                        String.valueOf(datosCriticos.size())
                );
            }

            int cantidadCritica
                    = librosCriticos != null
                            ? librosCriticos.size()
                            : 0;

            if (lblCantidadStockCritico != null) {
                lblCantidadStockCritico.setText(
                        String.valueOf(cantidadCritica)
                );

                if (cantidadCritica > 0) {
                    lblCantidadStockCritico.setStyle(
                            "-fx-text-fill: #EF4444;"
                            + "-fx-font-weight: bold;"
                    );
                } else {
                    lblCantidadStockCritico.setStyle(
                            "-fx-text-fill: #10B981;"
                            + "-fx-font-weight: bold;"
                    );
                }
            }

            if (librosCriticos != null
                    && !librosCriticos.isEmpty()) {

                StringBuilder mensaje = new StringBuilder();

                mensaje.append(
                        "Los siguientes libros alcanzaron "
                        + "o cayeron por debajo del stock mínimo:\n\n"
                );

                for (Libro libro : librosCriticos) {
                    mensaje.append("• ")
                            .append(libro.getTitulo())
                            .append(" | Stock actual: ")
                            .append(libro.getStockActual())
                            .append(" (Mínimo: ")
                            .append(libro.getStockMinimo())
                            .append(")\n");
                }

                Alert alert
                        = new Alert(Alert.AlertType.WARNING);

                alert.setTitle("Alerta de Inventario");
                alert.setHeaderText(
                        "¡Atención! Reabastecimiento Requerido"
                );
                alert.setContentText(mensaje.toString());
                alert.showAndWait();
            }

        } catch (Exception e) {
            if (lblCantidadStockCritico != null) {
                lblCantidadStockCritico.setText(
                        "No disponible"
                );
            }

            mostrarError(
                    "No se pudo consultar el stock crítico.\n"
                    + e.getMessage()
            );
        }
    }

    private void actualizarEncabezado() {
        SesionUsuario sesion
                = SesionUsuario.getInstancia();

        if (lblBienvenida != null) {
            lblBienvenida.setText(
                    "Bienvenido, "
                    + sesion.getNombreCompleto()
            );
        }

        if (lblRol != null
                && sesion.getRolActual() != null) {

            lblRol.setText(
                    "Rol: "
                    + sesion.getRolActual()
                            .getNombreVisible()
            );
        }

        if (lblPermisos != null) {
            lblPermisos.setText(
                    "Permisos: GESTION_INVENTARIO | "
                    + "CONSULTAR_PRODUCTOS | "
                    + "ENTRADAS_SALIDAS"
            );
        }
    }

    private void configurarCierre() {
        Platform.runLater(() -> {
            if (lblBienvenida != null
                    && lblBienvenida.getScene() != null
                    && lblBienvenida
                            .getScene()
                            .getWindow() != null) {

                Stage stage
                        = (Stage) lblBienvenida
                                .getScene()
                                .getWindow();

                stage.setOnCloseRequest(event -> {
                    event.consume();

                    SesionUsuario
                            .getInstancia()
                            .cerrarSesion();

                    volverLogin();
                });
            }
        });
    }

    @FXML
    private void onVerLibrosCriticos() {
        if (!NavegacionRol.validarRol(Rol.BODEGA)) {
            return;
        }

        try {
            FXMLLoader loader
                    = new FXMLLoader(
                            getClass().getResource(
                                    "/org/esperanza/view/"
                                    + "LibrosCriticos.fxml"
                            )
                    );

            Parent root = loader.load();

            Stage ventana = new Stage();

            ventana.setTitle(
                    "Libros con stock crítico"
            );

            ventana.setScene(
                    new Scene(root)
            );

            ventana.initOwner(
                    lblBienvenida
                            .getScene()
                            .getWindow()
            );

            ventana.initModality(
                    Modality.WINDOW_MODAL
            );

            ventana.showAndWait();

            verificarAlertaStock();

        } catch (IOException e) {
            mostrarError(
                    "No se pudo abrir la lista "
                    + "de libros críticos.\n"
                    + e.getMessage()
            );
        }
    }

    @FXML
    private void onInventario() {

        if (!NavegacionRol.validarPermiso(
                "GESTION_INVENTARIO")) {
            return;
        }

        try {

            FXMLLoader loader
                    = new FXMLLoader(
                            getClass().getResource(
                                    "/org/esperanza/view/Inventario.fxml"
                            )
                    );

            Parent root
                    = loader.load();

            Stage ventana
                    = new Stage();

            ventana.initOwner(
                    lblBienvenida
                            .getScene()
                            .getWindow()
            );

            ventana.initModality(
                    Modality.WINDOW_MODAL
            );

            ventana.setTitle(
                    "Gestión de Inventario - Librería La Esperanza"
            );

            ventana.setScene(
                    new Scene(root)
            );

            ventana.setMinWidth(900);
            ventana.setMinHeight(560);

            ventana.setResizable(true);

            ventana.showAndWait();

            verificarAlertaStock();

        } catch (IOException e) {

            mostrarError(
                    "No se pudo abrir Gestión de Inventario.\n"
                    + e.getMessage()
            );
        }
    }

    @FXML
    private void onIngresoInventario() {

        if (!NavegacionRol.validarPermiso(
                "ENTRADAS_SALIDAS")) {
            return;
        }

        try {

            FXMLLoader loader
                    = new FXMLLoader(
                            getClass().getResource(
                                    "/org/esperanza/view/IngresoInventario.fxml"
                            )
                    );

            Parent root
                    = loader.load();

            IngresoInventarioController controller
                    = loader.getController();

            if (SesionUsuario
                    .getInstancia()
                    .getUsuarioActual() != null) {

                controller.setIdUsuarioActual(
                        SesionUsuario
                                .getInstancia()
                                .getUsuarioActual()
                                .getId()
                );
            }

            Stage ventana
                    = new Stage();

            ventana.initOwner(
                    lblBienvenida
                            .getScene()
                            .getWindow()
            );

            ventana.initModality(
                    Modality.WINDOW_MODAL
            );

            ventana.setTitle(
                    "Ingreso de inventario - Librería La Esperanza"
            );

            ventana.setScene(
                    new Scene(root)
            );

            ventana.setResizable(false);

            ventana.showAndWait();

            verificarAlertaStock();

        } catch (IOException e) {

            mostrarError(
                    "No se pudo abrir el formulario de ingreso.\n"
                    + e.getMessage()
            );
        }
    }

    @FXML
    private void onEntradasSalidas() {
        if (!NavegacionRol.validarPermiso(
                "ENTRADAS_SALIDAS")) {
            return;
        }

        try {
            FXMLLoader loader
                    = new FXMLLoader(
                            getClass().getResource(
                                    "/org/esperanza/view/"
                                    + "SalidaInventario.fxml"
                            )
                    );

            Parent root = loader.load();

            SalidaInventarioController controller
                    = loader.getController();

            Stage ventana = new Stage();

            ventana.initOwner(
                    lblBienvenida
                            .getScene()
                            .getWindow()
            );

            ventana.initModality(
                    Modality.WINDOW_MODAL
            );

            ventana.setTitle(
                    "Salida de inventario - "
                    + "Librería La Esperanza"
            );

            ventana.setScene(
                    new Scene(root)
            );

            ventana.setResizable(false);

            ventana.setOnCloseRequest(event -> {
                if (controller.estaOcupado()) {
                    event.consume();
                }
            });

            ventana.showAndWait();

            verificarAlertaStock();

        } catch (IOException ex) {
            mostrarError(
                    "No se pudo abrir el formulario "
                    + "de salida.\n"
                    + ex.getMessage()
            );
        }
    }

    @FXML
    private void abrirFichaLibro() {

        if (tablaStockCritico == null) {
            return;
        }

        Libro libro
                = tablaStockCritico
                        .getSelectionModel()
                        .getSelectedItem();

        if (libro == null) {
            mostrarError(
                    "Por favor, seleccione un libro de la tabla."
            );
            return;
        }

        String informacion
                = "ISBN: " + libro.getIsbn()
                + "\nTítulo: " + libro.getTitulo()
                + "\nStock actual: " + libro.getStockActual()
                + "\nStock mínimo: " + libro.getStockMinimo();

        mostrarInfo(
                "Ficha del Libro",
                informacion
        );
    }

    @FXML
    private void onCerrarSesion() {
        SesionUsuario
                .getInstancia()
                .cerrarSesion();

        volverLogin();
    }

    private void volverLogin() {
        try {
            FXMLLoader loader
                    = new FXMLLoader(
                            getClass().getResource(
                                    "/org/esperanza/view/Login.fxml"
                            )
                    );

            Parent root = loader.load();

            if (lblBienvenida != null
                    && lblBienvenida.getScene() != null) {

                Stage stage
                        = (Stage) lblBienvenida
                                .getScene()
                                .getWindow();

                stage.setOnCloseRequest(null);

                stage.setScene(
                        new Scene(root)
                );

                stage.setTitle(
                        "Librería La Esperanza"
                );

                stage.centerOnScreen();
            }

        } catch (IOException e) {
            e.printStackTrace();

            mostrarError(
                    "No se pudo regresar al login.\n"
                    + e.getMessage()
            );
        }
    }

    private void redirigirDashboardCorrecto() {
        if (lblBienvenida == null
                || lblBienvenida.getScene() == null
                || lblBienvenida
                        .getScene()
                        .getWindow() == null) {
            return;
        }

        Stage stage
                = (Stage) lblBienvenida
                        .getScene()
                        .getWindow();

        if (SesionUsuario
                .getInstancia()
                .haySesionActiva()) {

            NavegacionRol
                    .abrirDashboardSegunRol(stage);

        } else {
            stage.close();
        }
    }

    private void mostrarInfo(
            String titulo,
            String mensaje) {

        Alert alert
                = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarError(String mensaje) {
        Alert alert
                = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
