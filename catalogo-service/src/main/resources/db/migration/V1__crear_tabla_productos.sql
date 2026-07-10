-- V1__crear_tabla_categorias.sql
CREATE TABLE IF NOT EXISTS categorias (
                                          id          BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          nombre      VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(255) NOT NULL UNIQUE
    );

-- V2__crear_tabla_productos.sql
CREATE TABLE IF NOT EXISTS productos (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre       VARCHAR(255) NOT NULL,
    descripcion  VARCHAR(255),
    precio       DOUBLE       NOT NULL,
    categoria_id BIGINT       NOT NULL,
    FOREIGN KEY (categoria_id) REFERENCES categorias(id)
    );