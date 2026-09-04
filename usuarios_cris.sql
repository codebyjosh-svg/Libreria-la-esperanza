-- =====================================================
-- Librería La Esperanza - SQL CORREGIDO
-- Usuario: cris | Contraseña: cris123
-- Autor: mi nombre
-- =====================================================

CREATE DATABASE IF NOT EXISTS libreria_esperanza
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE libreria_esperanza;

-- Tabla usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(64)  NOT NULL,
    rol         VARCHAR(20)  NOT NULL,
    nombre      VARCHAR(80)  NOT NULL,
    apellido    VARCHAR(80)  NOT NULL,
    correo      VARCHAR(120) NULL,
    activo      TINYINT(1)   NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- Auditoría de uso y cambios por usuario
CREATE TABLE IF NOT EXISTS auditoria_uso (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    tipo VARCHAR(40) NOT NULL,
    detalle VARCHAR(500) NULL,
    inicio DATETIME NOT NULL,
    fin DATETIME NULL,
    duracion_segundos BIGINT NOT NULL DEFAULT 0,
    INDEX idx_auditoria_usuario (usuario_id),
    INDEX idx_auditoria_inicio (inicio),
    CONSTRAINT fk_auditoria_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- Procedimientos
DROP PROCEDURE IF EXISTS sp_iniciar_sesion;
DROP PROCEDURE IF EXISTS sp_buscar_usuario;
DROP PROCEDURE IF EXISTS sp_registrar_usuario;

DELIMITER $$

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
END$$

DELIMITER ;

-- Usuario: cris | Contraseña: cris123 | Rol: ADMIN
DELETE FROM usuarios WHERE username = 'cris';

INSERT INTO usuarios (username, password, rol, nombre, apellido, correo, activo)
VALUES (
    'cris',
    '111b1aa631daa820ee51ca710a672abaf4ab7c067f755f3eecae342e9b2c5c64',
    'ADMIN',
    'Cris',
    'Usuario',
    'cris@libreria.com',
    1
);

-- Verificar
SELECT id, username, rol, nombre, apellido, activo FROM usuarios WHERE username = 'cris';
