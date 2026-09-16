package org.esperanza.model;

import java.math.BigDecimal;

public class ResumenVentasDia {

    private int cantidadVentas;
    private BigDecimal subtotal;
    private BigDecimal descuentos;
    private BigDecimal total;

    public ResumenVentasDia(
            int cantidadVentas,
            BigDecimal subtotal,
            BigDecimal descuentos,
            BigDecimal total) {

        this.cantidadVentas = cantidadVentas;
        this.subtotal = subtotal;
        this.descuentos = descuentos;
        this.total = total;
    }

    public int getCantidadVentas() {
        return cantidadVentas;
    }

    public void setCantidadVentas(int cantidadVentas) {
        this.cantidadVentas = cantidadVentas;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDescuentos() {
        return descuentos;
    }

    public void setDescuentos(BigDecimal descuentos) {
        this.descuentos = descuentos;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}