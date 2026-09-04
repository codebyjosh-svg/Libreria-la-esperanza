package org.esperanza.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.*;
import org.esperanza.Dao.VentaDao;


/** Carrito en memoria para conectar a un controlador JavaFX. */
public class CarritoVenta {
    private final Map<Integer, DetalleVenta> productos = new LinkedHashMap<>();

    public void agregarProducto(int idProducto, int cantidad, BigDecimal precioUnitario) {
        if (idProducto <= 0) throw new IllegalArgumentException("Producto invalido");
        BigDecimal precio = Objects.requireNonNull(precioUnitario).setScale(2, RoundingMode.UNNECESSARY);
        if (precio.precision() > 19) throw new IllegalArgumentException("Precio fuera de rango");
        DetalleVenta nuevo = new DetalleVenta(0, 0, idProducto, cantidad, precio);
        DetalleVenta actual = productos.get(idProducto);
        if (actual != null) {
            if (actual.getPrecioUnitario().compareTo(precio) != 0) {
                throw new IllegalArgumentException("El producto ya tiene otro precio; retiralo antes de cambiarlo");
            }
            nuevo.setCantidad(Math.addExact(actual.getCantidad(), cantidad));
        }
        productos.put(idProducto, nuevo);
    }

    public void cambiarCantidad(int idProducto, int cantidad) {
        DetalleVenta detalle = productos.get(idProducto);
        if (detalle == null) throw new IllegalArgumentException("El producto no esta en el carrito");
        detalle.setCantidad(cantidad);
    }

    public boolean quitarProducto(int idProducto) { return productos.remove(idProducto) != null; }
    public boolean estaVacio() { return productos.isEmpty(); }
    public void vaciar() { productos.clear(); }

    /** Devuelve copias para proteger el estado interno del carrito. */
    public List<DetalleVenta> getDetalles() {
        List<DetalleVenta> copia = new ArrayList<>();
        for (DetalleVenta d : productos.values()) {
            copia.add(new DetalleVenta(0, 0, d.getIdProducto(), d.getCantidad(), d.getPrecioUnitario()));
        }
        return Collections.unmodifiableList(copia);
    }

    public BigDecimal getTotal() {
        return productos.values().stream().map(DetalleVenta::getSubtotal)
                .reduce(new BigDecimal("0.00"), BigDecimal::add);
    }

    /** Solo vacia el carrito despues de un registro exitoso. */
    public Venta confirmarVenta(int idCliente, int idEmpleado, VentaDao ventaDao) throws SQLException {
        Venta venta = Objects.requireNonNull(ventaDao).registrar(idCliente, idEmpleado, getDetalles());
        vaciar();
        return venta;
    }
}


