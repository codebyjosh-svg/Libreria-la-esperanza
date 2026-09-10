import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;
import org.esperanza.controller.TicketVentaController;
import org.esperanza.model.DetalleVenta;

public class PruebaTicket extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/esperanza/view/TicketVenta.fxml")
        );

        Parent root = loader.load();
        TicketVentaController controller = loader.getController();

        controller.setDatosVenta(
                5,
                LocalDateTime.now(),
                2000100010101L,
                5,
                new BigDecimal("315.50"),
                BigDecimal.ZERO,
                new BigDecimal("315.50")
        );

        controller.setDetalles(List.of(
                new DetalleVenta(
                        0, 5, "978-0-124",
                        1, new BigDecimal("135.50")
                ),
                new DetalleVenta(
                        0, 5, "978-0-126",
                        1, new BigDecimal("180.00")
                )
        ));

        stage.setScene(new Scene(root));
        stage.setTitle("Ticket de venta");
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}