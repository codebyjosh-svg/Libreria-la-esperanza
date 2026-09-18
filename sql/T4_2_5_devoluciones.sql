-- T4.2.5: ejecutar UNA VEZ en la base configurada, con copia de seguridad.
-- MySQL 8.0.16+; el ZIP no incluye el esquema base.
-- Se asumen ventas(id_venta, estado), detalle_venta(id_detalle, id_venta, isbn,
-- cantidad), libros(isbn UNIQUE o PRIMARY KEY, stock_actual INT).
-- Los identificadores de venta deben caber en un INT positivo, como usa Java.
-- Realizar la migración durante mantenimiento: ALTER TABLE hace commit implícito.

-- Revisión previa: detener y corregir si hay estados NULL o motores distintos de InnoDB.
SELECT TABLE_NAME, ENGINE FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME IN ('ventas', 'detalle_venta', 'libros');
SELECT estado, COUNT(*) AS cantidad FROM ventas GROUP BY estado;

-- Conserva los textos de estados anteriores y permite DEVUELTA incluso si era ENUM.
-- La aplicación solo transforma COMPLETADA -> DEVUELTA; ANULADA y estados legados
-- no son elegibles. No se cambian fechas, totales ni detalles de ventas históricas.
ALTER TABLE ventas MODIFY COLUMN estado VARCHAR(30) NOT NULL DEFAULT 'COMPLETADA';

CREATE TABLE IF NOT EXISTS devoluciones_venta (
    id_venta INT NOT NULL PRIMARY KEY,
    motivo VARCHAR(500) NOT NULL,
    id_usuario INT NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_devolucion_motivo CHECK (CHAR_LENGTH(TRIM(motivo)) BETWEEN 5 AND 500),
    CONSTRAINT chk_devolucion_usuario CHECK (id_usuario > 0),
    INDEX idx_devolucion_fecha (fecha),
    INDEX idx_devolucion_usuario (id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS movimientos_devolucion_venta (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_venta INT NOT NULL,
    isbn VARCHAR(255) NOT NULL,
    cantidad INT NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_movimiento_devolucion UNIQUE (id_venta, isbn),
    CONSTRAINT chk_movimiento_devolucion_cantidad CHECK (cantidad > 0),
    CONSTRAINT fk_movimiento_devolucion FOREIGN KEY (id_venta)
        REFERENCES devoluciones_venta (id_venta) ON DELETE RESTRICT,
    INDEX idx_movimiento_devolucion_isbn (isbn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Sin FK hacia tablas del esquema base: sus tipos exactos no están en el ZIP.
-- El DAO comprueba venta y libro, y guarda auditoría, stock y estado en una transacción.
-- PK de auditoría y UNIQUE de movimientos evitan duplicados incluso con concurrencia.
-- Si una tabla base no es InnoDB, migrarla previamente durante mantenimiento:
-- ALTER TABLE ventas ENGINE=InnoDB;
-- ALTER TABLE detalle_venta ENGINE=InnoDB;
-- ALTER TABLE libros ENGINE=InnoDB;
-- Para bases grandes, evaluar un índice existente sobre ventas(fecha_venta,id_venta)
-- antes de crear otro. El DAO rechaza el uso si falta una tabla o el motor no es InnoDB.

SELECT TABLE_NAME, ENGINE FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME IN
    ('ventas', 'detalle_venta', 'libros', 'devoluciones_venta', 'movimientos_devolucion_venta');
