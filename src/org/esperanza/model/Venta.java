package org.esperanza.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/** Datos generales de una venta. El total corresponde a la suma de sus detalles. */
public class Venta {
    private int idVenta;
    private LocalDateTime fecha;
    private int idCliente;
    private int idEmpleado;
    private BigDecimal total;

    public Venta() {
        fecha = LocalDateTime.now();
        total = BigDecimal.ZERO;
    }

    public Venta(int idVenta, LocalDateTime fecha, int idCliente,
            int idEmpleado, BigDecimal total) {
        this.idVenta = idVenta;
        setFecha(fecha);
        this.idCliente = idCliente;
        this.idEmpleado = idEmpleado;
        setTotal(total);
    }

    public int getIdVenta() { return idVenta; }
    public void setIdVenta(int idVenta) { this.idVenta = idVenta; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) {
        this.fecha = Objects.requireNonNull(fecha, "La fecha es obligatoria");
    }
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    public int getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(int idEmpleado) { this.idEmpleado = idEmpleado; }
    public BigDecimal getTotal() { return total; }

    /** El DAO o servicio debe asignar la suma de los subtotales al guardar la venta. */
    public void setTotal(BigDecimal total) {
        Objects.requireNonNull(total, "El total es obligatorio");
        if (total.signum() < 0) {
            throw new IllegalArgumentException("El total no puede ser negativo");
        }
        this.total = total;
    }

    @Override
    public String toString() {
        return "Venta{idVenta=" + idVenta + ", fecha=" + fecha
                + ", idCliente=" + idCliente + ", idEmpleado=" + idEmpleado
                + ", total=" + total + '}';
    }
}

