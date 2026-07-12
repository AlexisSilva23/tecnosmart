# TecnoSmart

E-commerce basado en arquitectura de microservicios, desarrollado como proyecto académico. Implementa registro de servicio (Eureka), API Gateway, persistencia con migraciones versionadas (Flyway), comunicación entre servicios (Feign), datos de prueba generados (Datafaker) y APIs auto-descriptivas (HATEOAS).

## Autores

- Alexis Silva
- Felipe Yefi

## Arquitectura

El sistema está compuesto por 12 microservicios independientes, cada uno con su propia base de datos (o su propio *schema* aislado en producción), orquestados mediante Docker Compose en desarrollo y desplegados como servicios web independientes en Render.

```
                         ┌──────────────────┐
                         │  Eureka Server    │  :8761
                         │ (Service Registry)│
                         └────────▲──────────┘
                                  │
                         ┌────────┴──────────┐
                         │   API Gateway      │  :8080
                         └────────▲──────────┘
                                  │
   ┌───────────┬────────────┬────┴───────┬────────────┬────────────┐
   │           │            │            │            │            │
usuario   catalogo    inventario     carrito      pedidos       pagos ...
:8081      :8082         :8083        :8084         :8085        :8086
```

## Microservicios

| Servicio | Puerto | Responsabilidad | Autor  | Datafaker + HATEOAS |
|---|---|---|--------|---|
| eureka-server | 8761 | Registro y descubrimiento de servicios | Ambos  | N/A |
| api-gateway | 8080 | Punto de entrada único, enrutamiento | Ambos  | N/A |
| usuario-service | 8081 | Gestión de usuarios | Alexis | ✅ |
| catalogo-service | 8082 | Categorías y productos | Alexis | ✅ |
| inventario-service | 8083 | Stock de productos | Alexis | ✅ |
| carrito-service | 8084 | Carrito de compras | Alexis | ✅ |
| pedidos-service | 8085 | Órdenes de compra | Alexis | ✅ |
| pagos-service | 8086 | Procesamiento de pagos | Felipe | ✅ |
| despacho-service | 8087 | Gestión de despachos | Felipe | ✅ |
| envios-service | 8088 | Seguimiento de envíos | Felipe | ✅ |
| favoritos-service | 8089 | Lista de favoritos | Felipe | ✅ |
| notificaciones-service | 8090 | Notificaciones a usuarios | Felipe | ✅ |

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
- **Base de datos producción:** PostgreSQL administrado por Render
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

### Orden de arranque

Docker Compose respeta las dependencias declaradas (`depends_on`), pero en la práctica conviene tener en cuenta este orden lógico al levantar o depurar servicios de forma individual:

1. **`eureka-server`** — debe estar arriba antes que cualquier otro servicio, ya que todos se registran contra él.
2. **`api-gateway`** — depende de Eureka para poder enrutar hacia los demás servicios.
3. **Microservicios de negocio** (usuario, catalogo, inventario, carrito, pedidos, pagos, despacho, envios, favoritos, notificaciones) — pueden levantarse en cualquier orden entre sí, pero cada uno necesita que Eureka ya esté disponible para registrarse correctamente. Si alguno arranca antes que Eureka, reintentará el registro automáticamente hasta lograrlo (no es necesario reiniciarlo a mano).

Con `docker compose up -d` todos arrancan en paralelo y cada uno reintenta su conexión a Eureka hasta lograrlo, así que normalmente no hace falta levantar uno por uno.

## Bases de datos

### Desarrollo local (MySQL)

Cada microservicio con persistencia usa su propia base de datos. Debes crear las 10 bases antes de levantar el proyecto:

```
db_usuarios, db_catalogo, db_inventario, db_carrito, db_pedidos,
db_pagos, db_despacho, db_envios, db_favoritos, db_notificaciones
```

Las tablas se crean automáticamente al arrancar cada servicio mediante **Flyway**, que ejecuta los scripts versionados ubicados en `src/main/resources/db/migration/` de cada módulo. No es necesario crear las tablas a mano.

### Producción (PostgreSQL en Render)

En producción se usa una única base PostgreSQL administrada por Render (`tecnosmart-db`), ya que Render no ofrece MySQL como servicio nativo. Como todos los microservicios comparten la misma base física, **cada uno usa su propio *schema* de PostgreSQL** (equivalente a las 10 bases separadas de MySQL en local) para evitar colisiones entre las tablas de historial de Flyway y los nombres de tabla:

| Microservicio | Schema en PostgreSQL |
|---|---|
| usuarios-service | `usuarios` |
| catalogo-service | `catalogo` |
| inventario-service | `inventario` |
| carrito-service | `carrito` |
| pedidos-service | `pedidos` |
| pagos-service | `pagos` |
| despacho-service | `despacho` |
| envios-service | `envios` |
| favoritos-service | `favoritos` |
| notificaciones-service | `notificaciones` |

Esto se logra agregando el parámetro `currentSchema=<nombre>` a la URL JDBC de cada servicio en `render.yaml`. Flyway crea el schema automáticamente si no existe.

### Perfiles de configuración

