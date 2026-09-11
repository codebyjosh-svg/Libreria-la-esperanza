package org.esperanza.Model;

public enum TipoSalida {
    MERMA("Merma"),
    TRASLADO("Traslado"),
    DEVOLUCION_PROVEEDOR("Devolución");

    private final String nombre;

    TipoSalida(String nombre) { this.nombre = nombre; }

    @Override public String toString() { return nombre; }
}
