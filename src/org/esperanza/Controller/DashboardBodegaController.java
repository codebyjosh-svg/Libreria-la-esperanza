package org.esperanza.Controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

import org.esperanza.Service.NavegacionRol;
import org.esperanza.Service.SesionUsuario;
import org.esperanza.dao.StockDao;
import org.esperanza.model.Libro;

public class DashboardBodegaController {

    @FXML private Label lblBienvenida;
    @FXML private Label lblRol;
    @FXML private Label lblPermisos;
    @FXML private Button btnCerrarSesion;
    @FXML private Label lblContadorCritico;

    @FXML private TableView<Libro> tablaStockCritico;
    @FXML private TableColumn<Libro, String> colIsbn;
    @FXML private TableColumn<Libro, String> colTitulo;
    @FXML private TableColumn<Libro, Integer> colStock;

    private StockDao stockDao = new StockDao();

    @FXML
    private void initialize() {

        actualizarEncabezado();
        configurarCierre();

        colIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockActual"));

        cargarInventarioCritico();
    }

    private void cargarInventarioCritico() {
        try {
            List<Libro> listaPura = stockDao.obtenerStockCritico(5);
            ObservableList<Libro> librosCriticos = FXCollections.observableArrayList(listaPura);

            tablaStockCritico.setItems(librosCriticos);
            lblContadorCritico.setText(String.valueOf(librosCriticos.size()));

        } catch (SQLException e) {
            mostrarError("No se pudo cargar el stock crítico:\n" + e.getMessage());
        }
    }

    @FXML
    private void abrirFichaLibro() {
        Libro libroSeleccionado = tablaStockCritico.getSelectionModel().getSelectedItem();
        if (libroSeleccionado != null) {
            mostrarInfo("Ficha del Libro", "Abriendo ficha para ISBN: " + libroSeleccionado.getIsbn());
        } else {
            mostrarError("Por favor, seleccione un libro de la tabla.");
        }
    }

    private void actualizarEncabezado() {

        SesionUsuario sesion =
                SesionUsuario
                        .getInstancia();

        lblBienvenida.setText(
                "Bienvenido, "
                + sesion.getNombreCompleto()
        );

        lblRol.setText(
                "Rol: BODEGA"
        );

        lblPermisos.setText(
                "Permisos: GESTION_INVENTARIO | "
                + "CONSULTAR_PRODUCTOS | "
                + "ENTRADAS_SALIDAS"
        );
    }

    private void configurarCierre() {

        Platform.runLater(() -> {

            Stage stage = (Stage) lblBienvenida
                    .getScene()
                    .getWindow();

            stage.setOnCloseRequest(event -> {

                event.consume();

                SesionUsuario
                        .getInstancia()
                        .cerrarSesion();

                volverLogin();
            });
        });
    }

    @FXML
    private void onInventario() {

        if (!NavegacionRol.validarPermiso(
                "GESTION_INVENTARIO")) {

            return;
        }

        mostrarInfo(
                "Inventario",
                "Acceso a Gestión de Inventario autorizado."
        );
    }

    @FXML
    private void onEntradasSalidas() {

        if (!NavegacionRol.validarPermiso(
                "ENTRADAS_SALIDAS")) {

            return;
        }

        mostrarInfo(
                "Entradas / Salidas",
                "Acceso al registro de entradas y salidas autorizado."
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

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/org/esperanza/view/Login.fxml"
                    )
            );

            Parent root = loader.load();

            Stage stage = (Stage) lblBienvenida
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

        } catch (IOException e) {

            e.printStackTrace();

            mostrarError(
                    "No se pudo regresar al login.\n"
                    + e.getMessage()
            );
        }
    }

    private void mostrarInfo(
            String titulo,
            String mensaje) {

        Alert alert = new Alert(
                Alert.AlertType.INFORMATION
        );

        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarError(
            String mensaje) {

        Alert alert = new Alert(
                Alert.AlertType.ERROR
        );

        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}