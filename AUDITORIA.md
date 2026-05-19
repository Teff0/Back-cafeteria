# Auditoría — Back-cafeteria

> Sistema de Tickets UTP (Spring Boot 4 + Java 21 + Supabase PostgreSQL)
> Fecha de auditoría: 2026-05-19 · Rama: `dev` · Commit base: `e5c6825`

## Contexto

`Back-cafeteria` es un backend Java/Spring Boot para un **Sistema de Tickets de Cafetería UTP**: los estudiantes seleccionan comida, generan un pedido y un código (QR/número) para retirarlo, reduciendo filas. El proyecto está en rama `dev`, sin README y sin tests. Este documento es un análisis **read-only** del estado actual: stack, dominio, riesgos y desviaciones respecto al [Plan-Tech-Stack](Plan-Tech-Stack) original.

---

## 1. Stack técnico real

| Capa | Tecnología | Notas |
|---|---|---|
| Lenguaje | Java 21 | `<java.version>21</java.version>` |
| Framework | **Spring Boot 4.0.5** | El plan decía 3.3.x → **fue actualizado a 4.x** |
| Build | Maven (wrapper incluido `mvnw`) | `tickets-backend` v0.0.1-SNAPSHOT, groupId `com.utp` |
| Web | `spring-boot-starter-webmvc` + `webflux` (cliente) | En SB 4.x el starter cambió de `web` a `webmvc` |
| Persistencia | Spring Data JPA + PostgreSQL JDBC | `ddl-auto=none` (schema gestionado por SQL) |
| Seguridad | Spring Security 6.x + JJWT 0.12.5 | Auth JWT propio, no Supabase Auth |
| Tiempo real | Spring WebSocket | `WebSocketNotificationService` |
| Utilidades | Lombok, `dotenv-java` 3.0.2 | Carga `.env` desde [EnvConfig.java](src/main/java/com/utp/cafeteria/config/EnvConfig.java) |
| Tests | `*-test` starters declarados | **0 archivos en `src/test/`** |

**Dependencias planificadas pero NO incluidas:** MapStruct, SpringDoc OpenAPI (Swagger), Testcontainers, Flyway/Liquibase.

---

## 2. Arquitectura y dominio

**Estructura por capas** (no por feature como sugería el plan):

```
com.utp.cafeteria
├── config/        CORSConfig, EnvConfig, SecurityConfig, WebSocketConfig
├── controller/    Auth, Cart, Category, Order, Pago, Product, Report
├── service/       Auth, Cart, Menu, Pedido, Pago, Producto, Report, WebSocketNotification
├── entity/        Usuario, Producto, Menu, CarritoItem, Pedido, ItemPedido, Pago
├── repository/    7 interfaces JpaRepository (una por entidad)
├── dto/           13 DTOs (Request/Response separados)
├── security/      JwtUtil, CustomUserDetailsService, SupabaseAuthFilter
└── exception/     5 excepciones + GlobalExceptionHandler + ErrorResponse
```

### Modelo de dominio

- **Usuario** — codigo, email, password (BCrypt), rol enum (USUARIO/CAJA/ADMIN), activo
- **Producto** — nombre, precio, categoría enum, disponible, stock, imagenUrl
- **Menu** — fecha + horario (DESAYUNO/ALMUERZO/CENA), `@ManyToMany` con Producto
- **Pedido** — usuario, menu, estado (PENDIENTE→PAGADO→EN_PREPARACION→LISTO→ENTREGADO/CANCELADO), total, hora programada, `@OneToMany` ItemPedido
- **ItemPedido** — pedido, producto, cantidad, precioUnitario, subtotal
- **Pago** — `@OneToOne` Pedido, monto, método (EFECTIVO/TARJETA/YAPE/PLIN), estado, codigoTransaccion
- **CarritoItem** — usuario, producto, cantidad (carrito persistido en BD)

### Endpoints REST (resumen por recurso)

| Recurso | Rutas | Notas |
|---|---|---|
| `/api/auth` | POST login/logout/refresh | Tokens JWT stateless |
| `/api/products` | GET público, POST/PUT/DELETE/PATCH (ADMIN) | PATCH `/{id}/availability` |
| `/api/categories` | GET público, CRUD (ADMIN) | En realidad gestiona **menús**, no categorías |
| `/api/orders` | CRUD según rol, PATCH `/status` y `/cancel` | Lógica por rol (ADMIN, CAJA, USUARIO) |
| `/api/cart` | GET/POST items/PUT items/{id}/DELETE | Carrito persistente por usuario |
| `/api/payments` | POST `/initiate`, GET `/{orderId}/status`, POST `/webhook` | Webhook **público y vacío** |
| `/api/reports` | GET `/sales`, `/top-products` (ADMIN) | Reportes simples |

### Seguridad

- `STATELESS`, `@PreAuthorize` por rol, BCrypt
- Filtro `SupabaseAuthFilter` (mal nombrado — en realidad valida JWT propio, no JWT de Supabase)
- Rutas públicas: `/api/auth/**`, `GET /api/products`, `GET /api/categories`, `/api/payments/webhook`

---

## 3. Migraciones y base de datos

Carpeta [migrations/](migrations/) con 3 archivos convención Flyway:

