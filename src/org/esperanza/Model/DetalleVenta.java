package org.esperanza.model;

import java.math.BigDecimal;
import java.util.Objects;

/** Producto vendido, vinculado a una venta mediante idVenta. */
public class DetalleVenta {
    private int idDetalleVenta;
    private int idVenta;
    private int idProducto;
    private int cantidad;
    private BigDecimal precioUnitario;

    public DetalleVenta() {
        cantidad = 1;
        precioUnitario = BigDecimal.ZERO;
    }

    public DetalleVenta(int idDetalleVenta, int idVenta, int idProducto,
            int cantidad, BigDecimal precioUnitario) {
        this.idDetalleVenta = idDetalleVenta;
        this.idVenta = idVenta;
        this.idProducto = idProducto;
        setCantidad(cantidad);
        setPrecioUnitario(precioUnitario);
    }

    public int getIdDetalleVenta() { return idDetalleVenta; }
    public void setIdDetalleVenta(int idDetalleVenta) { this.idDetalleVenta = idDetalleVenta; }
    public int getIdVenta() { return idVenta; }
    public void setIdVenta(int idVenta) { this.idVenta = idVenta; }
    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        this.cantidad = cantidad;
    }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) {
        Objects.requireNonNull(precioUnitario, "El precio unitario es obligatorio");
        if (precioUnitario.signum() < 0) {
            throw new IllegalArgumentException("El precio unitario no puede ser negativo");
        }
        this.precioUnitario = precioUnitario;
    }

    /** Se calcula para mantenerlo actualizado al cambiar la cantidad o el precio. */
    public BigDecimal getSubtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    @Override
    public String toString() {
        return "DetalleVenta{idDetalleVenta=" + idDetalleVenta
                + ", idVenta=" + idVenta + ", idProducto=" + idProducto
                + ", cantidad=" + cantidad + ", precioUnitario=" + precioUnitario
                + ", subtotal=" + getSubtotal() + '}';
    }
}

