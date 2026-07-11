# TecnoSmart

E-commerce basado en arquitectura de microservicios, desarrollado como proyecto académico. Implementa registro de servicio (Eureka), API Gateway, persistencia con migraciones versionadas (Flyway), comunicación entre servicios (Feign) y APIs auto-descriptivas (HATEOAS).

## Autores

- Alexis Silva
- Felipe Yefi

## Arquitectura

El sistema está compuesto por 12 microservicios independientes, cada uno con su propia base de datos, orquestados mediante Docker Compose en desarrollo y desplegados como servicios independientes en Render.

```
                         ┌─────────────────┐
                         │  Eureka Server   │  :8761
                         │ (Service Registry)│
                         └────────▲─────────┘
                                  │
                         ┌────────┴─────────┐
                         │   API Gateway     │  :8080
                         └────────▲─────────┘
                                  │
        ┌──────────┬──────────┬──┴───────┬──────────┬──────────┐
        │          │          │          │          │          │
   usuario-service │   inventario   carrito    pedidos      pagos ...
      :8081    catalogo:8082  :8083     :8084      :8085    :8086
```

## Microservicios

| Servicio | Puerto | Responsabilidad | Autor |
|---|---|---|---|
| eureka-server | 8761 | Registro y descubrimiento de servicios | Propio |
| api-gateway | 8080 | Punto de entrada único, enrutamiento | Propio |
| usuario-service | 8081 | Gestión de usuarios | Propio |
| catalogo-service | 8082 | Categorías y productos | Propio |
| inventario-service | 8083 | Stock de productos | Propio |
| carrito-service | 8084 | Carrito de compras | Propio |
| pedidos-service | 8085 | Órdenes de compra | Propio |
| pagos-service | 8086 | Procesamiento de pagos | Propio |
| despacho-service | 8087 | Gestión de despachos | Propio |
| envios-service | 8088 | Seguimiento de envíos | Compañero |
| favoritos-service | 8089 | Lista de favoritos | Compañero |
| notificaciones-service | 8090 | Notificaciones a usuarios | Compañero |

> **Nota:** el nombre del servicio `usuario-service` en `docker-compose.yml` no lleva "s", aunque la carpeta del proyecto sí se llama `usuarios-service`.

## Stack tecnológico

- **Lenguaje:** Java 21
- **Framework:** Spring Boot 4.0.7
- **Gestión de dependencias:** Maven (multi-módulo)
- **Descubrimiento de servicios:** Netflix Eureka
- **Comunicación entre servicios:** OpenFeign
- **Persistencia:** Spring Data JPA + Hibernate
- **Migraciones de base de datos:** Flyway
- **Base de datos local:** MySQL 8.4 (vía Laragon)
- **Base de datos producción:** PostgreSQL (Render)
- **Datos de prueba:** Datafaker
- **APIs auto-descriptivas:** Spring HATEOAS
- **Documentación de API:** springdoc-openapi / Swagger UI
- **Contenedores:** Docker & Docker Compose
- **Despliegue:** Render (Blueprint / Infrastructure as Code)

## Requisitos previos

