# Progreso Sprint 2 — LogiTrack

## Última actualización
- Fecha: 2026-10-27
- Rama: `refactor/s2-clean-code` (parte de `develop` en `6d930a0`)
- Último commit: `a471f86` — limpieza de comentarios y documentación
- Build: `.\mvnw.cmd -B clean verify` → SUCCESS (210 tests)

### Detalle del build
| Módulo | Tests |
|---|---:|
| `config-server` | 1 |
| `shipment-service` | 139 |
| `courier-service` | 51 |
| `security-service` | 19 |
| **Total** | **210** |

`common-security` no tiene tests propios: su comportamiento se cubre con los 11 tests de
`ShipmentSecurityTest`, los 9 de `CourierSecurityTest` y los 16 de `ShipmentOwnershipTest`.

## Hecho
- [x] S2-05 (#15) — `security-service`: usuarios, roles, BCrypt y JWT.
- [x] S2-06 (#16) — resource server JWT en shipment y courier.
- [x] S2-07 (#18) — autorización por pertenencia del recurso.
- [x] S2-11 (#22) — tests y CI multi-módulo (issue abierta por lo administrativo).
- [x] Línea base verificada (210 tests) y `docs/PROGRESO.md`.
- [x] Auditoría en `docs/AUDITORIA-CLEAN-CODE.md` con los 15 puntos verificados.
- [x] **P0:** módulo `common-security` + ADR 0001; se eliminaron 6 clases duplicadas.
- [x] **P1:** alta de cliente con un solo guardado y vinculación antes del `insert`.
- [x] **P1:** `UsuarioActual` lee la identidad solo del claim `sub`.
- [x] **P2:** comentarios residuales fuera; README actualizado (estados, tests, estructura).

## En curso
- Nada. La pasada de clean code está cerrada.

## Siguiente paso exacto
1. Revisar el PR de `refactor/s2-clean-code` → `develop` y mergear con **merge commit**.
2. Esperar el CI de `develop`.
3. Las issues S2-04, S2-08, S2-09 y S2-10 las resuelve el usuario; **no tocarlas** desde acá.

## Decisiones tomadas
- **ADR 0001:** la configuración del resource server vive en `common-security` (librería), no
  duplicada ni en un microservicio.
- Cada `SecurityConfig` conserva solo su matriz de autorización.
- La autorización por pertenencia depende exclusivamente del JWT; no hay fallback al principal de
  `@WithMockUser`.
- **Alcance cerrado:** S2-04 (#14), S2-08 (#19), S2-09 (#20) y S2-10 (#21) no se tocaron.
- S2-12 (#23) sigue bloqueada por esas cuatro.

## Bloqueos / dudas
- Ninguno para esta pasada.
- Cuando se hagan los Dockerfiles (S2-08), cada build debe usar `-pl <servicio> -am` para incluir
  `common-security`.

## Archivos clave tocados
- `common-security/` (nuevo módulo).
- `shipment-service/.../config/SecurityConfig.java`
- `courier-service/.../config/SecurityConfig.java`
- `shipment-service/.../security/UsuarioActual.java`
- `shipment-service/.../service/impl/ShipmentServiceImpl.java`
- `security-service/.../service/AuthServiceImpl.java`
- `docs/PROGRESO.md`, `docs/AUDITORIA-CLEAN-CODE.md`, `docs/adr/0001-seguridad-compartida.md`
- `README.md`

## Cómo retomar
1. `git pull --ff-only`
2. `.\mvnw.cmd -B clean verify`
3. Leer este archivo y continuar desde “Siguiente paso exacto”.
