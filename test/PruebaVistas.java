import java.nio.file.*;
import java.util.concurrent.*;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.embed.swing.SwingFXUtils;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import org.esperanza.Model.Usuario;
import org.esperanza.Service.SesionUsuario;
import org.esperanza.Controller.ProveedoresController;
import org.esperanza.Controller.DevolucionesController;

/** Carga FXML real y obtiene capturas; necesita una BD de pruebas ya configurada. */
public class PruebaVistas {
    record Vista(Parent root, Object controller, Stage stage) {}
    static <T> T enFx(Callable<T> trabajo) throws Exception {
        FutureTask<T> tarea = new FutureTask<>(trabajo);
        Platform.runLater(tarea);
        return tarea.get(20, TimeUnit.SECONDS);
    }
    static boolean ocupado(Object controller) {
        return (controller instanceof ProveedoresController p && p.estaOcupado())
            || (controller instanceof DevolucionesController d && d.estaOcupado());
    }
    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Indica directorio de capturas.");
        Path salida = Path.of(args[0]); Files.createDirectories(salida);
        CountDownLatch inicio = new CountDownLatch(1);
        Platform.startup(inicio::countDown);
        if (!inicio.await(15, TimeUnit.SECONDS)) throw new AssertionError("JavaFX no inició");
        Platform.setImplicitExit(false);
        try {
            for (String nombre : new String[]{"DashboardAdmin", "Usuarios", "UsuarioAlta", "Proveedores", "Devoluciones", "DashboardCajero", "DashboardBodega"}) {
                String rol = nombre.equals("DashboardCajero") ? "CAJERO" : nombre.equals("DashboardBodega") ? "BODEGA" : "ADMIN";
                Vista vista = enFx(() -> {
                    SesionUsuario.getInstancia().iniciarSesion(new Usuario(1,"pruebas",rol,"Prueba","Local","prueba@example.test",true));
                    FXMLLoader loader = new FXMLLoader(PruebaVistas.class.getResource("/org/esperanza/view/"+nombre+".fxml"));
                    Parent root = loader.load();
                    Stage stage = new Stage(); stage.setScene(new Scene(root));
                    return new Vista(root, loader.getController(), stage);
                });
                long limite = System.nanoTime()+TimeUnit.SECONDS.toNanos(15);
                while (enFx(() -> ocupado(vista.controller()))) {
                    if (System.nanoTime()>limite) throw new AssertionError("Vista bloqueada: "+nombre);
                    Thread.sleep(50);
                }
                enFx(() -> {
                    vista.root().applyCss(); vista.root().layout();
                    if (nombre.equals("Proveedores")) {
                        String mensaje=((Label)vista.root().lookup("#lblMensaje")).getText();
                        if (!mensaje.startsWith("Proveedores encontrados:")) throw new AssertionError(mensaje);
                    }
                    if (nombre.equals("Devoluciones")) {
                        String mensaje=((Label)vista.root().lookup("#lblEstado")).getText();
                        if (!mensaje.contains("venta(s)")) throw new AssertionError(mensaje);
                    }
                    ImageIO.write(SwingFXUtils.fromFXImage(vista.root().snapshot(null,null),null),"png",salida.resolve(nombre+".png").toFile());
                    vista.stage().close();
                    return null;
                });
                System.out.println("OK FXML y captura: "+nombre);
            }
            enFx(() -> {
                SesionUsuario.getInstancia().iniciarSesion(new Usuario(3,"bodega","BODEGA","Prueba","Bodega","b@example.test",true));
                Parent proveedores=FXMLLoader.load(PruebaVistas.class.getResource("/org/esperanza/view/Proveedores.fxml"));
                Parent devoluciones=FXMLLoader.load(PruebaVistas.class.getResource("/org/esperanza/view/Devoluciones.fxml"));
                if(!proveedores.lookup("#contenido").isDisabled() || !devoluciones.isDisabled()) throw new AssertionError("BODEGA accedió a módulos restringidos");
                return null;
            });
            System.out.println("OK BODEGA: formularios restringidos bloqueados.");
        } finally { Platform.exit(); }
    }
}