- `V1__initial_schema.sql` — 7 tablas + RLS habilitado
- `V2__initial_data.sql` — seed
- `V3__add_codigo_and_password.sql` — alter table

**Problema:** el `pom.xml` **NO incluye Flyway ni Liquibase**, y `spring.jpa.hibernate.ddl-auto=none`. Las migraciones se deben aplicar manualmente (Supabase Studio / CLI) — no hay automatización en arranque.

---

## 4. Hallazgos críticos (riesgos)

### 🔴 Seguridad — acción inmediata recomendada

1. **`.env` está commiteado en git** (`git ls-files .env` → encontrado) con credenciales reales de Supabase, JWT secret y password de BD. Aunque rotes credenciales, el historial las conserva.
   - [.gitignore](.gitignore) no incluye `.env`
2. **`application.properties` tiene credenciales como valores por defecto** en claro: host Supabase real (`dbnhyyrnnqoppwqnedvrgg.supabase.co`), `password=contraseña123`, JWT secret base64, anon/service keys. Esto neutraliza la protección de `.env` aunque lo borraras.
3. **CORS abierto a `*`** (`cors.allowed-origins=*`) sin restricción por entorno.
4. **Webhook de pagos público y sin verificación de firma** ([PagoController](src/main/java/com/utp/cafeteria/controller/PagoController.java) líneas 41-42 reportadas vacías).
5. **Logging `DEBUG` en Spring Security** — filtraría tokens y headers sensibles en logs de producción.

### 🟡 Desviaciones del Plan-Tech-Stack

| Plan | Real | Impacto |
|---|---|---|
| Spring Boot 3.3.x | 4.0.5 | Starters renombrados (`web`→`webmvc`); algunos artefactos `*-test` declarados son inusuales |
| Roles `ROLE_LOCAL` / `ROLE_CLIENTE` | `USUARIO` / `CAJA` / `ADMIN` | El plan no contemplaba CAJA — bien para el negocio, pero desalineado |
| MapStruct | Mapeo manual `mapToResponse()` en servicios | Boilerplate creciente |
| SpringDoc OpenAPI (Swagger) | Ausente | No hay documentación viva de la API |
| Testcontainers + JUnit | Sin tests | Riesgo alto al evolucionar |
| Estructura por feature (`auth/`, `tickets/`...) | Estructura por capa | Decisión válida pero diferente al plan |

### 🟡 Calidad

- **0 tests** en el proyecto pese a starters de test declarados.
- **Sin README** ni documentación de arranque local.
- **Hibernate `ddl-auto=none` + sin Flyway** → cualquier nuevo entorno requiere correr SQL a mano.
- **CarritoItem en BD** convierte una operación de alta frecuencia (agregar al carrito) en escritura a Supabase — revisar si justifica un carrito en cliente o cache.
- **Webhook vacío** y mapeos parciales.

---

## 5. Lo que sí está bien

- Arquitectura en capas clara y consistente.
- Excepciones de dominio con `GlobalExceptionHandler` y `ErrorResponse` unificado.
- Roles bien modelados con `@PreAuthorize`.
- Separación Request/Response en DTOs.
- WebSocket pensado para notificar cambios de estado en cola (alineado con el caso de uso "fila").
- Estados de pedido bien definidos (máquina de estados implícita).

---

## 6. Recomendaciones priorizadas

**P0 — hoy mismo:**
1. Rotar todas las credenciales del `.env`/`application.properties` (Supabase keys, DB password, JWT secret).
2. Añadir `.env` al `.gitignore` y `git rm --cached .env`.
3. Eliminar los valores por defecto sensibles de [application.properties](src/main/resources/application.properties) (que falle si falta la env var).
4. Restringir CORS por entorno (perfiles `dev`/`prod`).
5. Bajar logging de Spring Security a `INFO` en prod.

**P1 — esta semana:**
6. Añadir **Flyway** al pom y mover [migrations/](migrations/) a `src/main/resources/db/migration/`.
7. Implementar verificación de firma del webhook de pagos.
8. Agregar **SpringDoc OpenAPI** (un solo starter, anotaciones mínimas en controllers).

**P2 — backlog:**
9. Tests: empezar por `PedidoService` (lógica de estados/stock) con Mockito; integración con Testcontainers para repos.
10. Crear `README.md` con setup local, variables, comando para correr migraciones y arranque.
11. Revisar nombre del filtro `SupabaseAuthFilter` (no usa Supabase Auth).
12. Considerar MapStruct si los `mapToResponse` empiezan a duplicarse.

---

## 7. Verificación end-to-end

Para confirmar el análisis tras cualquier cambio:

- `./mvnw clean verify` — compila y corre tests (cuando existan).
- `./mvnw spring-boot:run` con `.env` cargado — levantar API en `:8080`.
- `curl http://localhost:8080/api/products` — debe responder sin auth.
- `POST /api/auth/login` con un usuario seed (ver [V2__initial_data.sql](migrations/V2__initial_data.sql)) — obtener JWT.
- `git ls-files .env` — debe retornar vacío.
- Confirmar en Supabase MCP que el schema aplicado coincide con `V1`, `V2`, `V3`.
