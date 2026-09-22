package org.esperanza.test;

import java.math.BigDecimal;
import java.sql.SQLException;
import org.esperanza.dao.VentaDao;
import org.esperanza.model.CarritoVenta;

/** Comprueba que la venta y el cambio de stock comparten una transacción. */
public class PruebaVentaStock {

    private static CarritoVenta carrito(int cantidad) {
        CarritoVenta carrito = new CarritoVenta();
        carrito.agregarProducto("978-0-124", cantidad, new BigDecimal("25.50"));
        return carrito;
    }

    private static void ventaExitosa() throws SQLException {
        PruebaVentas.BaseSimulada base = new PruebaVentas.BaseSimulada();
        base.stockDisponible = 5;
        CarritoVenta carrito = carrito(2);
        carrito.confirmarVenta(1L, 1, new VentaDao(base::conectar));
        PruebaVentas.verificar(base.stockDisponible == 3 && base.commit && !base.rollback,
                "El commit debe persistir exactamente dos unidades descontadas");
        PruebaVentas.verificar(carrito.estaVacio() && base.cerrada,
                "La venta confirmada debe vaciar el carrito y cerrar la conexión");
    }

    private static void stockInsuficiente() throws SQLException {
        PruebaVentas.BaseSimulada base = new PruebaVentas.BaseSimulada();
        base.stockDisponible = 1;
        CarritoVenta carrito = carrito(2);
        try {
            carrito.confirmarVenta(1L, 1, new VentaDao(base::conectar));
            throw new AssertionError("La venta sin stock se confirmó");
        } catch (IllegalArgumentException esperado) {
            PruebaVentas.verificar(base.stockDisponible == 1 && base.rollback
                            && !base.commit && !carrito.estaVacio(),
                    "La venta rechazada debe conservar stock y carrito");
        }
    }

    private static void falloDespuesDeDescontar() throws SQLException {
        PruebaVentas.BaseSimulada base = new PruebaVentas.BaseSimulada();
        base.stockDisponible = 5;
        base.fallarActualizacionNumero = 2;
        CarritoVenta carrito = carrito(2);
        carrito.agregarProducto("978-0-126", 1, new BigDecimal("10.00"));
        try {
            carrito.confirmarVenta(1L, 1, new VentaDao(base::conectar));
            throw new AssertionError("La segunda actualización debió fallar");
        } catch (SQLException esperado) {
            PruebaVentas.verificar(base.actualizacionesStock == 1 && base.rollback
                            && base.stockDisponible == 5 && !base.commit,
                    "Debe revertir también el stock descontado por el primer libro");
            PruebaVentas.verificar(!carrito.estaVacio() && base.cerrada,
                    "Un fallo JDBC debe conservar el carrito y cerrar la conexión");
        }
    }

    private static void falloAlConfirmar() throws SQLException {
        PruebaVentas.BaseSimulada base = new PruebaVentas.BaseSimulada();
        base.stockDisponible = 5;
        base.fallarCommit = true;
        try {
            carrito(2).confirmarVenta(1L, 1, new VentaDao(base::conectar));
            throw new AssertionError("El commit debió fallar");
        } catch (SQLException esperado) {
            PruebaVentas.verificar(base.stockDisponible == 5 && base.rollback
                            && !base.commit && base.cerrada,
                    "Si falla el commit se debe intentar revertir la venta y el stock");
        }
    }

    private static void errorAlRevertir() throws SQLException {
        PruebaVentas.BaseSimulada base = new PruebaVentas.BaseSimulada();
        base.fallarStock = true;
        base.fallarRollback = true;
        try {
            carrito(1).confirmarVenta(1L, 1, new VentaDao(base::conectar));
            throw new AssertionError("Se esperaba un fallo JDBC");
        } catch (SQLException esperado) {
            PruebaVentas.verificar(esperado.getMessage().contains("actualizar stock")
                            && esperado.getSuppressed().length == 1
                            && esperado.getSuppressed()[0] instanceof SQLException
                            && base.cerrada,
                    "El error del rollback debe adjuntarse al error original");
        }
    }

    public static void main(String[] args) throws SQLException {
        ventaExitosa();
        stockInsuficiente();
        falloDespuesDeDescontar();
        falloAlConfirmar();
        errorAlRevertir();
        System.out.println("OK - T4.I.7 y T4.I.11: venta, stock y rollback JDBC");
    }
}
