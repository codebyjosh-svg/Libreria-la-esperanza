import java.math.BigDecimal;
import java.sql.SQLException;
import org.esperanza.Model.Usuario;
import org.esperanza.Service.SesionUsuario;
import org.esperanza.dao.VentaDao;
import org.esperanza.Model.CarritoVenta;
import org.esperanza.Model.DescuentoVenta;
import org.esperanza.Model.Venta;

public class PruebaDescuentos {

    private static void igual(BigDecimal esperado, BigDecimal real) {
        if (esperado.compareTo(real) != 0) {
            throw new AssertionError("Esperado Q" + esperado + ", obtenido Q" + real);
        }
    }

    private static void invalido(Runnable accion) {
        try {
            accion.run();
            throw new AssertionError("Se aceptó un descuento inválido");
        } catch (IllegalArgumentException esperado) {
            // La venta debe rechazar un valor fuera de las reglas.
        }
    }

    private static void reglas() {
        BigDecimal subtotal = new BigDecimal("61.00");
        igual(new BigDecimal("0.00"),
                DescuentoVenta.sinDescuento().calcularMonto(subtotal));
        igual(new BigDecimal("6.10"),
                DescuentoVenta.porcentaje(new BigDecimal("10")).calcularMonto(subtotal));
        igual(new BigDecimal("7.63"),
                DescuentoVenta.porcentaje(new BigDecimal("12.5")).calcularMonto(subtotal));
        igual(new BigDecimal("5.50"),
                DescuentoVenta.monto(new BigDecimal("5.50")).calcularMonto(subtotal));
        igual(subtotal,
                DescuentoVenta.porcentaje(new BigDecimal("100")).calcularMonto(subtotal));
        invalido(() -> DescuentoVenta.porcentaje(new BigDecimal("100.01")));
        invalido(() -> DescuentoVenta.porcentaje(new BigDecimal("-1")));
        invalido(() -> DescuentoVenta.monto(new BigDecimal("1.234")));
        invalido(() -> DescuentoVenta.monto(new BigDecimal("62.00"))
                .calcularMonto(subtotal));
        System.out.println("OK - Porcentaje, monto, redondeo y límites");
    }

    private static void iniciar(String rol) {
        Usuario usuario = new Usuario(1, "prueba", rol, "Prueba", "Sistema", "", true);
        if (!SesionUsuario.getInstancia().iniciarSesion(usuario)) {
            throw new AssertionError("No se pudo iniciar la sesión de prueba");
        }
    }

    private static void autorizado() throws SQLException {
        iniciar("ADMIN");
        PruebaVentas.BaseSimulada base = new PruebaVentas.BaseSimulada();
        CarritoVenta carrito = PruebaVentas.crearCarrito();
        Venta venta = carrito.confirmarVenta(1L, 1, new VentaDao(base::conectar),
                DescuentoVenta.porcentaje(new BigDecimal("10")));
        igual(new BigDecimal("61.00"), venta.getSubtotal());
        igual(new BigDecimal("6.10"), venta.getDescuento());
        igual(new BigDecimal("54.90"), venta.getTotal());
        igual(venta.getSubtotal(), base.subtotalRegistrado);
        igual(venta.getDescuento(), base.descuentoRegistrado);
        igual(venta.getTotal(), base.totalRegistrado);
        PruebaVentas.verificar(base.autorizaciones == 1 && base.commit,
                "No se verificó el administrador ni se guardó la venta");
        PruebaVentas.verificar(carrito.estaVacio(), "El carrito debe vaciarse tras el commit");

        base = new PruebaVentas.BaseSimulada();
        Venta ventaMonto = PruebaVentas.crearCarrito().confirmarVenta(
                1L, 1, new VentaDao(base::conectar),
                DescuentoVenta.monto(new BigDecimal("5.00")));
        igual(new BigDecimal("56.00"), ventaMonto.getTotal());
        igual(new BigDecimal("5.00"), base.descuentoRegistrado);
        System.out.println("OK - Venta con descuento guardado y total ajustado");
    }

    private static void sinPermiso() throws SQLException {
        iniciar("CAJERO");
        PruebaVentas.BaseSimulada base = new PruebaVentas.BaseSimulada();
        CarritoVenta carrito = PruebaVentas.crearCarrito();
        try {
            carrito.confirmarVenta(1L, 1, new VentaDao(base::conectar),
                    DescuentoVenta.monto(new BigDecimal("1.00")));
            throw new AssertionError("El cajero pudo aplicar el descuento");
        } catch (SecurityException esperado) {
            PruebaVentas.verificar(!base.commit && base.insertados == 0
                    && !carrito.estaVacio(), "La venta no autorizada no debe guardarse");
        }

        iniciar("ADMIN");
        base = new PruebaVentas.BaseSimulada();
        base.usuarioAdmin = false;
        carrito = PruebaVentas.crearCarrito();
        try {
            carrito.confirmarVenta(1L, 1, new VentaDao(base::conectar),
                    DescuentoVenta.monto(new BigDecimal("1.00")));
            throw new AssertionError("Se aceptó un rol distinto en la base");
        } catch (SecurityException esperado) {
            PruebaVentas.verificar(base.rollback && base.insertados == 0
                    && !carrito.estaVacio(), "Debe revertir la venta si el rol cambió");
        }

        base = new PruebaVentas.BaseSimulada();
        base.usuarioActivo = false;
        carrito = PruebaVentas.crearCarrito();
        try {
            carrito.confirmarVenta(1L, 1, new VentaDao(base::conectar),
                    DescuentoVenta.monto(new BigDecimal("1.00")));
            throw new AssertionError("Se aceptó un administrador inactivo");
        } catch (SecurityException esperado) {
            PruebaVentas.verificar(base.rollback && base.insertados == 0,
                    "Debe revertir la venta del usuario inactivo");
        }
        System.out.println("OK - Cajero, rol cambiado e inactivo rechazados");
    }

    public static void main(String[] args) throws Exception {
        try {
            reglas();
            autorizado();
            sinPermiso();
        } finally {
            SesionUsuario.getInstancia().cerrarSesion();
        }
    }
}
