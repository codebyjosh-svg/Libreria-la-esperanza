import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.esperanza.controller.TicketVentaController;
import org.esperanza.model.DetalleVenta;

public class PruebaComprobante extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/esperanza/view/TicketVenta.fxml")
        );

        Parent root = loader.load();
        TicketVentaController controller = loader.getController();

        List<DetalleVenta> detalles = List.of(
                new DetalleVenta(
                        0,
                        10,
                        "978-0-123",
                        2,
                        new BigDecimal("150.00")
                ),
                new DetalleVenta(
                        0,
                        10,
                        "978-0-126",
                        1,
                        new BigDecimal("180.00")
                )
        );

        Map<String, String> titulos = Map.of(
                "978-0-123", "Cien Años de Soledad",
                "978-0-126", "Harry Potter y la Piedra Filosofal"
        );

        controller.setDatosVenta(
                10,
                LocalDateTime.now(),
                2000100010101L,
                5,
                new BigDecimal("480.00"),
                BigDecimal.ZERO,
                new BigDecimal("480.00")
        );

        controller.setNombreCliente(
                "Ana López",
                2000100010101L
        );

        controller.setDetalles(
                detalles,
                titulos
        );

        stage.setTitle("Prueba del comprobante");
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}