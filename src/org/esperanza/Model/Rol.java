package org.esperanza.Model;

/**
 * Roles del sistema y permisos asociados.
 */
public enum Rol {

    ADMIN(
            "Administrador",
            new String[]{
                "GESTION_USUARIOS",
                "VER_REPORTES",
                "GESTION_INVENTARIO",
                "VENTAS",
                "CONFIGURACION",
                "DASHBOARD_ADMIN"
            }
    ),

    CAJERO(
            "Cajero",
            new String[]{
                "VENTAS",
                "CONSULTAR_PRODUCTOS",
                "DASHBOARD_CAJERO"
            }
    ),

    BODEGA(
            "Bodega",
            new String[]{
                "GESTION_INVENTARIO",
                "CONSULTAR_PRODUCTOS",
                "ENTRADAS_SALIDAS",
                "DASHBOARD_BODEGA"
            }
    );

    private final String nombreVisible;
    private final String[] permisos;

    Rol(String nombreVisible, String[] permisos) {
        this.nombreVisible = nombreVisible;
        this.permisos = permisos;
    }

    public String getNombreVisible() {
        return nombreVisible;
    }

    public String[] getPermisos() {
        return permisos.clone();
    }

    public boolean tienePermiso(String permiso) {

        if (permiso == null) {
            return false;
        }

        for (String permisoRol : permisos) {

            if (permisoRol.equalsIgnoreCase(permiso)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Convierte el rol almacenado en la base de datos
     * al enum correspondiente.
     */
    public static Rol fromString(String valor) {

        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }

        String rol = valor.trim().toUpperCase();

        switch (rol) {

            case "ADMIN":
            case "ADMINISTRADOR":
                return ADMIN;

            case "CAJERO":
                return CAJERO;

            case "BODEGA":
            case "BODEGUERO":
                return BODEGA;

            default:
                return null;
        }
    }
}