- Java 21 (JDK)
- Maven (o usar el wrapper `mvnw` incluido)
- Docker Desktop
- MySQL local (por ejemplo vía [Laragon](https://laragon.org/)) con las 10 bases de datos creadas (ver sección Bases de datos)

## Cómo levantar el proyecto en local

1. Clona el repositorio:
   ```powershell
   git clone https://github.com/AlexisSilva23/tecnosmart.git
   cd tecnosmart
   ```

2. Asegúrate de tener MySQL corriendo localmente (Laragon) con las 10 bases de datos creadas (ver más abajo).

3. Compila todos los módulos:
   ```powershell
   .\mvnw.cmd clean package -DskipTests
   ```

4. Levanta todos los servicios con Docker Compose:
   ```powershell
   docker compose up --build -d
   ```

5. Verifica que los 12 servicios estén registrados en Eureka:
   ```
   http://localhost:8761
   ```

6. Todo el tráfico hacia los microservicios pasa por el API Gateway:
   ```
   http://localhost:8080
   ```

## Bases de datos

### Desarrollo local (MySQL)

Cada microservicio con persistencia usa su propia base de datos. Debes crear las 10 bases antes de levantar el proyecto:

```
db_usuarios, db_catalogo, db_inventario, db_carrito, db_pedidos,
db_pagos, db_despacho, db_envios, db_favoritos, db_notificaciones
```

Las tablas se crean automáticamente al arrancar cada servicio mediante **Flyway**, que ejecuta los scripts versionados ubicados en `src/main/resources/db/migration/` de cada módulo. No es necesario crear las tablas a mano.

### Producción (PostgreSQL en Render)

En producción se usa una base PostgreSQL administrada por Render (`tecnosmart-db`), ya que Render no ofrece MySQL como servicio nativo. El mismo mecanismo de Flyway se encarga de crear el esquema.

### Perfiles de configuración

Cada microservicio con base de datos define su `application.yml`/`.yaml` en tres bloques separados por `---`:

- **General:** puerto, configuración de Eureka, Swagger.
- **`local`:** conexión a MySQL (Laragon).
- **`prod`:** conexión a PostgreSQL (Render).

El perfil activo se controla con la variable de entorno `SPRING_PROFILES_ACTIVE`.

## Documentación de la API (Swagger)

Cada microservicio expone su propia documentación OpenAPI/Swagger de forma local en:

```
http://localhost:<puerto>/swagger-ui.html
```

Por ejemplo, para `usuario-service`:
```
http://localhost:8081/swagger-ui.html
```

## Flujo de compra de ejemplo

El flujo principal de negocio, probado de extremo a extremo a través del API Gateway (`http://localhost:8080`), es:

1. **Crear/consultar usuario** → `usuario-service`
2. **Consultar productos del catálogo** → `catalogo-service`
3. **Agregar productos al carrito** → `carrito-service`
4. **Generar el pedido** → `pedidos-service`, que consulta el precio real de cada producto vía Feign contra `catalogo-service` para calcular `precioUnitario` y `total` de forma consistente.

## Despliegue en Render

El despliegue de los 12 servicios está definido como código en [`render.yaml`](./render.yaml), usando el modelo de Blueprints de Render (Infrastructure as Code).

Para desplegar:

1. En el [dashboard de Render](https://dashboard.render.com), selecciona **New → Blueprint** y conecta este repositorio.
2. Render detectará el `render.yaml` y mostrará la lista de los 12 servicios a crear.
3. Durante la creación, Render pedirá el valor de las variables marcadas con `sync: false` (por ejemplo `DB_PASSWORD`) — estas **no** están hardcodeadas en el repositorio por seguridad, y deben ingresarse manualmente en el dashboard.
4. Una vez desplegado el `eureka-server`, actualiza la variable `EUREKA_URL` en los demás servicios con la URL pública real asignada por Render, si difiere de la definida por defecto.

## Estructura del repositorio

```
tecnosmart/
├── eureka-server/
├── api-gateway/
├── usuarios-service/
├── catalogo-service/
├── inventario-service/
├── carrito-service/
├── pedidos-service/
├── pagos-service/
├── despacho-service/
├── envios-service/
├── favoritos-service/
├── notificaciones-service/
├── docker-compose.yml
├── render.yaml
└── README.md
```

Cada módulo sigue la estructura estándar de Maven/Spring Boot, con sus migraciones de Flyway en `src/main/resources/db/migration/`.

## Notas de seguridad

Las credenciales de bases de datos de producción no se almacenan en el repositorio. Las variables sensibles (`DB_PASSWORD`) se configuran directamente en el dashboard de Render mediante `sync: false` en el Blueprint.