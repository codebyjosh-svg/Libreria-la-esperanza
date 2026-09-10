package org.esperanza.model;

import java.math.BigDecimal;
import java.util.Objects;

public class DetalleVenta {

    private int idDetalle;
    private int idVenta;
    private String isbn;
    private int cantidad;
    private BigDecimal precioUnitario;

    public DetalleVenta() {
        this.cantidad = 1;
        this.precioUnitario = BigDecimal.ZERO;
    }

    public DetalleVenta(
            int idDetalle,
            int idVenta,
            String isbn,
            int cantidad,
            BigDecimal precioUnitario) {

        this.idDetalle = idDetalle;
        this.idVenta = idVenta;

        setIsbn(isbn);
        setCantidad(cantidad);
        setPrecioUnitario(precioUnitario);
    }

    public int getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(int idDetalle) {
        this.idDetalle = idDetalle;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {

        if (isbn == null || isbn.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El ISBN es obligatorio"
            );
        }

        this.isbn = isbn.trim();
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {

        if (cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que cero"
            );
        }

        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(
            BigDecimal precioUnitario) {

        Objects.requireNonNull(
                precioUnitario,
                "El precio unitario es obligatorio"
        );

        if (precioUnitario.signum() < 0) {
            throw new IllegalArgumentException(
                    "El precio unitario no puede ser negativo"
            );
        }

        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getSubtotal() {

        return precioUnitario.multiply(
                BigDecimal.valueOf(cantidad)
        );
    }

    @Override
    public String toString() {

        return "DetalleVenta{"
                + "idDetalle=" + idDetalle
                + ", idVenta=" + idVenta
                + ", isbn='" + isbn + '\''
                + ", cantidad=" + cantidad
                + ", precioUnitario=" + precioUnitario
                + ", subtotal=" + getSubtotal()
                + '}';
    }
}