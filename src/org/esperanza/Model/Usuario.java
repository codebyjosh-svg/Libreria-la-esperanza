
package org.esperanza.Model;

/**
 *
 * @author Joshua
 */
public class Usuario {

    private int id;
    private String usrname;
    private String rol;

    public Usuario() {
    }

    public Usuario(int id, String usrname, String rol) {
        this.id = id;
        this.usrname = usrname;
        this.rol = rol;
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
}
