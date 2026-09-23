package org.esperanza.model;

import java.util.Locale;

/** Solo una venta completada puede recibir una devolución total. */
public enum EstadoVenta {
    COMPLETADA, DEVUELTA, ANULADA, DESCONOCIDA;

    public boolean permiteDevolucion() { return this == COMPLETADA; }

    public static EstadoVenta desdeBaseDatos(String valor) {
        if (valor == null) return DESCONOCIDA;
        try {
            return valueOf(valor.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return DESCONOCIDA;
        }
    }
}
