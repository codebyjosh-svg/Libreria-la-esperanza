package org.esperanza.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class VentaDia {

    private int idVenta;
    private LocalDateTime fechaVenta;
    private long cuiCliente;
    private String cliente;
    private int idUsuario;
    private String usuario;
    private BigDecimal subtotal;
    private BigDecimal descuento;
    private BigDecimal total;
    private String estado;

    public VentaDia(
            int idVenta,
            LocalDateTime fechaVenta,
            long cuiCliente,
            String cliente,
            int idUsuario,
            String usuario,
            BigDecimal subtotal,
            BigDecimal descuento,
            BigDecimal total,
            String estado) {

        this.idVenta = idVenta;
        this.fechaVenta = fechaVenta;
        this.cuiCliente = cuiCliente;
        this.cliente = cliente;
        this.idUsuario = idUsuario;
        this.usuario = usuario;
        this.subtotal = subtotal;
        this.descuento = descuento;
        this.total = total;
        this.estado = estado;
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

    public void setFechaVenta(LocalDateTime fechaVenta) {
        this.fechaVenta = fechaVenta;
    }

    public String getHora() {
        if (fechaVenta == null) {
            return "";
        }

        return fechaVenta.format(
                DateTimeFormatter.ofPattern("HH:mm:ss")
        );
    }

    public long getCuiCliente() {
        return cuiCliente;
    }

    public void setCuiCliente(long cuiCliente) {
        this.cuiCliente = cuiCliente;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDescuento() {
        return descuento;
    }

    public void setDescuento(BigDecimal descuento) {
        this.descuento = descuento;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}