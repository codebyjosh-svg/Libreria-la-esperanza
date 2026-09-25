package org.esperanza.Model;

import java.util.Locale;
import java.util.regex.Pattern;

/** Datos inmutables de un proveedor; las mismas reglas se aplican fuera de JavaFX. */
public final class Proveedor {
    private static final Pattern CORREO = Pattern.compile(
            "^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?"
            + "(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)+$");

    private final int id;
    private final String nit;
    private final String nombre;
    private final String contacto;
    private final String telefono;
    private final String correo;
    private final String direccion;
    private final boolean activo;

    public Proveedor(int id, String nit, String nombre, String contacto, String telefono,
            String correo, String direccion, boolean activo) {
        if (id < 0) throw new IllegalArgumentException("El identificador no puede ser negativo.");
        this.id = id;
        String identificador = texto(nit, "NIT", 35, true).toUpperCase(Locale.ROOT);
        if (!identificador.matches("[A-Z0-9 -]+")) {
            throw new IllegalArgumentException("El NIT solo admite letras, números, espacios y guiones.");
        }
        this.nit = identificador.replace("-", "").replace(" ", "");
        if (this.nit.length() < 3 || this.nit.length() > 25) {
            throw new IllegalArgumentException("El NIT debe tener entre 3 y 25 letras o números.");
        }
        this.nombre = texto(nombre, "Nombre", 150, true);
        if (this.nombre.length() < 2) throw new IllegalArgumentException("El nombre debe tener al menos 2 caracteres.");
        this.contacto = texto(contacto, "Contacto", 100, false);
        this.telefono = texto(telefono, "Teléfono", 25, true);
        if (!this.telefono.matches("\\+?[0-9 ()-]+")) {
            throw new IllegalArgumentException("El teléfono solo admite números, + inicial, espacios, guiones y paréntesis.");
        }
        int digitos = this.telefono.replaceAll("[^0-9]", "").length();
        if (digitos < 7 || digitos > 15) throw new IllegalArgumentException("El teléfono debe contener entre 7 y 15 dígitos.");
        this.correo = texto(correo, "Correo", 150, false);
        if (!this.correo.isEmpty() && (!CORREO.matcher(this.correo).matches()
                || this.correo.indexOf('@') > 64 || this.correo.startsWith(".")
                || this.correo.contains("..") || this.correo.contains(".@"))) {
            throw new IllegalArgumentException("Ingrese un correo válido, por ejemplo contacto@empresa.com.");
        }
        this.direccion = texto(direccion, "Dirección", 250, false);
        this.activo = activo;
    }

    private static String texto(String valor, String campo, int maximo, boolean requerido) {
        String limpio = valor == null ? "" : valor.trim();
        if (requerido && limpio.isEmpty()) throw new IllegalArgumentException(campo + " es obligatorio.");
        if (limpio.length() > maximo) throw new IllegalArgumentException(campo + " admite hasta " + maximo + " caracteres.");
        if (limpio.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException(campo + " contiene caracteres de control no permitidos.");
        }
        return limpio;
    }

    public int getId() { return id; }
    public String getNit() { return nit; }
    public String getNombre() { return nombre; }
    public String getContacto() { return contacto; }
    public String getTelefono() { return telefono; }
    public String getCorreo() { return correo; }
    public String getDireccion() { return direccion; }
    public boolean isActivo() { return activo; }
}
