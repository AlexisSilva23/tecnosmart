-- V2__crear_tabla_pedido_items.sql
CREATE TABLE IF NOT EXISTS pedido_items (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id        BIGINT  NOT NULL,
    producto_id      BIGINT  NOT NULL,
    cantidad         INT     NOT NULL,
    precio_unitario  DOUBLE PRECISION,
    FOREIGN KEY (pedido_id) REFERENCES pedidos(id)
);
