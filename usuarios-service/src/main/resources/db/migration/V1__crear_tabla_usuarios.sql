-- V1__crear_tabla_usuarios.sql
CREATE TABLE IF NOT EXISTS usuarios (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre    VARCHAR(100) NOT NULL,
    email     VARCHAR(100) NOT NULL UNIQUE,
    password  VARCHAR(255) NOT NULL,
    direccion VARCHAR(255)
);
