package org.esperanza.system;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.esperanza.dao.UsuarioDao;
import org.esperanza.Model.Usuario;
import org.esperanza.Service.SesionUsuario;

/**
 * Punto de entrada. La aplicación inicia directamente, sin pantalla de login.
 */
public class main {
    public static class Ventana extends Application {
        @Override
        public void start(Stage stage) throws Exception {
            UsuarioDao dao = new UsuarioDao();
            Usuario usuarioInicial = dao.listarUsuariosActivos().stream().findFirst().orElse(null);
            if (usuarioInicial != null) SesionUsuario.getInstancia().iniciarSesion(usuarioInicial);

            Parent root = FXMLLoader.load(getClass().getResource("/org/esperanza/view/DashboardAdmin.fxml"));
            Scene scene = new Scene(root, 1050, 700);
            stage.setTitle("Librería La Esperanza");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setOnCloseRequest(e -> SesionUsuario.getInstancia().cerrarSesion());
            stage.centerOnScreen();
            stage.show();
        }
    }

    public static void main(String[] args) {
        Application.launch(Ventana.class, args);
    }
}
