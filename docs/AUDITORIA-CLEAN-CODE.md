# Auditoría de clean code — S2-05 / S2-06 / S2-07

**Fecha:** 2026-10-27
**Rama:** `refactor/s2-clean-code` (base `develop` `6d930a0`)
**Línea base:** `.\mvnw.cmd -B clean verify` → SUCCESS, 210 tests.

**Alcance:** solo código de S2-05 (security), S2-06 (resource server) y S2-07 (pertenencia).
**Fuera de alcance:** S2-04, S2-08, S2-09 y S2-10. No se toca `config-server/`, `Dockerfile`, `docker-compose.yml`/`compose.yaml` ni imágenes.

---

## 1. Hallazgos verificados

| # | Ubicación | Hallazgo | Evidencia | Impacto | Prioridad |
|---|---|---|---|---|---|
| 1 | `shipment/config` y `courier/config` | 4 clases duplicadas **literalmente**: `ProblemDetailWriter`, `JwtAuthenticationEntryPoint`, `JwtAccessDeniedHandler` y casi todo `SecurityConfig` | `ProblemDetailWriter` idéntico (35 líneas); entry point y denied handler idénticos salvo el paquete; `SecurityConfig` comparte rutas públicas, decoder, converter y cadena de filtros | Alto: dos copias que hay que mantener sincronizadas | **P0** |
| 2 | `SecurityConfig` de ambos servicios | Constantes `"ADMIN"`, `"CLIENTE"`, `"REPARTIDOR"` como strings sueltos, mientras `UsuarioActual` ya define `ROL_*` | `hasAnyRole("ADMIN", "CLIENTE")` vs `UsuarioActual.ROL_ADMIN` | Bajo: riesgo de typo silencioso | **P0** (se resuelve junto con #1) |
| 3 | `ShipmentServiceImpl.obtenerOCrearCliente` | Un cliente nuevo se guarda dos veces: al crearlo y al vincularlo al usuario | Línea 168 `clienteRepository.save(new Cliente(...))` y línea 183 `clienteRepository.save(cliente)` | Bajo: un `INSERT` + un `UPDATE` innecesario por alta | **P1** |
| 4 | `UsuarioActual.usuarioId()` | Fallback a `Authentication.getName()` “para tests con `@WithMockUser`” | Línea 49; los únicos `@WithMockUser` son `ADMIN` y no necesitan `usuarioId` | Medio: la identidad podría salir de un principal que no es el JWT | **P1** |
| 5 | `AuthServiceImpl` | Dos comentarios en maltés y uno redundante | Líneas 25, 45 y 50: “Kostruttur espliċitu…”, “Validación de regla…”, “Użu tal-Builder…” | Bajo: ruido y resto de otra autoría | **P2** |
| 6 | `README.md` | Estados desactualizados: `security-service` y `config-server` figuran como “esqueleto”; S2-07 aparece como pendiente | Líneas 25, 26, 143-144 y 179 | Bajo: documentación engañosa | **P2** |
| 7 | `.env.example` | Solo declara la base de shipment | Falta `DB_NAME` de courier/security y las URLs entre servicios | Bajo: depende del Compose completo | **No tocar** (S2-09) |
| 8 | `JwtTestSupport` (shipment y courier) | Dos fixtures parecidos pero **no iguales**: courier no firma `sub` UUID ni `repartidorId` | `Compare-Object` muestra diferencias reales | Bajo: es duplicación de test aceptable; un `test-jar` agrega complejidad | **No tocar** |
| 9 | Paquete `ar.edu.unju.fi.securityservice` | No sigue `ar.edu.unju.fi.logitrack.*` | `security-service/src/main/java/...` | Alto: renombrar toca clases, tests, `@ComponentScan` y documentación | **No tocar** |
| 10 | `courier-service` usa `.properties` | El resto usa YAML | `application.properties` vs `application.yml` | Bajo | **No tocar** (S2-04 externaliza la configuración) |
| 11 | `AutorizacionEnvio` | Mezcla lectura, escritura y filtro de listado | 115 líneas, 4 métodos públicos | Bajo: es una clase de política cohesiva y legible | **No tocar** |
| 12 | `ShipmentServiceImpl.buscarSegunFiltros` | Cuatro ramas para combinaciones estado/repartidor | Líneas 131-140 | Bajo: cada rama delega en un método distinto del repositorio; abstraerlo no reduce líneas | **No tocar** |
| 13 | `Dockerfile` raíz | No coincide con el multi-módulo | `COPY src`, `target/daas-proyecto-final.jar` | — | **Excluido (S2-08)** |

---

## 2. Refactors elegidos (máximo 3)

### P0 — Módulo `common-security`
Un módulo Maven tipo `jar` (librería, **no** microservicio) con:
- `ProblemDetailWriter`, `JwtAuthenticationEntryPoint`, `JwtAccessDeniedHandler`.
- `Roles` con las constantes `ROLE_*`.
- Una configuración base que aporte `JwtDecoder` y el converter de `roles`.
- Cada servicio declara **solo** su matriz de autorización en su `SecurityConfig`.

**Por qué:** elimina ~250 líneas duplicadas y la posibilidad de que un servicio quede con un comportamiento distinto al otro. Es la opción recomendada en el ADR.

**Riesgo:** agrega un módulo al reactor. No afecta Docker (fuera de alcance), pero cuando se hagan los Dockerfiles (S2-08) hay que incluir `-pl <servicio> -am`.

### P1 — `ShipmentServiceImpl.obtenerOCrearCliente`
Resolver la identidad una sola vez y vincular **antes** del `save`, para que un cliente nuevo se inserte ya con dueño y no haya doble escritura.

**Por qué:** corrige el doble guardado y deja el flujo de alta en un solo punto.

**Riesgo:** bajo; los tests de pertenencia cubren alta con y sin dueño.

### P1 — Quitar el fallback de `UsuarioActual.usuarioId()`
Leer la identidad solo del claim `sub` del JWT. Si no hay JWT, devolver `null` (y el llamador ya lanza 403).

**Por qué:** la autorización no debe depender del nombre del principal. Los tests con `@WithMockUser` son de `ADMIN` y no usan `usuarioId()`.

**Riesgo:** bajo; verificado con los 10 tests de `AutorizacionEnvioTest` y los 16 de pertenencia.

### P2 — Limpieza
Comentarios en maltés de `AuthServiceImpl`, README desactualizado y `.env.example` incompleto.

---

## 3. Lo que NO conviene tocar

| Tema | Motivo |
|---|---|
| Renombrar el paquete `securityservice` | Alto riesgo y cero valor funcional; se documenta como deuda. |
| Unificar `.properties` de courier a YAML | S2-04 va a mover esa configuración al Config Server; hacerlo ahora genera conflicto. |
| Extraer `JwtTestSupport` a un `test-jar` | Son ~60 líneas y los fixtures ya divergieron; el `test-jar` agrega acoplamiento entre módulos. |
| Partir `AutorizacionEnvio` | Es una política de autorización cohesiva; partirla dispersa la regla de negocio. |
| Reescribir `buscarSegunFiltros` con `Specification` | No reduce complejidad y agrega una dependencia de criterios dinámicos. |
| `Dockerfile` raíz y Compose | Pertenecen a S2-08/S2-09, fuera de alcance. |
| `.env.example` | Las variables de courier/security dependen del Compose completo (S2-09). |

---

## 4. Criterio de aceptación de la pasada

1. `.\mvnw.cmd -B clean verify` en verde con **210 tests** (o más si se agregan).
2. Comportamiento HTTP idéntico: 401/403 con el mismo `ProblemDetail`.
3. Ningún cambio en `config-server/`, `Dockerfile`, `docker-compose.yml` ni `compose.yaml`.
4. Documentación y commits en español.
