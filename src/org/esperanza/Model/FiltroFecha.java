package org.esperanza.model;

public enum FiltroFecha {

    DIA("Día"),
    SEMANA("Semana"),
    MES("Mes");

    private final String nombre;

    FiltroFecha(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}