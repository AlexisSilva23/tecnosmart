-- V1__crear_tabla_favoritos.sql
CREATE TABLE IF NOT EXISTS favoritos (
   id BIGINT AUTO_INCREMENT PRIMARY KEY,
   usuario VARCHAR(255) NOT NULL,
   tipo_item VARCHAR(100) NOT NULL,
   nombre_item VARCHAR(255) NOT NULL
);
