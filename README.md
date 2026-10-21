# daas-proyecto-final · LogiTrack shipment-service

Microservicio del **Sprint 1** (Desarrollo y Arquitecturas Avanzadas de Software – FI UNJu 2026).
Gestiona el ciclo de vida de los envíos, el historial de auditoría de estados y los repartidores.

| | |
|---|---|
| Lenguaje | Java 25 |
| Framework | Spring Boot 3.5 (Web, Data JPA, Validation, Actuator) |
| Base de datos | MySQL 8.4 (H2 en memoria solo para tests) |
| Documentación | springdoc-openapi (Swagger UI) |
| Tests | JUnit 5, Mockito, AssertJ, @DataJpaTest, @WebMvcTest |
| Infra | Dockerfile multi-stage + docker-compose |

---
## Modelo de dominio

```mermaid
classDiagram
    direction LR

    class BaseEntity {
        <<abstract>>
        -UUID id
        -Long version
        -Instant createdAt
        -Instant updatedAt
    }

    class Cliente {
        -String nombre
        -String email
        -String telefono
        +actualizarContacto(String nombre, String telefono)
    }

    class Envio {
        -String codigoSeguimiento
        -BigDecimal pesoKg
        -BigDecimal altoCm
        -BigDecimal anchoCm
        -BigDecimal largoCm
        -BigDecimal tarifa
        -EstadoEnvio estado
        +calcularTarifa() BigDecimal
        +puedeCancelarse() boolean
        +cancelar()
        +asignarRepartidor(Repartidor repartidor)
        +cambiarEstado(EstadoEnvio nuevoEstado)
    }

    class Direccion {
        <<Embeddable>>
        -String calle
        -String numero
        -String ciudad
        -String codigoPostal
    }

    class HistorialEstado {
        -EstadoEnvio estadoAnterior
        -EstadoEnvio estadoNuevo
        -Instant fechaHora
    }

    class Repartidor {
        -String nombre
        -TipoVehiculo tipoVehiculo
        -EstadoRepartidor estado
        +estaActivo() boolean
        +activar()
        +desactivar()
    }

    class EstadoEnvio {
        <<enumeration>>
        PENDIENTE
        EN_RUTA
        ENTREGADO
        CANCELADO
        +transicionesPermitidas() Set
        +puedeTransicionarA(EstadoEnvio destino) boolean
        +esFinal() boolean
    }

    class EstadoRepartidor {
        <<enumeration>>
        ACTIVO
        INACTIVO
    }

    class TipoVehiculo {
        <<enumeration>>
        BICICLETA
        MOTO
        AUTO
        CAMIONETA
    }

    BaseEntity <|-- Cliente
    BaseEntity <|-- Envio
    BaseEntity <|-- Repartidor
    BaseEntity <|-- HistorialEstado

    Cliente "1" --> "*" Envio : realiza
    Envio "*" --> "0..1" Repartidor : asignado_a
    Envio *-- "1" Direccion : origen
    Envio *-- "1" Direccion : destino
    Envio "1" *-- "*" HistorialEstado : historial
    Envio --> EstadoEnvio : estado
    HistorialEstado --> EstadoEnvio : estadoAnterior / estadoNuevo
    Repartidor --> EstadoRepartidor : estado
    Repartidor --> TipoVehiculo : tipoVehiculo
```

