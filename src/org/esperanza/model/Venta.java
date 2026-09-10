package org.esperanza.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Venta {

    private int idVenta;
    private LocalDateTime fechaVenta;

    private BigDecimal subtotal;
    private BigDecimal descuento;
    private BigDecimal total;

    private long cuiCliente;
    private int idUsuario;

    public Venta() {

        this.fechaVenta = LocalDateTime.now();
        this.subtotal = BigDecimal.ZERO;
        this.descuento = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
    }

    public Venta(
            int idVenta,
            LocalDateTime fechaVenta,
            BigDecimal subtotal,
            BigDecimal descuento,
            BigDecimal total,
            long cuiCliente,
            int idUsuario) {

        this.idVenta = idVenta;
        this.fechaVenta = Objects.requireNonNull(fechaVenta);
        this.subtotal = Objects.requireNonNull(subtotal);
        this.descuento = Objects.requireNonNull(descuento);
        this.total = Objects.requireNonNull(total);
        this.cuiCliente = cuiCliente;
        this.idUsuario = idUsuario;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public LocalDateTime getFechaVenta() {
        return fechaVenta;
    }

    public void setFechaVenta(
            LocalDateTime fechaVenta) {

        this.fechaVenta =
                Objects.requireNonNull(fechaVenta);
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(
            BigDecimal subtotal) {

        this.subtotal =
                Objects.requireNonNull(subtotal);
    }

    public BigDecimal getDescuento() {
        return descuento;
    }

    public void setDescuento(
            BigDecimal descuento) {

        this.descuento =
                Objects.requireNonNull(descuento);
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(
            BigDecimal total) {

        this.total =
                Objects.requireNonNull(total);
    }

    public long getCuiCliente() {
        return cuiCliente;
    }

    public void setCuiCliente(
            long cuiCliente) {

        this.cuiCliente = cuiCliente;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(
            int idUsuario) {

        this.idUsuario = idUsuario;
    }

    @Override
    public String toString() {

        return "Venta{"
                + "idVenta=" + idVenta
                + ", fechaVenta=" + fechaVenta
                + ", subtotal=" + subtotal
                + ", descuento=" + descuento
                + ", total=" + total
                + ", cuiCliente=" + cuiCliente
                + ", idUsuario=" + idUsuario
                + '}';
    }
}