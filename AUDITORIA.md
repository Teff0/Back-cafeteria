# Auditoría — Back-cafeteria

> Sistema de Tickets UTP (Spring Boot 4 + Java 21 + Supabase PostgreSQL)
> Fecha de auditoría: 2026-05-19 · Rama: `feature/auditoria` · Commit base: `e5c6825`
> Última actualización: 2026-05-19 — se aplicaron fixes de seguridad, compilación y lógica de negocio.

---

## 1. Stack técnico real

| Capa | Tecnología | Notas |
|---|---|---|
| Lenguaje | Java 21 | `<java.version>21</java.version>` |
| Framework | **Spring Boot 4.0.5** | El plan decía 3.3.x → fue actualizado a 4.x |
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
├── dto/           15 DTOs — 13 originales + MenuRequest + ProductoRequest (añadidos)
├── security/      JwtUtil, CustomUserDetailsService, SupabaseAuthFilter
└── exception/     5 excepciones + GlobalExceptionHandler + ErrorResponse
```

### Modelo de dominio

- **Usuario** — codigo, email, password (BCrypt), rol enum (USUARIO/CAJA/ADMIN), activo
- **Producto** — nombre, precio (`BigDecimal`), categoría enum, disponible, stock, imagenUrl
- **Menu** — fecha + horario (DESAYUNO/ALMUERZO/CENA), `@ManyToMany` con Producto
- **Pedido** — usuario, menu, estado (PENDIENTE→PAGADO→EN_PREPARACION→LISTO→ENTREGADO/CANCELADO), total, hora programada, `@OneToMany` ItemPedido
- **ItemPedido** — pedido, producto, cantidad, precioUnitario (`BigDecimal`), subtotal (`BigDecimal`)
- **Pago** — `@OneToOne` Pedido, monto (`BigDecimal`), método (EFECTIVO/TARJETA/YAPE/PLIN), estado, codigoTransaccion
- **CarritoItem** — usuario, producto, cantidad (carrito persistido en BD)

### Endpoints REST

| Recurso | Rutas | Notas |
|---|---|---|
| `/api/auth` | POST login/logout/refresh | Tokens JWT stateless |
| `/api/products` | GET público, POST/PUT/DELETE/PATCH (ADMIN) | Body: `ProductoRequest` (DTO) |
| `/api/categories` | GET público, CRUD (ADMIN) | Gestiona menús; body: `MenuRequest` (DTO) |
| `/api/orders` | CRUD según rol, PATCH `/status` y `/cancel` | Lógica por rol (ADMIN, CAJA, USUARIO) |
| `/api/cart` | GET/POST items/PUT items/{id}/DELETE | Solo USUARIO; verifica stock |
| `/api/payments` | POST `/initiate`, GET `/{orderId}/status`, POST `/webhook` | Valida monto == total pedido |
| `/api/reports` | GET `/sales`, `/top-products` (ADMIN) | Query por rango de fechas |

### Seguridad aplicada

- `STATELESS`, `@PreAuthorize` por rol, BCrypt
- Filtro `SupabaseAuthFilter` (nombre incorrecto — valida JWT propio, no JWT de Supabase)
- Rutas públicas: `/api/auth/**`, `GET /api/products`, `GET /api/categories`, `/api/payments/webhook`

---

## 3. Migraciones y base de datos

Carpeta [migrations/](migrations/) con 3 archivos convención Flyway:

- `V1__initial_schema.sql` — 7 tablas + RLS habilitado
- `V2__initial_data.sql` — seed
- `V3__add_codigo_and_password.sql` — alter table

**Pendiente:** el `pom.xml` **no incluye Flyway ni Liquibase**, y `spring.jpa.hibernate.ddl-auto=none`. Las migraciones se aplican manualmente (Supabase Studio / CLI).

---

## 4. Hallazgos de seguridad

### ✅ Aplicados

| # | Hallazgo | Fix aplicado |
|---|---|---|
| 1 | `.env` commiteado en git con credenciales reales | `git rm --cached .env`; `.env` añadido al `.gitignore` |
| 2 | `application.properties` con defaults sensibles hardcodeados (host Supabase, password, JWT secret, API keys) | Eliminados todos los defaults sensibles — falla en arranque si falta la env var |
| 3 | `.env.example` contenía las mismas keys reales que `.env` | Reemplazado por template con placeholders |
| 4 | CORS abierto a `*` sin restricción por entorno | `CORSConfig` lee `CORS_ALLOWED_ORIGINS` (env var); lanza `IllegalStateException` si se intenta `*` |
| 5 | Logging `DEBUG` en Spring Security | Bajado a `INFO`; parametrizable con `LOG_LEVEL_SECURITY` en dev |

### ⚠️ Pendientes (acción manual requerida)

| # | Hallazgo | Acción requerida |
|---|---|---|
| 6 | **Historial git contiene credenciales** — aunque `.env` fue removido del index, el historial conserva los valores expuestos | Rotar TODAS las credenciales en Supabase: anon/service keys, DB password, JWT secret |
| 7 | **Webhook de pagos sin verificación de firma** — `POST /api/payments/webhook` es público y no valida el payload | Implementar verificación HMAC con el secret del proveedor de pagos |

---

## 5. Bugs corregidos

### 5.1 Bugs de compilación (el proyecto no arrancaba)

| Archivo | Bug | Fix |
|---|---|---|
| [CartItemResponse.java](src/main/java/com/utp/cafeteria/dto/CartItemResponse.java) | `precioUnitario` y `subtotal` como `Double`; `Producto.precio` es `BigDecimal` | Cambiado a `BigDecimal` |
| [CartResponse.java](src/main/java/com/utp/cafeteria/dto/CartResponse.java) | `total` como `Double` | Cambiado a `BigDecimal` |
| [CartService.java](src/main/java/com/utp/cafeteria/service/CartService.java) | `BigDecimal * Integer` directo (operador `*` no existe en `BigDecimal`) | `precio.multiply(BigDecimal.valueOf(cantidad))` |
| [ReportService.java](src/main/java/com/utp/cafeteria/service/ReportService.java) | `mapToDouble` con `BigDecimal` sin conversión explícita | `.doubleValue()` en todos los puntos de extracción |
| [ReportService.java](src/main/java/com/utp/cafeteria/service/ReportService.java) | `computeIfAbsent(key, () -> ...)` — lambda sin parámetro (firma incorrecta) | `computeIfAbsent(key, k -> ...)` |
| [MenuService.java](src/main/java/com/utp/cafeteria/service/MenuService.java) | `actualizarMenu` llamaba `getNombre()`, `getDescripcion()`, `getPrecio()` en `Menu` — campos que no existen en la entidad | Eliminadas; `actualizarMenu` ahora solo modifica `fecha`, `horario` y `productos` |

### 5.2 Bugs de lógica de negocio

| Archivo | Bug | Fix |
|---|---|---|
| [PedidoService.java](src/main/java/com/utp/cafeteria/service/PedidoService.java) | `cancelarPedido` no devolvía el stock de los productos al cancelar un pedido | Itera `pedido.getItems()` y restaura `producto.stock + item.cantidad` antes de cancelar |
| [PedidoService.java](src/main/java/com/utp/cafeteria/service/PedidoService.java) | `cambiarEstado` aceptaba cualquier transición arbitraria (incluidas retrogradas) | Mapa de transiciones válidas: `PAGADO→{EN_PREPARACION,CANCELADO}`, `EN_PREPARACION→{LISTO}`, `LISTO→{ENTREGADO}` |
| [PagoService.java](src/main/java/com/utp/cafeteria/service/PagoService.java) | El monto enviado en el pago no se validaba contra el total del pedido | `request.getMonto().compareTo(pedido.getTotal()) != 0` → 400 Bad Request |
| [CartService.java](src/main/java/com/utp/cafeteria/service/CartService.java) | `agregarItem` no verificaba stock disponible (solo verificaba `disponible=true`) | Validación `producto.getStock() >= cantidad` antes de insertar o acumular |
| [PedidoRequest.java](src/main/java/com/utp/cafeteria/dto/PedidoRequest.java) | `@NotBlank` en campo `LocalTime horaProgramada` — anotación solo aplica a `String` | Cambiado a `@NotNull` |

---

## 6. Mejoras de calidad aplicadas

| Área | Cambio |
|---|---|
| **DTOs** | Creados [MenuRequest.java](src/main/java/com/utp/cafeteria/dto/MenuRequest.java) y [ProductoRequest.java](src/main/java/com/utp/cafeteria/dto/ProductoRequest.java). Los controllers ya no reciben entidades JPA directamente como `@RequestBody` (elimina riesgo de mass-assignment) |
| **MenuService** | `crearMenu` valida unicidad de `fecha + horario`; `actualizarMenu` acepta `Set<UUID> productoIds` para reasignar productos al menú |
| **ProductoService** | `crear` respeta el campo `stock` del request (default 100 si se omite); antes ignoraba el valor siempre |
| **PedidoService** | Eliminado `mapToResponseAdmin` (duplicado exacto de `mapToResponse`); consolidado en un solo método |
| **PedidoRepository** | Nueva query `findByFechaRangoYEstado(LocalDateTime, LocalDateTime)` con filtro de fechas y exclusión de CANCELADOS — reemplaza el `findAll()` + filtro en Java que tenía `ReportService` |
| **Validación** | `@Valid` añadido en todos los endpoints CRUD que reciben request body |

---

## 7. Lo que sí estaba bien (sin cambios)

- Arquitectura en capas clara y consistente.
- `GlobalExceptionHandler` con `ErrorResponse` unificado.
- Roles bien modelados con `@PreAuthorize`.
- `PedidoService.crearPedido` con validación de stock y descuento correcto.
- `PagoService` — flujo de pago, generación de código de transacción.
- WebSocket para notificación de cambios de estado en cola.
- Repositories con queries JPQL nombradas (sin `findAll` innecesarios, excepto los ya corregidos).

---

## 8. Pendientes (backlog)

| Prioridad | Item |
|---|---|
| P0 | **Rotar credenciales** en Supabase (keys, DB password, JWT secret) — el historial git las expone |
| P1 | Añadir **Flyway** al `pom.xml` y mover [migrations/](migrations/) a `src/main/resources/db/migration/` |
| P1 | Implementar **verificación de firma** en `POST /api/payments/webhook` |
| P1 | Agregar **SpringDoc OpenAPI** (swagger-ui) |
| P2 | Tests unitarios: empezar por `PedidoService` (estados, stock) con Mockito |
| P2 | Crear `README.md` con setup local, variables requeridas y arranque |
| P2 | Renombrar `SupabaseAuthFilter` → `JwtAuthFilter` (no usa Supabase Auth) |
| P2 | Considerar MapStruct cuando los `mapToResponse` crezcan |

---

## 9. Verificación

```bash
# Compilar (debe pasar sin errores)
./mvnw -DskipTests compile

# Arrancar (requiere .env completo con credenciales reales)
./mvnw spring-boot:run

# Smoke test endpoints públicos
curl http://localhost:8080/api/products
curl http://localhost:8080/api/categories

# Verificar que .env no está trackeado
git ls-files .env    # debe retornar vacío

# Flujo de login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"codigo":"<codigo>","password":"<password>"}'
```

> **Nota IDE:** el Language Server JDT de VSCode reporta errores falsos en archivos con `@RequiredArgsConstructor` de Lombok. Estos no son errores reales — `./mvnw compile` confirma cero errores de `javac`. Para eliminar los falsos positivos instala el plugin oficial de Lombok para VSCode.
