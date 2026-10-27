# Progreso Sprint 2 — LogiTrack

## Última actualización
- Fecha: 2026-10-27
- Rama: `refactor/s2-clean-code` (parte de `develop` en `6d930a0`)
- Último commit: `6d930a0` — Merge PR #33 (S2-07)
- Build: `.\mvnw.cmd -B clean verify` → SUCCESS (210 tests)

### Detalle del build
| Módulo | Tests |
|---|---:|
| `config-server` | 1 |
| `shipment-service` | 139 |
| `courier-service` | 51 |
| `security-service` | 19 |
| **Total** | **210** |

## Hecho
- [x] S2-05 (#15) — `security-service`: usuarios, roles, BCrypt y JWT.
- [x] S2-06 (#16) — resource server JWT en shipment y courier.
- [x] S2-07 (#18) — autorización por pertenencia del recurso.
- [x] S2-11 (#22) — tests y CI multi-módulo (issue abierta por lo administrativo).
- [x] Línea base del Sprint 2 verificada: build completo en verde, 210 tests.

## En curso
- Pasada de clean code sobre S2-05/06/07: auditoría y refactors sin cambiar comportamiento.

## Siguiente paso exacto
1. Crear `docs/AUDITORIA-CLEAN-CODE.md` con los hallazgos verificados contra el código.
2. Elegir como máximo 3 refactors para el próximo paso (P0/P1).

## Decisiones tomadas
- **Alcance cerrado:** S2-04 (#14), S2-08 (#19), S2-09 (#20) y S2-10 (#21) **no se tocan** en esta pasada. Las resuelve el usuario.
- S2-12 (#23) queda bloqueada hasta que S2-04/08/09/10 estén resueltas.
- El trabajo se hace sobre `refactor/s2-clean-code`, una sola rama para toda la pasada; PR a `develop` con merge commit.
- Los refactors no deben cambiar el comportamiento HTTP ni la API pública.

## Bloqueos / dudas
- Nada bloqueante para la auditoría.
- Si un refactor obliga a tocar `Dockerfile`, `docker-compose.yml` o `config-server`, se descarta: está fuera de alcance.

## Archivos clave tocados
- `docs/PROGRESO.md` (este archivo).

## Cómo retomar
1. `git pull --ff-only`
2. `.\mvnw.cmd -B clean verify`
3. Leer este archivo y continuar desde “Siguiente paso exacto”.
