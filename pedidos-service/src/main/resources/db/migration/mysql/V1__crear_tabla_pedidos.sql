-- V1__crear_tabla_pedidos.sql
CREATE TABLE IF NOT EXISTS pedidos (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT       NOT NULL,
    total      DOUBLE,
    fecha      TIMESTAMP    NOT NULL,
    estado     VARCHAR(50)  NOT NULL
);
