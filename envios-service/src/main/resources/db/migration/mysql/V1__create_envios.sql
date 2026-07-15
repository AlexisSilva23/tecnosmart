-- V1__crear_tabla_envios.sql
CREATE TABLE IF NOT EXISTS envios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    destinatario VARCHAR(255) NOT NULL,
    direccion_destino VARCHAR(255) NOT NULL,
    empresa_transporte VARCHAR(100) NOT NULL,
    estado VARCHAR(50) NOT NULL
);
