package org.esperanza.model;

public class Cliente {

    private long cui;
    private String nombre;
    private String apellido;
    private String correo;

    public Cliente() {
    }

    public Cliente(
            long cui,
            String nombre,
            String apellido,
            String correo) {

        this.cui = cui;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
    }

    public long getCui() {
        return cui;
    }

    public void setCui(long cui) {
        this.cui = cui;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
}