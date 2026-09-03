package org.esperanza.Controller;

import java.io.IOException;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.esperanza.dao.UsuarioDao;
import org.esperanza.Model.Usuario;

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

    @FXML
    private void initialize() {
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
            @Override protected void updateItem(Boolean activo, boolean empty) {
                super.updateItem(activo, empty);
                setText(empty || activo == null ? null : activo ? "Activo" : "Inactivo");
                getStyleClass().removeAll("estado-activo", "estado-inactivo");
                if (!empty && activo != null) getStyleClass().add(activo ? "estado-activo" : "estado-inactivo");
            }
        });
    }

    private void configurarAccion() {
        colAccion.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Desactivar");
            {
                btn.getStyleClass().add("btn-desactivar");
                btn.setOnAction(e -> desactivar(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableView().getItems().get(getIndex()).isActivo() == false) setGraphic(null);
                else setGraphic(btn);
            }
        });
    }

    @FXML
    private void onNuevoUsuario(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/esperanza/view/UsuarioAlta.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Alta de usuario - Librería La Esperanza");
            stage.setScene(new Scene(root, 520, 610));
            stage.setResizable(false);
            stage.setOnHidden(e -> cargarUsuarios());
            stage.show();
        } catch (IOException e) {
            mostrarError("No se pudo abrir el formulario de alta: " + e.getMessage());
        }
    }

    @FXML
    private void onActualizar(ActionEvent event) { cargarUsuarios(); }

    private void cargarUsuarios() {
        datos.setAll(usuarioDao.listarUsuarios());
        tablaUsuarios.setItems(datos);
        lblMensaje.setText("Usuarios encontrados: " + datos.size());
    }

    private void desactivar(Usuario usuario) {
        if (usuario == null || !usuario.isActivo()) return;
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Deseas desactivar al usuario " + usuario.getUsrname() + "?",
                ButtonType.YES, ButtonType.NO);
        confirmacion.setTitle("Desactivar usuario");
        confirmacion.setHeaderText(null);
        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.YES) {
                if (usuarioDao.desactivarUsuario(usuario.getId())) {
                    mostrarInfo("Usuario desactivado correctamente.");
                    cargarUsuarios();
                } else {
                    mostrarError("No se pudo desactivar el usuario.");
                }
            }
        });
    }

    private void mostrarInfo(String mensaje) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Gestión de usuarios"); a.setHeaderText(null); a.setContentText(mensaje); a.showAndWait();
    }

    private void mostrarError(String mensaje) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error"); a.setHeaderText(null); a.setContentText(mensaje); a.showAndWait();
    }
}
