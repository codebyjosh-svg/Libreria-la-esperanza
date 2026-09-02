package org.esperanza.Model;

public class Usuario {
    private int id;
    private String usrname;
    private String rol;
    private String nombre;
    private String apellido;
    private boolean activo;

    public Usuario() {
    }

    public Usuario(int id, String usrname, String rol, String nombre, String apellido, boolean activo) {
        this.id = id;
        this.usrname = usrname;
        this.rol = rol;
        this.nombre = nombre;
        this.apellido = apellido;
        this.activo = activo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsrname() {
        return usrname;
    }

    public void setUsrname(String usrname) {
        this.usrname = usrname;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
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

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}