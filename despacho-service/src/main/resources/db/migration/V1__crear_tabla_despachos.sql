-- V1__crear_tabla_despachos.sql
 IF NOT EXISTS despachos (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id        BIGINT       NOT NULL,
    direccion        VARCHAR(255) NOT NULL,
    comuna           VARCHAR(100) NOT NULL,
    estado           VARCHAR(50)  NOT NULL,
    fecha_programada DATE
);