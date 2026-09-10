package org.esperanza.Controller;

import java.io.IOException;

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

import org.esperanza.Model.Usuario;
import org.esperanza.dao.UsuarioDao;

public class UsuariosController {

    @FXML
    private TableView<Usuario> tablaUsuarios;

    @FXML
    private TableColumn<Usuario, Integer> colId;

    @FXML
    private TableColumn<Usuario, String> colUsuario;

    @FXML
    private TableColumn<Usuario, String> colNombre;

    @FXML
    private TableColumn<Usuario, String> colApellido;

    @FXML
    private TableColumn<Usuario, String> colRol;

    @FXML
    private TableColumn<Usuario, String> colCorreo;

    @FXML
    private TableColumn<Usuario, Boolean> colActivo;

    @FXML
    private TableColumn<Usuario, Void> colAccion;

    @FXML
    private Label lblMensaje;

    private final UsuarioDao usuarioDao =
            new UsuarioDao();

    private final ObservableList<Usuario> datos =
            FXCollections.observableArrayList();

    // =====================================================
    // INICIALIZAR
    // =====================================================

    @FXML
    private void initialize() {

        colId.setCellValueFactory(
                new PropertyValueFactory<>("id")
        );

        colUsuario.setCellValueFactory(
                new PropertyValueFactory<>("usrname")
        );

        colNombre.setCellValueFactory(
                new PropertyValueFactory<>("nombre")
        );

        colApellido.setCellValueFactory(
                new PropertyValueFactory<>("apellido")
        );

        colRol.setCellValueFactory(
                new PropertyValueFactory<>("rol")
        );

        colCorreo.setCellValueFactory(
                new PropertyValueFactory<>("correo")
        );

        colActivo.setCellValueFactory(
                new PropertyValueFactory<>("activo")
        );

        configurarEstado();
        configurarAccion();
        cargarUsuarios();
    }

    // =====================================================
    // MOSTRAR ESTADO ACTIVO / INACTIVO
    // =====================================================

    private void configurarEstado() {

        colActivo.setCellFactory(
                col -> new TableCell<>() {

                    @Override
                    protected void updateItem(
                            Boolean activo,
                            boolean empty) {

                        super.updateItem(
                                activo,
                                empty
                        );

                        if (empty || activo == null) {

                            setText(null);

                            getStyleClass()
                                    .removeAll(
                                            "estado-activo",
                                            "estado-inactivo"
                                    );

                            return;
                        }

                        setText(
                                activo
                                        ? "Activo"
                                        : "Inactivo"
                        );

                        getStyleClass()
                                .removeAll(
                                        "estado-activo",
                                        "estado-inactivo"
                                );

                        getStyleClass()
                                .add(
                                        activo
                                                ? "estado-activo"
                                                : "estado-inactivo"
                                );
                    }
                }
        );
    }

    // =====================================================
    // BOTÓN DESACTIVAR
    // =====================================================

    private void configurarAccion() {

        colAccion.setCellFactory(
                col -> new TableCell<>() {

                    private final Button btn =
                            new Button(
                                    "Desactivar"
                            );

                    {
                        btn.getStyleClass()
                                .add(
                                        "btn-desactivar"
                                );

                        btn.setOnAction(
                                e -> {

                                    int indice =
                                            getIndex();

                                    if (indice >= 0
                                            && indice
                                            < getTableView()
                                                    .getItems()
                                                    .size()) {

                                        Usuario usuario =
                                                getTableView()
                                                        .getItems()
                                                        .get(indice);

                                        desactivar(
                                                usuario
                                        );
                                    }
                                }
                        );
                    }

                    @Override
                    protected void updateItem(
                            Void item,
                            boolean empty) {

                        super.updateItem(
                                item,
                                empty
                        );

                        if (empty
                                || getIndex() < 0
                                || getIndex()
                                >= getTableView()
                                        .getItems()
                                        .size()) {

                            setGraphic(null);
                            return;
                        }

                        Usuario usuario =
                                getTableView()
                                        .getItems()
                                        .get(
                                                getIndex()
                                        );

                        if (usuario == null
                                || !usuario.isActivo()) {

                            setGraphic(null);

                        } else {

                            setGraphic(btn);
                        }
                    }
                }
        );
    }

