-- T4.4.6: ejecutar en la base configurada en db.properties, antes de abrir Proveedores.
-- Es una migración adicional; no elimina ni modifica proveedores existentes.
CREATE TABLE IF NOT EXISTS proveedores (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nit VARCHAR(25) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    contacto VARCHAR(100) NOT NULL DEFAULT '',
    telefono VARCHAR(25) NOT NULL,
    correo VARCHAR(150) NOT NULL DEFAULT '',
    direccion VARCHAR(250) NOT NULL DEFAULT '',
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_proveedores_nit UNIQUE (nit),
    CONSTRAINT chk_proveedores_nombre CHECK (CHAR_LENGTH(TRIM(nombre)) >= 2),
    CONSTRAINT chk_proveedores_nit CHECK (CHAR_LENGTH(nit) BETWEEN 3 AND 25),
    CONSTRAINT chk_proveedores_telefono CHECK (CHAR_LENGTH(TRIM(telefono)) >= 7),
    CONSTRAINT chk_proveedores_activo CHECK (activo IN (0, 1)),
    INDEX idx_proveedores_activo_nombre (activo, nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- El NIT se guarda en mayúsculas, sin espacios ni guiones; también es único en las bajas.
-- La aplicación hace UPDATE activo = FALSE para eliminar y permite reactivar el mismo ID.
-- No se asume una relación con libros/compras ausente del proyecto original.
