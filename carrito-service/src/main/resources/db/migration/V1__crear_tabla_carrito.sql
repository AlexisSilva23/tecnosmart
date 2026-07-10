-- V1__crear_tabla_carrito_items.sql
CREATE TABLE IF NOT EXISTS carrito_items (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT      NOT NULL,
    producto_id BIGINT     NOT NULL,
    cantidad   INT         NOT NULL
);
