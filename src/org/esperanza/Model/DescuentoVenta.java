package org.esperanza.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** Reglas de descuento de una venta. El resultado siempre es un monto en quetzales. */
public final class DescuentoVenta {

    public enum Tipo {
        NINGUNO("Sin descuento"),
        PORCENTAJE("Porcentaje (%)"),
        MONTO("Monto fijo (Q)");

        private final String nombre;

        Tipo(String nombre) {
            this.nombre = nombre;
        }

        @Override
        public String toString() {
            return nombre;
        }
    }

    private final Tipo tipo;
    private final BigDecimal valor;

    private DescuentoVenta(Tipo tipo, BigDecimal valor) {
        this.tipo = Objects.requireNonNull(tipo, "tipo");
        try {
            this.valor = Objects.requireNonNull(valor, "valor")
                    .setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Usa como máximo dos decimales.", e);
        }
        if (this.valor.signum() < 0) {
            throw new IllegalArgumentException("El descuento no puede ser negativo.");
        }
        if (tipo == Tipo.PORCENTAJE
                && this.valor.compareTo(new BigDecimal("100.00")) > 0) {
            throw new IllegalArgumentException("El porcentaje debe estar entre 0 y 100.");
        }
    }

    public static DescuentoVenta sinDescuento() {
        return new DescuentoVenta(Tipo.NINGUNO, BigDecimal.ZERO);
    }

    public static DescuentoVenta porcentaje(BigDecimal porcentaje) {
        return new DescuentoVenta(Tipo.PORCENTAJE, porcentaje);
    }

    public static DescuentoVenta monto(BigDecimal monto) {
        return new DescuentoVenta(Tipo.MONTO, monto);
    }

    public Tipo getTipo() {
        return tipo;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public BigDecimal calcularMonto(BigDecimal subtotal) {
        BigDecimal base;
        try {
            base = Objects.requireNonNull(subtotal, "subtotal")
                    .setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("El subtotal debe tener dos decimales.", e);
        }
        if (base.signum() < 0) {
            throw new IllegalArgumentException("El subtotal no puede ser negativo.");
        }

        BigDecimal descuento = switch (tipo) {
            case NINGUNO -> BigDecimal.ZERO.setScale(2);
            case PORCENTAJE -> base.multiply(valor)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            case MONTO -> valor;
        };
        if (descuento.compareTo(base) > 0) {
            throw new IllegalArgumentException(
                    "El descuento no puede superar el subtotal de la venta.");
        }
        return descuento;
    }
}