Cada microservicio con base de datos define su `application.yml`/`.yaml` en tres bloques separados por `---`:

- **General:** puerto, configuración de Eureka, Swagger.
- **`local`:** conexión a MySQL (Laragon).
- **`prod`:** conexión a PostgreSQL (Render), variables `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`.

El perfil activo se controla con la variable de entorno `SPRING_PROFILES_ACTIVE`.

## Despliegue en Render

El despliegue de los 12 servicios está definido como código en [`render.yaml`](./render.yaml), usando el modelo de Blueprints de Render (Infrastructure as Code).

Para desplegar desde cero:

1. En el [dashboard de Render](https://dashboard.render.com), selecciona **New → Blueprint** y conecta este repositorio.
2. Render detectará el `render.yaml` y mostrará la lista de los 12 servicios a crear.
3. Durante la creación, Render pedirá el valor de las variables marcadas con `sync: false` (por ejemplo `SPRING_DATASOURCE_PASSWORD`) — estas **no** están hardcodeadas en el repositorio por seguridad, y deben ingresarse manualmente en el dashboard para cada uno de los 10 servicios con base de datos.
4. Una vez desplegado `eureka-server`, confirma que la variable `EUREKA_URL` de los demás servicios apunte a la URL pública real asignada por Render.

> **Nota:** el plan gratuito de Render "duerme" los servicios tras un período de inactividad. La primera petición tras un tiempo sin uso puede tardar unos segundos más mientras el servicio despierta — esto es esperado y no indica una falla.

## Documentación de la API (Swagger)

Cada microservicio expone su propia documentación OpenAPI/Swagger.

**En local**, en `http://localhost:<puerto>/swagger-ui.html`:

| Servicio | URL local |
|---|---|
| usuario-service | http://localhost:8081/swagger-ui.html |
| catalogo-service | http://localhost:8082/swagger-ui.html |
| inventario-service | http://localhost:8083/swagger-ui.html |
| carrito-service | http://localhost:8084/swagger-ui.html |
| pedidos-service | http://localhost:8085/swagger-ui.html |
| pagos-service | http://localhost:8086/swagger-ui.html |
| despacho-service | http://localhost:8087/swagger-ui.html |
| envios-service | http://localhost:8088/swagger-ui.html |
| favoritos-service | http://localhost:8089/swagger-ui.html |
| notificaciones-service | http://localhost:8090/swagger-ui.html |

**En producción**, reemplaza `http://localhost:<puerto>` por la URL pública que Render asignó a cada servicio (formato `https://<nombre-servicio>.onrender.com/swagger-ui.html`).

---

## Cómo probar que todo funciona correctamente

### 1. Verificar que los 12 servicios están registrados en Eureka

**Local:** `http://localhost:8761`
**Producción:** `https://eureka-server.onrender.com`

Deberías ver los 12 servicios listados con estado `UP`. Si alguno falta, revisa sus logs (`docker compose logs <servicio>` en local, o la pestaña Logs del servicio en el dashboard de Render).

> Es normal ver ocasionalmente el mensaje *"EUREKA MAY BE INCORRECTLY CLAIMING INSTANCES ARE UP..."* — es una advertencia del modo de auto-preservación de Eureka, no un error, y se resuelve sola cuando los servicios estabilizan su ritmo de heartbeat.

### 2. Verificar que cada servicio generó sus datos de prueba

Revisa en los logs de arranque de cada microservicio que aparezca una línea de confirmación de Datafaker, por ejemplo:

```
Datafaker: 15 usuarios de prueba generados.
Datafaker: 6 categorías y 20 productos de prueba generados.
Datafaker: 15 items de carrito de prueba generados.
Datafaker: 10 pedidos de prueba generados.
Datafaker: 10 pagos de prueba generados.
```

Si algún servicio no muestra esta línea (o muestra un error de inserción, por ejemplo un `NOT NULL constraint` violado), revisa su clase `DataLoader` en `src/main/java/duoc/<servicio>/config/DataLoader.java`.

### 3. Probar cada microservicio individualmente en Swagger

Para cada servicio, abre su Swagger UI y prueba al menos:
- Un endpoint `GET` de listado (por ejemplo `GET /api/usuarios`) — debe devolver los registros generados por Datafaker, con enlaces HATEOAS (`_links`) en la respuesta.
- Un endpoint `GET` por ID — debe devolver el recurso individual, también con sus `_links`.

### 4. Probar el flujo de compra completo vía Postman (a través del API Gateway)

Este es el flujo principal de negocio del sistema. Todas las peticiones deben hacerse contra el **Gateway**, no directo a cada microservicio, para simular el comportamiento real:

**Base URL:**
- Local: `http://localhost:8080`
- Producción: `https://api-gateway.onrender.com` (o la URL real asignada por Render)

**Paso a paso:**

1. **Consultar usuarios existentes**
   ```
   GET {{base_url}}/usuarios
   ```
   Copia el `id` de un usuario para usarlo más adelante.

2. **Consultar el catálogo de productos**
   ```
   GET {{base_url}}/productos
   ```
   Copia el `id` de uno o dos productos.

3. **Agregar un producto al carrito**
   ```
   POST {{base_url}}/carrito
   Content-Type: application/json

   {
     "usuarioId": 1,
     "productoId": 3,
     "cantidad": 2
   }
   ```

4. **Consultar el carrito del usuario**
   ```
   GET {{base_url}}/carrito/usuario/1
   ```

5. **Generar un pedido**
   ```
   POST {{base_url}}/pedidos
   Content-Type: application/json

   {
     "usuarioId": 1,
     "items": [
       { "productoId": 3, "cantidad": 2 }
     ]
   }
   ```

6. **Verificación clave:** consulta el pedido recién creado
   ```
   GET {{base_url}}/pedidos/{id}
   ```
   Confirma que:
   - `precioUnitario` de cada item **no sea `null`** (se obtiene en tiempo real desde `catalogo-service` vía Feign).
   - `total` del pedido **no sea `null`** ni `0`, y corresponda a la suma de `cantidad × precioUnitario` de todos los items.

   Este paso valida específicamente la corrección del bug histórico donde `precioUnitario` y `total` quedaban en `null` por no consultar el precio real del producto al momento de generar el pedido.

### 5. Probar HATEOAS

En cualquier respuesta `GET` de un recurso individual (por ejemplo `GET /usuarios/1`), confirma que la respuesta JSON incluya un bloque `_links` con enlaces de navegación (`self`, y en los casos con relaciones cruzadas —como carrito→usuario o producto→categoría— también un enlace hacia el recurso relacionado).

### 6. Checklist de verificación final

- [ ] Los 12 servicios aparecen `UP` en Eureka
- [ ] Cada servicio con base de datos generó sus datos de prueba (revisar logs)
- [ ] Swagger UI carga correctamente para al menos 3-4 servicios de muestra
- [ ] El flujo completo de compra (usuario → producto → carrito → pedido) funciona de punta a punta vía el Gateway
- [ ] `total` y `precioUnitario` del pedido no son `null`
- [ ] Las respuestas de la API incluyen enlaces `_links` (HATEOAS)
- [ ] El mismo flujo funciona tanto en local (Docker Compose) como en producción (Render)

## Colección de Postman

El repositorio incluye [`TecnoSmart.postman_collection.json`](./TecnoSmart.postman_collection.json), lista para importar en Postman. Contiene:

- **Flujo de compra completo:** las 6 peticiones del flujo principal de negocio (usuario → producto → carrito → pedido), con tests automáticos que verifican que `total` y `precioUnitario` no sean `null`.
- **CRUD por microservicio:** una carpeta por cada uno de los 10 microservicios con base de datos, con peticiones `GET`, `POST` (crear) y `DELETE` (eliminar) de prueba.

### Cómo importarla y usarla

1. En Postman: **Import** → selecciona el archivo `TecnoSmart.postman_collection.json`.
2. Click en la colección → pestaña **Variables** → ajusta `base_url`:
   - Local: `http://localhost:8080`
   - Producción: la URL pública real del `api-gateway` en Render.
3. Todas las rutas del negocio pasan por el Gateway bajo el prefijo `/api/` (por ejemplo `{{base_url}}/api/usuarios`), configurado en `api-gateway/src/main/resources/application.yaml`.
4. Después de un `POST` que crea un recurso, copia el `id` devuelto en la respuesta y actualiza la variable correspondiente (`usuario_id`, `producto_id`, `pedido_id`, etc.) antes de usarlo en otras peticiones o en el `DELETE`.

### Nivel de confianza de las rutas y los DTOs

Las rutas base (`/api/<recurso>`) están confirmadas contra el `application.yaml` real del Gateway. Los cuerpos (`body`) de las peticiones `POST` están confirmados contra los `RequestDTO` reales para:

- `UsuarioRequestDTO` (`nombre`, `email`, `password`, `direccion`)
- `InventarioRequestDTO` (`productoId`, `cantidad`)
- `CarritoRequestDTO` (`usuarioId`, `productoId`, `cantidad`)

Para el resto de los servicios (`categorias`, `productos`, `pedidos`, `pagos`, `despachos`, `envios`, `favoritos`, `notificaciones`), el cuerpo de las peticiones es una estimación basada en el mismo patrón (campos planos, sin objetos anidados) y en las entidades JPA correspondientes, pero **no está verificado contra el `RequestDTO` real de cada uno**. Si una petición `POST` devuelve `400 Bad Request`, compara el body enviado contra el `RequestDTO` real de ese microservicio y ajusta los nombres de campo directamente en Postman.

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
├── TecnoSmart.postman_collection.json
└── README.md
```

Cada módulo sigue la estructura estándar de Maven/Spring Boot, con sus migraciones de Flyway en `src/main/resources/db/migration/` y su generador de datos de prueba en `src/main/java/duoc/<servicio>/config/DataLoader.java`.

## Notas de seguridad

Las credenciales de bases de datos de producción no se almacenan en el repositorio. Las variables sensibles (`SPRING_DATASOURCE_PASSWORD`) se configuran directamente en el dashboard de Render mediante `sync: false` en el Blueprint, y nunca quedan versionadas en `render.yaml` ni en el historial de Git.
