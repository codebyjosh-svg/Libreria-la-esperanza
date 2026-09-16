-- Ejecutar una vez en la misma base MySQL configurada en src/db.properties.
-- libros debe usar InnoDB. Comprobar antes de utilizar el formulario:
SELECT TABLE_NAME, ENGINE FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'libros';
-- Si no usa InnoDB, migrarla durante mantenimiento: ALTER TABLE libros ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS movimientos_salida_inventario (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    isbn VARCHAR(255) NOT NULL,
    tipo_salida ENUM('MERMA', 'TRASLADO', 'DEVOLUCION_PROVEEDOR') NOT NULL,
    cantidad INT NOT NULL,
    observacion VARCHAR(500) NOT NULL,
    usuario_id INT NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_salida_cantidad CHECK (cantidad > 0),
    INDEX idx_salida_isbn_fecha (isbn, fecha),
    INDEX idx_salida_usuario (usuario_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
