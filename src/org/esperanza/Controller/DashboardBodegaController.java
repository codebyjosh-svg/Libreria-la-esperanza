package org.esperanza.Controller;

import java.util.List;
import java.util.function.BooleanSupplier;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import org.esperanza.dao.LibroDAO;
import org.esperanza.dao.impl.LibroDAOImpl;
import org.esperanza.Model.Libro;
import org.esperanza.Model.Rol;
import org.esperanza.Service.NavegacionRol;
import org.esperanza.Service.Pantallas;
import org.esperanza.Service.SesionUsuario;

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

    private final LibroDAO libroDAO = new LibroDAOImpl();

    @FXML
    private void initialize() {
        if (!NavegacionRol.validarRol(Rol.BODEGA)) {
            Platform.runLater(this::redirigirDashboardCorrecto);
            return;
        }

        SesionUsuario sesion = SesionUsuario.getInstancia();

        lblBienvenida.setText(
                "Bienvenido, " + sesion.getNombreCompleto()
        );
        lblRol.setText("Rol: Bodega");

        if (lblPermisos != null) {
            lblPermisos.setText("");
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

        Platform.runLater(() -> {
            if (lblBienvenida.getScene() == null
                    || lblBienvenida.getScene().getWindow() == null) {
                return;
            }

            Stage stage = ventana();

            stage.setOnCloseRequest(event -> {
                event.consume();
                onCerrarSesion();
            });

            verificarAlertaStock();
        });
    }

    private Stage ventana() {
        return (Stage) lblBienvenida.getScene().getWindow();
    }

    private void verificarAlertaStock() {
        try {
            List<Libro> libros = libroDAO.obtenerStockCritico();

            if (libros == null) {
                libros = List.of();
            }

            tablaStockCritico.getItems().setAll(libros);

            String cantidad = String.valueOf(libros.size());

            if (lblContadorCritico != null) {
                lblContadorCritico.setText(cantidad);
            }

            if (lblCantidadStockCritico != null) {
                lblCantidadStockCritico.setText(cantidad);
                lblCantidadStockCritico.setStyle(
                        libros.isEmpty()
                        ? "-fx-text-fill:#10B981;"
                        + "-fx-font-weight:bold;"
                        : "-fx-text-fill:#EF4444;"
                        + "-fx-font-weight:bold;"
                );
            }

            if (!libros.isEmpty()) {
                StringBuilder mensaje = new StringBuilder(
                        "Los siguientes libros alcanzaron "
                        + "o cayeron por debajo del stock mínimo:\n\n"
                );

                for (Libro libro : libros) {
                    mensaje.append("• ")
                            .append(libro.getTitulo())
                            .append(" | Stock actual: ")
                            .append(libro.getStockActual())
                            .append(" (Mínimo: ")
                            .append(libro.getStockMinimo())
                            .append(")\n");
                }

                Alert alerta = new Alert(Alert.AlertType.WARNING);
                alerta.initOwner(ventana());
                alerta.setTitle("Alerta de Inventario");
                alerta.setHeaderText("Reabastecimiento requerido");
                alerta.setContentText(mensaje.toString());
                alerta.showAndWait();
            }

        } catch (Exception ex) {
            if (lblCantidadStockCritico != null) {
                lblCantidadStockCritico.setText("No disponible");
            }

            Pantallas.error(
                    "No se pudo consultar el stock crítico.\n"
                    + ex.getMessage()
            );
        }
    }
    
    private void mostrarEnMismaVentana(
            Parent contenido,
            String titulo,
            BooleanSupplier ocupado
    ) {
        Stage stage = ventana();

        stage.setOnCloseRequest(null);
        stage.setScene(new Scene(contenido, 950, 620));
        stage.setTitle(titulo + " - Librería La Esperanza");
        stage.setResizable(true);
        stage.setMaximized(false);

        stage.setOnCloseRequest(event -> {
            event.consume();

            if (!ocupado.getAsBoolean()) {
                volverDashboard(stage);
            }
        });

        stage.sizeToScene();
        stage.centerOnScreen();
    }

    private void volverDashboard(Stage stage) {
        stage.setOnCloseRequest(null);
        NavegacionRol.abrirDashboardSegunRol(stage);
    }

    private void abrirPantalla(String archivo, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/org/esperanza/view/" + archivo + ".fxml"
                    )
            );

            Parent contenido = loader.load();

            mostrarEnMismaVentana(
                    contenido,
                    titulo,
                    () -> false
            );

        } catch (Exception ex) {
            Pantallas.error(
                    "No se pudo abrir " + titulo + ".\n"
                    + ex.getMessage()
            );
        }
    }

    @FXML
    private void onInventario() {
        if (!NavegacionRol.validarPermiso("GESTION_INVENTARIO")) {
            return;
        }

        abrirPantalla("Inventario", "Gestión de Inventario");
    }

    @FXML
    private void onIngresoInventario() {
        if (!NavegacionRol.validarPermiso("ENTRADAS_SALIDAS")) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/org/esperanza/view/IngresoInventario.fxml"
                    )
            );

            Parent contenido = loader.load();

            IngresoInventarioController Controller
                    = loader.getController();

            Controller.setIdUsuarioActual(
                    SesionUsuario.getInstancia()
                            .getUsuarioActual()
                            .getId()
            );

            mostrarEnMismaVentana(
                    contenido,
                    "Ingreso de inventario",
                    () -> false
            );

        } catch (Exception ex) {
            Pantallas.error(
                    "No se pudo abrir el ingreso de inventario.\n"
                    + ex.getMessage()
            );
        }
    }

    @FXML
    private void onEntradasSalidas() {
        if (!NavegacionRol.validarPermiso("ENTRADAS_SALIDAS")) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/org/esperanza/view/SalidaInventario.fxml"
                    )
            );

            Parent contenido = loader.load();

            SalidaInventarioController Controller
                    = loader.getController();

            mostrarEnMismaVentana(
                    contenido,
                    "Salida de inventario",
                    Controller::estaOcupado
            );

        } catch (Exception ex) {
            Pantallas.error(
                    "No se pudo abrir la salida de inventario.\n"
                    + ex.getMessage()
            );
        }
    }

    @FXML
    private void onVerLibrosCriticos() {
        if (!NavegacionRol.validarRol(Rol.BODEGA)) {
            return;
        }

        abrirPantalla(
                "LibrosCriticos",
                "Libros con stock crítico"
        );
    }

    @FXML
    private void onLibros() {
        if (!NavegacionRol.validarRol(Rol.BODEGA)) {
            return;
        }

        Pantallas.abrir(lblBienvenida, "Libros", "Libros");
    }

    @FXML
    private void onCategorias() {
        if (!NavegacionRol.validarRol(Rol.BODEGA)) {
            return;
        }

        Pantallas.catalogo(
                lblBienvenida,
                "categorias",
                "Categorías"
        );
    }

    @FXML
    private void onAutores() {

        if (!NavegacionRol.validarRol(Rol.BODEGA)) {
            return;
        }

        Pantallas.catalogo(
                lblBienvenida,
                "autores",
                "Autores"
        );
    }

    @FXML
    private void abrirFichaLibro() {
        Libro libro
                = tablaStockCritico.getSelectionModel().getSelectedItem();

        if (libro == null) {
            Pantallas.error("Selecciona un libro de la tabla.");
            return;
        }

        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.initOwner(ventana());
        alerta.setTitle("Ficha del libro");
        alerta.setHeaderText(null);
        alerta.setContentText(
                "ISBN: " + libro.getIsbn()
                + "\nTítulo: " + libro.getTitulo()
                + "\nStock actual: " + libro.getStockActual()
                + "\nStock mínimo: " + libro.getStockMinimo()
        );
        alerta.showAndWait();
    }

    @FXML
    private void onCerrarSesion() {
        try {
            Parent contenido = FXMLLoader.load(
                    getClass().getResource(
                            "/org/esperanza/view/Login.fxml"
                    )
            );

            Stage stage = ventana();

            stage.setOnCloseRequest(null);
            SesionUsuario.getInstancia().cerrarSesion();

            stage.setScene(new Scene(contenido));
            stage.setTitle("Librería La Esperanza");
            stage.setMaximized(false);
            stage.sizeToScene();
            stage.centerOnScreen();

        } catch (Exception ex) {
            Pantallas.error(
                    "No se pudo abrir el login.\n" + ex.getMessage()
            );
        }
    }

    private void redirigirDashboardCorrecto() {
        if (lblBienvenida.getScene() == null
                || lblBienvenida.getScene().getWindow() == null) {
            return;
        }

        if (SesionUsuario.getInstancia().haySesionActiva()) {
            NavegacionRol.abrirDashboardSegunRol(ventana());
        } else {
            onCerrarSesion();
        }
    }
}
