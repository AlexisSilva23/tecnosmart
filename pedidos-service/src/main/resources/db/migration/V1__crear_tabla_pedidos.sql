-- V1__crear_tabla_pedidos.sql
 IF NOT EXISTS pedidos (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT       NOT NULL,
    total      DOUBLE,
    fecha      TIMESTAMP    NOT NULL,
    estado     VARCHAR(50)  NOT NULL
    );

-- V2__crear_tabla_pedido_items.sql
 IF NOT EXISTS pedido_items (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id        BIGINT  NOT NULL,
    producto_id      BIGINT  NOT NULL,
    cantidad         INT     NOT NULL,
    precio_unitario  DOUBLE,
    FOREIGN KEY (pedido_id) REFERENCES pedidos(id)
    );