Los cambios que hicimos respecto del modelo guía del PDF están explicados en la [sección 6](#6-modelo-de-dominio--cambios-respecto-del-modelo-guía).

---

## 1. Requisitos

- **JDK 25** (Temurin recomendado)
- **Maven 3.9+** — IntelliJ ya trae uno integrado; si no lo tenés instalado podés usar el del IDE
- **Docker Desktop** (o Docker Engine + Compose v2)

## 2. Levantar todo con un comando

```bash
docker compose up --build
```

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health
- MySQL: `localhost:3307` · usuario `logitrack` / `logitrack` · base `logitrack_shipments`

El script [`db/init/01-schema.sql`](db/init/01-schema.sql) crea las tablas y carga datos de prueba
**solo la primera vez** que se crea el volumen. Para empezar de cero:

```bash
docker compose down -v
```

## 3. Desarrollo local (app desde el IDE)

```bash
docker compose up -d mysql        # solo la base
```

Después ejecutar `ShipmentServiceApplication` desde el IDE (o `mvn spring-boot:run`).
Los valores por defecto de `application.yml` ya apuntan a ese MySQL; se pueden pisar con variables
de entorno (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`). Ver `.env.example`.

## 4. Tests

```bash
mvn test
```

La suite **no necesita MySQL ni Docker**: usa H2 en memoria (modo MySQL). El pipeline de
GitHub Actions (`.github/workflows/ci.yml`) corre `mvn verify` en cada push y pull request;
el resultado se ve en la pestaña **Actions** del repositorio.

| Test | Tipo | Qué valida |
|---|---|---|
| `EstadoEnvioTest` | Unitario | Matriz completa de transiciones de la máquina de estados |
| `EnvioTest` | Unitario | Cálculo de tarifa, cancelación, asignación, historial |
| `RepartidorServiceTest` | Unitario (Mockito) | Servicio con repositorio mockeado — **modelo para `ShipmentServiceTest`** |
| `RepartidorRepositoryTest` / `EnvioRepositoryTest` | Integración (`@DataJpaTest`) | Mapeo JPA, UUID, auditoría, cascada del historial |
| `RepartidorControllerTest` | Integración (`@WebMvcTest`) | Contrato HTTP, códigos 201/400/404 |
| `ShipmentServiceApplicationTests` | `@SpringBootTest` | El contexto completo levanta |

## 5. Estructura

```
src/main/java/ar/edu/unju/fi/logitrack/shipment
├── config/       JPA auditing, OpenAPI
├── controller/   REST controllers (solo reciben/devuelven DTOs)
├── dto/          records de request/response, uno por módulo (dto/repartidor, dto/envio, ...)
├── entity/       entidades JPA + enums + Value Object Direccion
├── exception/    excepciones de dominio + GlobalExceptionHandler (RFC 7807)
├── mapper/       conversión entidad ⇄ DTO
├── repository/   Spring Data JPA
└── service/      lógica de aplicación y transacciones
```

**El módulo Repartidores está completo como ejemplo de referencia** (DTO → Controller → Service →
Repository + tests de cada capa). Los módulos de envíos se implementan siguiendo el mismo patrón.

## 6. Modelo de dominio — cambios respecto del modelo guía

| Cambio | Motivo |
|---|---|
| `BaseEntity` con `id` UUID, `createdAt`, `updatedAt` y `version` | Requisito de UUID + auditoría en **todas** las entidades; `@Version` evita actualizaciones perdidas |
| Nueva entidad `HistorialEstado` | Trazabilidad de cada cambio de estado (estado anterior, nuevo, fecha/hora) |
| `BigDecimal` en lugar de `Double` para peso, medidas y tarifa | Evitar errores de redondeo con dinero |
| `TipoVehiculo` enum en lugar de `String` | Valores consistentes y validables |
| `Direccion` como `@Embeddable` inmutable | Value Object sin identidad; se guarda en columnas `origen_*` / `destino_*` |
| Máquina de estados en `EstadoEnvio.transicionesPermitidas()` | Una sola fuente de verdad para las transiciones |
| `Envio` sin setters: `cambiarEstado()`, `cancelar()`, `asignarRepartidor()` | Las reglas no se pueden saltear; cada cambio de estado agrega historial automáticamente |
| Código de seguimiento `LT-XXXXXXXXXX` (`TrackingCodeGenerator`) | Legible, sin caracteres ambiguos |

**Reglas de negocio implementadas en el dominio**

- Flujo: `PENDIENTE → EN_RUTA → ENTREGADO`, o `PENDIENTE → CANCELADO`. Cualquier otra transición → 422.
- Solo se cancela en `PENDIENTE`.
- Para pasar a `EN_RUTA` el envío debe tener repartidor; solo se asignan repartidores `ACTIVO` a envíos `PENDIENTE`.
- **Tarifa** (provisoria, a validar con el equipo): `1500 + 450 × máx(peso real, peso volumétrico)`,
  con peso volumétrico = `alto × ancho × largo / 5000`. Las constantes están en `Envio`.

## 7. API

Base: `/api/v1`. Errores en formato `application/problem+json`:
400 (validación / formato), 404 (no existe), 409 (dato duplicado), 422 (regla de negocio).

| Método | Endpoint | Descripción | Estado |
|---|---|---|---|
| POST | `/repartidores` | Registrar repartidor | ✅ |
| GET | `/repartidores?estado=ACTIVO` | Listar (filtro opcional) | ✅ |
| GET | `/repartidores/{id}` | Detalle | ✅ |
| POST | `/envios` | Registrar envío (cliente, origen, destino, peso, medidas) | ⬜ |
| GET | `/envios/{id}` | Consultar por id | ⬜ |
| GET | `/envios/seguimiento/{codigo}` | Consultar por código de seguimiento | ⬜ |
| GET | `/envios?estado=PENDIENTE` | Listar por estado | ⬜ |
| PATCH | `/envios/{id}/estado` | Cambiar estado `{ "estado": "EN_RUTA" }` | ⬜ |
| POST | `/envios/{id}/cancelacion` | Cancelar (solo PENDIENTE) | ⬜ |
| PUT | `/envios/{id}/repartidor` | Asignar repartidor `{ "repartidorId": "..." }` | ⬜ |
| GET | `/envios/{id}/historial` | Historial de estados | ⬜ |

## 8. Flujo de trabajo en Git

- `main`: versión entregable, protegida (solo pull requests).
- `develop`: integración del sprint.
- `feature/<n°-issue>-<descripcion-corta>` → pull request hacia `develop`, con al menos una revisión.
- Commits estilo *Conventional Commits*: `feat: ...`, `fix: ...`, `test: ...`, `docs: ...`, `chore: ...`.

### Reparto sugerido de tareas (un issue + una rama por tarea)

| # | Tarea | Rama sugerida |
|---|---|---|
| 1 | Base del proyecto, Docker, dominio y módulo repartidores | `feature/1-base-proyecto` ✅ |
| 2 | Registrar envío: `EnvioRequest/Response`, `EnvioMapper`, cliente buscar-o-crear por email, `ShipmentService.registrar`, `POST /envios` | `feature/2-registro-envios` |
| 3 | Consultas: por id, por código de seguimiento, por estado | `feature/3-consultas-envios` |
| 4 | Cambio de estado + endpoint de historial | `feature/4-estados-historial` |
| 5 | Cancelación y asignación de repartidor | `feature/5-cancelacion-asignacion` |
| 6 | `ShipmentServiceTest` (tarifa + máquina de estados) y `@WebMvcTest` de envíos | `feature/6-tests-envios` |
| 7 | Revisión final, Swagger, README y guion del video | `feature/7-documentacion` |

### Primer push (lo hace una sola persona)

```bash
git init -b main
git add .
git commit -m "chore: proyecto base daas-proyecto-final (shipment-service)"
git remote add origin <URL-del-repo-en-GitHub>
git push -u origin main
git switch -c develop
git push -u origin develop
```

Los demás:

```bash
git clone <URL-del-repo-en-GitHub>
cd daas-proyecto-final
git switch develop
git switch -c feature/<n°>-<descripcion>
```
