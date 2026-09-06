package org.esperanza.Model;

public enum Rol {

    ADMIN(
            "Administrador",
            new String[]{
                "GESTION_USUARIOS",
                "VER_REPORTES",
                "GESTION_INVENTARIO",
                "VENTAS",
                "CONFIGURACION",
                "CONSULTAR_PRODUCTOS",
                "ENTRADAS_SALIDAS"
            }
    ),

    CAJERO(
            "Cajero",
            new String[]{
                "VENTAS",
                "CONSULTAR_PRODUCTOS"
            }
    ),

    BODEGA(
            "Bodega",
            new String[]{
                "GESTION_INVENTARIO",
                "CONSULTAR_PRODUCTOS",
                "ENTRADAS_SALIDAS"
            }
    );

    private final String nombreVisible;
    private final String[] permisos;

    Rol(
            String nombreVisible,
            String[] permisos) {

        this.nombreVisible =
                nombreVisible;

        this.permisos =
                permisos;
    }

    public String getNombreVisible() {

        return nombreVisible;
    }

    public String[] getPermisos() {

        return permisos.clone();
    }

    public boolean tienePermiso(
            String permiso) {

        if (permiso == null) {
            return false;
        }

        for (String permisoRol
                : permisos) {

            if (permisoRol
                    .equalsIgnoreCase(
                            permiso
                    )) {

                return true;
            }
        }

        return false;
    }

    public static Rol fromString(
            String valor) {

        if (valor == null
                || valor
                        .trim()
                        .isEmpty()) {

            return null;
        }

        String normalizado =
                valor
                        .trim()
                        .toUpperCase();

        return switch (normalizado) {

            case "ADMIN",
                 "ADMINISTRADOR" ->
                    ADMIN;

            case "CAJERO" ->
                    CAJERO;

            case "BODEGA",
                 "BODEGUERO" ->
                    BODEGA;

            default ->
                    null;
        };
    }
}