package org.esperanza.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DetalleVenta {

    private int idDetalle;
    private int idVenta;
    private String isbn;
    private int cantidad;
    private BigDecimal precioUnitario;

    public DetalleVenta() {
        this.idDetalle = 0;
        this.idVenta = 0;
        this.isbn = "";
        this.cantidad = 1;
        this.precioUnitario = BigDecimal.ZERO.setScale(2);
    }

    public DetalleVenta(
            int idDetalle,
            int idVenta,
            String isbn,
            int cantidad,
            BigDecimal precioUnitario) {

        this.idDetalle = idDetalle;
        this.idVenta = idVenta;
        this.isbn = validarIsbn(isbn);
        this.cantidad = validarCantidad(cantidad);
        this.precioUnitario = validarPrecio(precioUnitario);
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
        this.isbn = validarIsbn(isbn);
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = validarCantidad(cantidad);
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(
            BigDecimal precioUnitario) {

        this.precioUnitario =
                validarPrecio(precioUnitario);
    }

    public BigDecimal getSubtotal() {

        if (precioUnitario == null) {
            return BigDecimal.ZERO.setScale(2);
        }

        return precioUnitario
                .multiply(
                        BigDecimal.valueOf(cantidad)
                )
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private static String validarIsbn(
            String isbn) {

        if (isbn == null
                || isbn.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El ISBN es obligatorio."
            );
        }

        return isbn.trim();
    }

    private static int validarCantidad(
            int cantidad) {

        if (cantidad <= 0) {

            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que cero."
            );
        }

        return cantidad;
    }

    private static BigDecimal validarPrecio(
            BigDecimal precioUnitario) {

        if (precioUnitario == null) {

            throw new IllegalArgumentException(
                    "El precio unitario es obligatorio."
            );
        }

        if (precioUnitario.compareTo(
                BigDecimal.ZERO
        ) < 0) {

            throw new IllegalArgumentException(
                    "El precio unitario no puede ser negativo."
            );
        }

        return precioUnitario.setScale(
                2,
                RoundingMode.HALF_UP
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