    // =====================================================
    // NUEVO USUARIO
    // =====================================================

    @FXML
    private void onNuevoUsuario(
            ActionEvent event) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass()
                                    .getResource(
                                            "/org/esperanza/view/UsuarioAlta.fxml"
                                    )
                    );

            Parent root =
                    loader.load();

            Stage stage =
                    new Stage();

            stage.setTitle(
                    "Alta de usuario - Librería La Esperanza"
            );

            stage.setScene(
                    new Scene(
                            root,
                            520,
                            610
                    )
            );

            stage.setResizable(
                    false
            );

            stage.setOnHidden(
                    e -> cargarUsuarios()
            );

            stage.show();

        } catch (IOException e) {

            e.printStackTrace();

            mostrarError(
                    "No se pudo abrir el formulario de alta: "
                    + e.getMessage()
            );
        }
    }

    // =====================================================
    // ACTUALIZAR LISTADO
    // =====================================================

    @FXML
    private void onActualizar(
            ActionEvent event) {

        cargarUsuarios();
    }

    // =====================================================
    // VOLVER AL DASHBOARD ADMIN
    // =====================================================

    @FXML
    private void onVolverDashboard() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass()
                                    .getResource(
                                            "/org/esperanza/view/DashboardAdmin.fxml"
                                    )
                    );

            Parent root =
                    loader.load();

            Stage stage =
                    (Stage) tablaUsuarios
                            .getScene()
                            .getWindow();

            stage.setScene(
                    new Scene(root)
            );

            stage.setTitle(
                    "Librería La Esperanza - Panel Administrador"
            );

            stage.centerOnScreen();

        } catch (IOException e) {

            e.printStackTrace();

            mostrarError(
                    "No se pudo regresar al Dashboard.\n"
                    + e.getMessage()
            );
        }
    }

    // =====================================================
    // CARGAR USUARIOS
    // =====================================================

    private void cargarUsuarios() {

        datos.setAll(
                usuarioDao
                        .listarUsuarios()
        );

        tablaUsuarios.setItems(
                datos
        );

        lblMensaje.setText(
                "Usuarios encontrados: "
                + datos.size()
        );
    }

    // =====================================================
    // DESACTIVAR USUARIO
    // =====================================================

    private void desactivar(
            Usuario usuario) {

        if (usuario == null
                || !usuario.isActivo()) {

            return;
        }

        Alert confirmacion =
                new Alert(
                        Alert.AlertType.CONFIRMATION,
                        "¿Deseas desactivar al usuario "
                        + usuario.getUsrname()
                        + "?",
                        ButtonType.YES,
                        ButtonType.NO
                );

        confirmacion.setTitle(
                "Desactivar usuario"
        );

        confirmacion.setHeaderText(
                null
        );

        confirmacion
                .showAndWait()
                .ifPresent(
                        respuesta -> {

                            if (respuesta
                                    == ButtonType.YES) {

                                if (usuarioDao
                                        .desactivarUsuario(
                                                usuario.getId()
                                        )) {

                                    mostrarInfo(
                                            "Usuario desactivado correctamente."
                                    );

                                    cargarUsuarios();

                                } else {

                                    mostrarError(
                                            "No se pudo desactivar el usuario."
                                    );
                                }
                            }
                        }
                );
    }

    // =====================================================
    // MENSAJE DE INFORMACIÓN
    // =====================================================

    private void mostrarInfo(
            String mensaje) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(
                "Gestión de usuarios"
        );

        alert.setHeaderText(
                null
        );

        alert.setContentText(
                mensaje
        );

        alert.showAndWait();
    }

    // =====================================================
    // MENSAJE DE ERROR
    // =====================================================

    private void mostrarError(
            String mensaje) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle(
                "Error"
        );

        alert.setHeaderText(
                null
        );

        alert.setContentText(
                mensaje
        );

        alert.showAndWait();
    }
}