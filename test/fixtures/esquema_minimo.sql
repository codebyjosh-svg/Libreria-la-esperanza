-- Fixture MySQL 8 para pruebas locales en un esquema VACIO y desechable.
-- No es una migracion de produccion ni una copia del esquema original.
-- El ejecutor selecciona la base de pruebas; este archivo no selecciona ni borra bases.
-- Despues aplicar las migraciones sql de proveedores/devoluciones.
-- No contiene usuarios, contrasenas, ventas ni datos personales.

CREATE TABLE usuarios (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash CHAR(64) NOT NULL,
    rol VARCHAR(20) NOT NULL,
    nombre VARCHAR(80) NOT NULL,
    apellido VARCHAR(80) NOT NULL,
    correo VARCHAR(120),
    activo BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE clientes (
    cui BIGINT NOT NULL PRIMARY KEY,
    nombre_cliente VARCHAR(80) NOT NULL,
    apellido_cliente VARCHAR(80) NOT NULL,
    correo_electronico VARCHAR(120)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE libros (
    isbn VARCHAR(255) NOT NULL PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    fecha_publicacion DATE,
    precio DECIMAL(12,2) NOT NULL,
    id_categoria INT,
    nit_editorial VARCHAR(30),
    id_proveedor INT,
    stock_actual INT NOT NULL DEFAULT 0,
    stock_minimo INT NOT NULL DEFAULT 0,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_libros_stock_fixture CHECK (stock_actual >= 0),
    CONSTRAINT chk_libros_minimo_fixture CHECK (stock_minimo >= 0),
    CONSTRAINT chk_libros_precio_fixture CHECK (precio >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE ventas (
    id_venta INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    fecha_venta DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    subtotal DECIMAL(12,2) NOT NULL,
    descuento DECIMAL(12,2) NOT NULL DEFAULT 0,
    total DECIMAL(12,2) NOT NULL,
    estado ENUM('COMPLETADA', 'ANULADA') NOT NULL DEFAULT 'COMPLETADA',
    cui_cliente BIGINT NOT NULL,
    id_usuario INT NOT NULL,
    INDEX idx_ventas_fecha_fixture (fecha_venta),
    CONSTRAINT fk_ventas_cliente_fixture FOREIGN KEY (cui_cliente) REFERENCES clientes(cui),
    CONSTRAINT fk_ventas_usuario_fixture FOREIGN KEY (id_usuario) REFERENCES usuarios(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE detalle_venta (
    id_detalle INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_venta INT NOT NULL,
    isbn VARCHAR(255) NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(12,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    CONSTRAINT chk_detalle_cantidad_fixture CHECK (cantidad > 0),
    CONSTRAINT fk_detalle_venta_fixture FOREIGN KEY (id_venta) REFERENCES ventas(id_venta),
    CONSTRAINT fk_detalle_libro_fixture FOREIGN KEY (isbn) REFERENCES libros(isbn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE movimientos_inventario (
    id_movimiento BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    isbn VARCHAR(255) NOT NULL,
    tipo_movimiento ENUM('INGRESO', 'SALIDA') NOT NULL,
    cantidad INT NOT NULL,
    id_usuario INT NOT NULL,
    observacion VARCHAR(500),
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_mov_cantidad_fixture CHECK (cantidad > 0),
    CONSTRAINT fk_mov_libro_fixture FOREIGN KEY (isbn) REFERENCES libros(isbn),
    CONSTRAINT fk_mov_usuario_fixture FOREIGN KEY (id_usuario) REFERENCES usuarios(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE movimientos_salida_inventario (
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

DELIMITER $$

CREATE PROCEDURE sp_iniciar_sesion(IN p_username VARCHAR(50), IN p_password_hash CHAR(64))
BEGIN
    SELECT id, username, rol, nombre, apellido, correo, activo FROM usuarios
    WHERE username = p_username AND password_hash = p_password_hash AND activo = TRUE;
END$$

CREATE PROCEDURE sp_buscar_usuario(IN p_username VARCHAR(50))
BEGIN
    SELECT id, username, rol, nombre, apellido, correo, activo FROM usuarios WHERE username = p_username;
END$$

CREATE PROCEDURE sp_registrar_usuario(
    IN p_username VARCHAR(50), IN p_password_hash CHAR(64), IN p_rol VARCHAR(20),
    IN p_nombre VARCHAR(80), IN p_apellido VARCHAR(80), IN p_correo VARCHAR(120))
BEGIN
    INSERT INTO usuarios (username, password_hash, rol, nombre, apellido, correo, activo)
    VALUES (p_username, p_password_hash, p_rol, p_nombre, p_apellido, p_correo, TRUE);
END$$

CREATE PROCEDURE sp_listarlibros()
BEGIN
    SELECT * FROM libros ORDER BY titulo;
END$$

CREATE PROCEDURE sp_buscarlibro(IN p_isbn VARCHAR(255))
BEGIN
    SELECT * FROM libros WHERE isbn = p_isbn;
END$$

CREATE PROCEDURE sp_insertarlibro(
    IN p_isbn VARCHAR(255), IN p_titulo VARCHAR(255), IN p_fecha DATE,
    IN p_precio DECIMAL(12,2), IN p_categoria INT, IN p_editorial VARCHAR(30),
    IN p_proveedor INT, IN p_stock INT, IN p_minimo INT)
BEGIN
    INSERT INTO libros (isbn, titulo, fecha_publicacion, precio, id_categoria,
        nit_editorial, id_proveedor, stock_actual, stock_minimo, activo)
    VALUES (p_isbn, p_titulo, p_fecha, p_precio, p_categoria, p_editorial,
        p_proveedor, p_stock, p_minimo, TRUE);
END$$

CREATE PROCEDURE sp_actualizarlibro(
    IN p_isbn VARCHAR(255), IN p_titulo VARCHAR(255), IN p_fecha DATE,
    IN p_precio DECIMAL(12,2), IN p_categoria INT, IN p_editorial VARCHAR(30),
    IN p_proveedor INT, IN p_minimo INT)
BEGIN
    UPDATE libros SET titulo = p_titulo, fecha_publicacion = p_fecha, precio = p_precio,
        id_categoria = p_categoria, nit_editorial = p_editorial, id_proveedor = p_proveedor,
        stock_minimo = p_minimo WHERE isbn = p_isbn;
END$$

CREATE PROCEDURE sp_eliminarlibro(IN p_isbn VARCHAR(255))
BEGIN
    UPDATE libros SET activo = FALSE WHERE isbn = p_isbn;
END$$

DELIMITER ;
