package org.esperanza.Model;

public record SalidaInventario(String isbn, TipoSalida tipo, int cantidad,
                               String observacion, int usuarioId) {
    public SalidaInventario {
        if (isbn == null || isbn.isBlank() || isbn.trim().length() > 255) {
            throw new IllegalArgumentException("Ingresa un ISBN válido (máximo 255 caracteres).");
        }
        if (tipo == null) throw new IllegalArgumentException("Selecciona el tipo de salida.");
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        if (observacion == null || observacion.isBlank() || observacion.trim().length() > 500) {
            throw new IllegalArgumentException("Ingresa un motivo de entre 1 y 500 caracteres.");
        }
        if (usuarioId <= 0) throw new IllegalArgumentException("Se requiere un usuario válido.");
        isbn = isbn.trim();
        observacion = observacion.trim();
    }
}
