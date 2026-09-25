package org.esperanza.Model;

import java.math.BigDecimal;
import java.util.Objects;

/** Resumen del catálogo, las ventas completadas y los usuarios activos. */
public final class IndicadoresDashboardAdmin {

    private final BigDecimal ventasTotales;
    private final long cantidadLibros;
    private final long usuariosActivos;

    public IndicadoresDashboardAdmin(BigDecimal ventasTotales,
                                     long cantidadLibros,
                                     long usuariosActivos) {
        this.ventasTotales = Objects.requireNonNull(ventasTotales, "ventasTotales");
        if (cantidadLibros < 0 || usuariosActivos < 0) {
            throw new IllegalArgumentException("Los indicadores no pueden ser negativos");
        }
        this.cantidadLibros = cantidadLibros;
        this.usuariosActivos = usuariosActivos;
    }

    public BigDecimal getVentasTotales() {
        return ventasTotales;
    }

    public long getCantidadLibros() {
        return cantidadLibros;
    }

    public long getUsuariosActivos() {
        return usuariosActivos;
    }
}
