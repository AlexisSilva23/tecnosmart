-- V1__crear_tabla_pagos.sql
 IF NOT EXISTS pagos (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id  BIGINT       NOT NULL,
    metodo     VARCHAR(100) NOT NULL,
    monto      INT          NOT NULL,
    estado     VARCHAR(50)  NOT NULL,
    fecha_pago TIMESTAMP
    );