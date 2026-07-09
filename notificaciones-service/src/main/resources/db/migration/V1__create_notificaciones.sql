-- V1__crear_tabla_notificaciones.sql
 notificaciones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_destino VARCHAR(255) NOT NULL,
    mensaje VARCHAR(500) NOT NULL,
    tipo_notificacion VARCHAR(50) NOT NULL
);