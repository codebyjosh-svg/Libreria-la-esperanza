-- =====================================================
-- Librería La Esperanza - Script de usuarios de prueba
-- Autor: mi nombre
-- =====================================================

-- 1) Tabla usuarios (si aún no la tienes)
CREATE TABLE IF NOT EXISTS usuarios (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(64)  NOT NULL,          -- SHA-256 (64 caracteres hex)
    rol         VARCHAR(20)  NOT NULL,          -- ADMIN | CAJERO | BODEGA
    nombre      VARCHAR(80)  NOT NULL,
    apellido    VARCHAR(80)  NOT NULL,
    correo      VARCHAR(120),
    activo      TINYINT(1)   NOT NULL DEFAULT 1
);

-- 2) Procedimientos que usa el código Java

DELIMITER //

DROP PROCEDURE IF EXISTS sp_registrar_usuario //
CREATE PROCEDURE sp_registrar_usuario(
    IN p_username VARCHAR(50),
    IN p_password_hash VARCHAR(64),
    IN p_rol VARCHAR(20),
    IN p_nombre VARCHAR(80),
    IN p_apellido VARCHAR(80),
    IN p_correo VARCHAR(120)
)
BEGIN
    INSERT INTO usuarios (username, password, rol, nombre, apellido, correo, activo)
    VALUES (p_username, p_password_hash, p_rol, p_nombre, p_apellido, p_correo, 1);
END //

DELIMITER ;

-- 3) Usuarios de prueba (contraseñas en texto plano → hash SHA-256)

-- Usuario: admin   | Contraseña: admin123  | Rol: ADMIN
INSERT INTO usuarios (username, password, rol, nombre, apellido, correo, activo)
VALUES (
    'admin',
    '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
    'ADMIN',
    'Administrador',
    'Sistema',
    'admin@libreria.com',
    1
);

-- Usuario: cajero  | Contraseña: cajero123 | Rol: CAJERO
INSERT INTO usuarios (username, password, rol, nombre, apellido, correo, activo)
VALUES (
    'cajero',
    '1ed4353e845e2e537e017c0fac3a0d402d231809b7989e90da15191c1148a93f',
    'CAJERO',
    'Juan',
    'Pérez',
    'cajero@libreria.com',
    1
);

-- Usuario: bodega  | Contraseña: bodega123 | Rol: BODEGA
INSERT INTO usuarios (username, password, rol, nombre, apellido, correo, activo)
VALUES (
    'bodega',
    '3e2388e8ceddc313076daab3e4eb98a3feb2c0da2464e9c632eff130483208eb',
    'BODEGA',
    'María',
    'López',
    'bodega@libreria.com',
    1
);
