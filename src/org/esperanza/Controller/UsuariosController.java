package org.esperanza.Controller;

import java.io.IOException;
import java.util.Objects;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.esperanza.dao.UsuarioDao;
import org.esperanza.Model.Usuario;
import org.esperanza.Service.SesionUsuario;

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

    @FXML
    private Button btnEditar;

    private final UsuarioDao usuarioDao
            = new UsuarioDao();

    private final ObservableList<Usuario> datos
            = FXCollections.observableArrayList();

    @FXML
    private void initialize() {

        if (!esAdminActual()) {

            throw new SecurityException(
                    "Solo un administrador puede gestionar usuarios."
            );
        }

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

        tablaUsuarios.setItems(
                datos
        );

        btnEditar
                .disableProperty()
                .bind(
                        tablaUsuarios
                                .getSelectionModel()
                                .selectedItemProperty()
                                .isNull()
                );

        configurarEstado();
        configurarAccion();
        cargarUsuarios();
    }

    private boolean esAdminActual() {

        SesionUsuario sesion
                = SesionUsuario.getInstancia();

        Usuario actual
                = sesion.getUsuarioActual();

        return sesion.haySesionActiva()
                && actual != null
                && actual.isActivo()
                && sesion.esAdmin();
    }

    private boolean puedeGestionar(
            Usuario usuario) {

        Usuario actual
                = SesionUsuario
                        .getInstancia()
                        .getUsuarioActual();

        return esAdminActual()
                && actual != null
                && usuario != null
                && usuario.getId()
                != actual.getId();
    }

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

                if (empty
                        || activo == null) {

                    setText(null);
                    return;
                }

                setText(
                        activo
                                ? "Activo"
                                : "Inactivo"
                );
            }
        });
    }

    private void configurarAccion() {

        colAccion.setCellFactory(
                col -> new TableCell<>() {

            private final Button btn
                    = new Button();

            {
                btn.setOnAction(
                        e -> {

                            Usuario usuario
                            = getTableRow()
                                    .getItem();

                            if (usuario != null) {

                                cambiarEstado(
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

                Usuario usuario
                        = getTableView()
                                .getItems()
                                .get(
                                        getIndex()
                                );

                if (!puedeGestionar(
                        usuario)) {

                    setGraphic(null);
                    return;
                }

                btn.setText(
                        usuario.isActivo()
                        ? "Desactivar"
                        : "Activar"
                );

                btn.setStyle(
                        usuario.isActivo()
                        ? "-fx-background-color:#dc2626;"
                        + "-fx-text-fill:white;"
                        : "-fx-background-color:#16a34a;"
                        + "-fx-text-fill:white;"
                );

                setGraphic(btn);
            }
        });
    }

    @FXML
    private void onEditarUsuario(
            ActionEvent event) {

        if (!esAdminActual()) {

            mostrarError(
                    "Solo un administrador puede editar usuarios."
            );

            return;
        }

        Usuario seleccionado
                = tablaUsuarios
                        .getSelectionModel()
                        .getSelectedItem();

        if (seleccionado == null) {

            mostrarError(
                    "Selecciona un usuario para editar."
            );

            return;
        }

        Dialog<ButtonType> dialogo
                = new Dialog<>();

        dialogo.initOwner(
                tablaUsuarios
                        .getScene()
                        .getWindow()
        );

        dialogo.setTitle(
                "Editar usuario"
        );

        dialogo.setHeaderText(
                "Modificar datos de "
                + seleccionado.getUsrname()
        );

        TextField txtUsuario
                = new TextField(
                        Objects.toString(
                                seleccionado.getUsrname(),
                                ""
                        )
                );

        TextField txtNombre
                = new TextField(
                        Objects.toString(
                                seleccionado.getNombre(),
                                ""
                        )
                );

        TextField txtApellido
                = new TextField(
                        Objects.toString(
                                seleccionado.getApellido(),
                                ""
                        )
                );

        ComboBox<String> cmbRol
                = new ComboBox<>();

        cmbRol.getItems().addAll(
                "ADMIN",
                "CAJERO",
                "BODEGA"
        );

        String rolActual
                = Objects.toString(
                        seleccionado.getRol(),
                        ""
                )
                        .trim()
                        .toUpperCase();

        cmbRol.getSelectionModel()
                .select(rolActual);

        Usuario usuarioActual
                = SesionUsuario
                        .getInstancia()
                        .getUsuarioActual();

        if (usuarioActual != null
                && usuarioActual.getId()
                == seleccionado.getId()) {

            cmbRol.setDisable(true);
        }

        TextField txtCorreo
                = new TextField(
                        Objects.toString(
                                seleccionado.getCorreo(),
                                ""
                        )
                );

        txtUsuario.setPrefColumnCount(
                25
        );

        cmbRol.setPrefWidth(
                200
        );

        GridPane formulario
                = new GridPane();

        formulario.setPadding(
                new Insets(20)
        );

        formulario.setHgap(12);
        formulario.setVgap(12);

        formulario.addRow(
                0,
                new Label("Usuario:"),
                txtUsuario
        );

        formulario.addRow(
                1,
                new Label("Nombre:"),
                txtNombre
        );

        formulario.addRow(
                2,
                new Label("Apellido:"),
                txtApellido
        );

        formulario.addRow(
                3,
                new Label("Rol:"),
                cmbRol
        );

        formulario.addRow(
                4,
                new Label("Correo:"),
                txtCorreo
        );

        dialogo
                .getDialogPane()
                .setContent(
                        formulario
                );

        ButtonType guardar
                = new ButtonType(
                        "Guardar cambios",
                        ButtonBar.ButtonData.OK_DONE
                );

        ButtonType cancelar
                = new ButtonType(
                        "Cancelar",
                        ButtonBar.ButtonData.CANCEL_CLOSE
                );

        dialogo
                .getDialogPane()
                .getButtonTypes()
                .addAll(
                        guardar,
                        cancelar
                );

        dialogo
                .getDialogPane()
                .lookupButton(
                        guardar
                )
                .addEventFilter(
                        ActionEvent.ACTION,
                        e -> {

                            try {

                                if (!esAdminActual()) {

                                    throw new SecurityException(
                                            "Solo Admin puede editar usuarios."
                                    );
                                }

                                if (cmbRol.getValue() == null) {

                                    throw new IllegalArgumentException(
                                            "Selecciona un rol."
                                    );
                                }

                                usuarioDao.editarUsuario(
                                        seleccionado.getId(),
                                        txtUsuario
                                                .getText()
                                                .trim(),
                                        cmbRol.getValue(),
                                        txtNombre
                                                .getText()
                                                .trim(),
                                        txtApellido
                                                .getText()
                                                .trim(),
                                        txtCorreo
                                                .getText()
                                                .trim()
                                );

                            } catch (Exception ex) {

                                e.consume();

                                Alert alerta
                                = new Alert(
                                        Alert.AlertType.ERROR
                                );

                                alerta.initOwner(
                                        dialogo
                                                .getDialogPane()
                                                .getScene()
                                                .getWindow()
                                );

                                alerta.setTitle(
                                        "Editar usuario"
                                );

                                alerta.setHeaderText(
                                        null
                                );

                                alerta.setContentText(
                                        ex.getMessage()
                                );

                                alerta.showAndWait();
                            }
                        }
                );

        if (dialogo
                .showAndWait()
                .orElse(cancelar)
                == guardar) {

            cargarUsuarios();

            for (Usuario usuario : datos) {

                if (usuario.getId()
                        == seleccionado.getId()) {

                    tablaUsuarios
                            .getSelectionModel()
                            .select(usuario);

                    tablaUsuarios
                            .scrollTo(usuario);

                    break;
                }
            }

            lblMensaje.setStyle(
                    "-fx-text-fill:#166534;"
            );

            lblMensaje.setText(
                    "Usuario actualizado correctamente."
            );
        }
    }

    private void cambiarEstado(
            Usuario usuario) {

        if (!puedeGestionar(
                usuario)) {

            mostrarError(
                    "No puedes cambiar el estado de este usuario."
            );

            return;
        }

        boolean nuevoEstado
                = !usuario.isActivo();

        String accion
                = nuevoEstado
                        ? "activar"
                        : "desactivar";

        Alert alerta
                = new Alert(
                        Alert.AlertType.CONFIRMATION,
                        "¿Deseas "
                        + accion
                        + " al usuario "
                        + usuario.getUsrname()
                        + "?",
                        ButtonType.YES,
                        ButtonType.NO
                );

        alerta.initOwner(
                tablaUsuarios
                        .getScene()
                        .getWindow()
        );

        alerta.setHeaderText(null);

        alerta.setTitle(
                nuevoEstado
                        ? "Activar usuario"
                        : "Desactivar usuario"
        );

        if (alerta
                .showAndWait()
                .orElse(
                        ButtonType.NO
                )
                != ButtonType.YES) {

            return;
        }

        if (!puedeGestionar(
                usuario)) {

            mostrarError(
                    "No puedes cambiar el estado de este usuario."
            );

            return;
        }

        if (usuarioDao
                .cambiarEstadoUsuario(
                        usuario.getId(),
                        nuevoEstado
                )) {

            cargarUsuarios();

            lblMensaje.setStyle(
                    "-fx-text-fill:#166534;"
            );

            lblMensaje.setText(
                    "Usuario "
                    + usuario.getUsrname()
                    + (nuevoEstado
                            ? " activado."
                            : " desactivado.")
            );

        } else {

            mostrarError(
                    "No se pudo cambiar el estado del usuario."
            );
        }
    }

    @FXML
    private void onNuevoUsuario(
            ActionEvent event) {

        if (!esAdminActual()) {

            mostrarError(
                    "Solo un administrador puede gestionar usuarios."
            );

            return;
        }

        try {

            FXMLLoader loader
                    = new FXMLLoader(
                            getClass()
                                    .getResource(
                                            "/org/esperanza/view/UsuarioAlta.fxml"
                                    )
                    );

            Parent root
                    = loader.load();

            Stage stage
                    = new Stage();

            stage.initOwner(
                    tablaUsuarios
                            .getScene()
                            .getWindow()
            );

            stage.initModality(
                    Modality.WINDOW_MODAL
            );

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

            mostrarError(
                    "No se pudo abrir el formulario: "
                    + e.getMessage()
            );
        }
    }

    @FXML
    private void onActualizar(
            ActionEvent event) {

        cargarUsuarios();
    }

    @FXML
    private void onVolverDashboard() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/org/esperanza/view/DashboardAdmin.fxml"
                    )
            );

            Parent root = loader.load();

            Stage stage = (Stage) tablaUsuarios
                    .getScene()
                    .getWindow();

            stage.setScene(new Scene(root));

            stage.setTitle(
                    "Panel Administrador - Librería La Esperanza"
            );

            stage.sizeToScene();
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {

            mostrarError(
                    "No se pudo regresar al Dashboard Admin.\n"
                    + e.getMessage()
            );
        }
    }

    private void cargarUsuarios() {

        if (!esAdminActual()) {

            datos.clear();

            tablaUsuarios.setItems(
                    datos
            );

            lblMensaje.setStyle(
                    "-fx-text-fill:#b91c1c;"
            );

            lblMensaje.setText(
                    "Solo un administrador con sesión activa puede gestionar usuarios."
            );

            return;
        }

        datos.setAll(
                usuarioDao.listarUsuarios()
        );

        tablaUsuarios.refresh();

        lblMensaje.setStyle(
                "-fx-text-fill:#374151;"
        );

        lblMensaje.setText(
                "Usuarios encontrados: "
                + datos.size()
        );
    }

    private void mostrarError(
            String mensaje) {

        lblMensaje.setStyle(
                "-fx-text-fill:#b91c1c;"
        );

        lblMensaje.setText(
                mensaje
        );

        Alert alert
                = new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle(
                "Gestión de Usuarios"
        );

        alert.setHeaderText(
                null
        );

        alert.setContentText(
                mensaje
        );

        if (tablaUsuarios.getScene()
                != null) {

            alert.initOwner(
                    tablaUsuarios
                            .getScene()
                            .getWindow()
            );
        }

        alert.showAndWait();
    }
}
