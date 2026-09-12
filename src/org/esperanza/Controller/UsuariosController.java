package org.esperanza.Controller;

import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.esperanza.Model.Usuario;
import org.esperanza.Service.SesionUsuario;
import org.esperanza.dao.UsuarioDao;

public class UsuariosController {

    @FXML private TableView<Usuario> tablaUsuarios;
    @FXML private TableColumn<Usuario, Integer> colId;
    @FXML private TableColumn<Usuario, String> colUsuario;
    @FXML private TableColumn<Usuario, String> colNombre;
    @FXML private TableColumn<Usuario, String> colApellido;
    @FXML private TableColumn<Usuario, String> colRol;
    @FXML private TableColumn<Usuario, String> colCorreo;
    @FXML private TableColumn<Usuario, Boolean> colActivo;
    @FXML private TableColumn<Usuario, Void> colAccion;
    @FXML private Label lblMensaje;

    private final UsuarioDao usuarioDao = new UsuarioDao();
    private final ObservableList<Usuario> datos = FXCollections.observableArrayList();
    private Usuario usuarioActual;

    @FXML
    private void initialize() {
        usuarioActual = SesionUsuario.getInstancia().getUsuarioActual();

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usrname"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        colCorreo.setCellValueFactory(new PropertyValueFactory<>("correo"));
        colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));

        configurarEstado();
        configurarAccion();
        cargarUsuarios();
    }

    private void configurarEstado() {
        colActivo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean activo, boolean empty) {
                super.updateItem(activo, empty);
                if (empty || activo == null) {
                    setText(null);
                    return;
                }
                setText(activo ? "Activo" : "Inactivo");
            }
        });
    }

    private void configurarAccion() {
        colAccion.setCellFactory(col -> new TableCell<>() {

            private final Button btn = new Button();

            {
                btn.setOnAction(e -> {
                    Usuario usuario = getTableView().getItems().get(getIndex());
                    cambiarEstado(usuario);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }

                Usuario usuario = getTableView().getItems().get(getIndex());

                if (!puedeGestionar(usuario)) {
                    setGraphic(null);
                    return;
                }

                btn.setText(usuario.isActivo() ? "Desactivar" : "Activar");
                btn.setStyle(usuario.isActivo()
                        ? "-fx-background-color:#dc2626; -fx-text-fill:white;"
                        : "-fx-background-color:#16a34a; -fx-text-fill:white;");

                setGraphic(btn);
            }
        });
    }

    private boolean esAdminActual() {
        return usuarioActual != null
                && usuarioActual.getRol() != null
                && usuarioActual.getRol().equalsIgnoreCase("admin");
    }

    private boolean puedeGestionar(Usuario usuario) {
        return esAdminActual()
                && usuario != null
                && usuario.getId() != usuarioActual.getId();
    }

    private void cambiarEstado(Usuario usuario) {
        if (!puedeGestionar(usuario)) {
            mostrarError("No puedes cambiar el estado de este usuario.");
            return;
        }

        boolean nuevoEstado = !usuario.isActivo();
        String accion = nuevoEstado ? "activar" : "desactivar";

        Alert alerta = new Alert(
                Alert.AlertType.CONFIRMATION,
                "¿Deseas " + accion + " al usuario " + usuario.getUsrname() + "?",
                ButtonType.YES,
                ButtonType.NO
        );

        alerta.setHeaderText(null);
        alerta.setTitle(nuevoEstado ? "Activar usuario" : "Desactivar usuario");

        if (alerta.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) return;

        if (usuarioDao.cambiarEstadoUsuario(usuario.getId(), nuevoEstado)) {
            cargarUsuarios();
            lblMensaje.setStyle("-fx-text-fill:#166534;");
            lblMensaje.setText(
                    "Usuario " + usuario.getUsrname()
                    + (nuevoEstado ? " activado." : " desactivado.")
            );
        } else {
            mostrarError("No se pudo cambiar el estado del usuario.");
        }
    }

    @FXML
    private void onNuevoUsuario(ActionEvent event) {
        if (!esAdminActual()) {
            mostrarError("Solo un administrador puede gestionar usuarios.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/esperanza/view/UsuarioAlta.fxml")
            );

            Parent root = loader.load();
            Stage stage = new Stage();

            stage.setTitle("Alta de usuario - Librería La Esperanza");
            stage.setScene(new Scene(root, 520, 610));
            stage.setResizable(false);
            stage.setOnHidden(e -> cargarUsuarios());
            stage.show();

        } catch (IOException e) {
            mostrarError("No se pudo abrir el formulario: " + e.getMessage());
        }
    }

    @FXML
    private void onActualizar(ActionEvent event) {
        usuarioActual = SesionUsuario.getInstancia().getUsuarioActual();
        cargarUsuarios();
    }

    @FXML
    private void onVolverDashboard() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/org/esperanza/view/DashboardAdmin.fxml")
            );

            Stage stage = (Stage) tablaUsuarios.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Librería La Esperanza - Panel Administrador");
            stage.centerOnScreen();

        } catch (IOException e) {
            mostrarError("No se pudo regresar al Dashboard.");
        }
    }

    private void cargarUsuarios() {
        datos.setAll(usuarioDao.listarUsuarios());
        tablaUsuarios.setItems(datos);
        tablaUsuarios.refresh();

        lblMensaje.setStyle("-fx-text-fill:#374151;");
        lblMensaje.setText("Usuarios encontrados: " + datos.size());
    }

    private void mostrarError(String mensaje) {
        lblMensaje.setStyle("-fx-text-fill:#b91c1c;");
        lblMensaje.setText(mensaje);

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Gestión de Usuarios");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}