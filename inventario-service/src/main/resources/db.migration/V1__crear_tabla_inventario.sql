-- V1__crear_tabla_inventarios.sql
 inventarios (
     id BIGINT AUTO_INCREMENT PRIMARY KEY,
     producto_id BIGINT NOT NULL UNIQUE,
     cantidad INT NOT NULL
);