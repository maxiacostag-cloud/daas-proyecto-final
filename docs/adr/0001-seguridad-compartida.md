# ADR 0001 — Seguridad del resource server compartida entre microservicios

- **Estado:** aceptada
- **Fecha:** 2026-10-27
- **Contexto:** S2-06 (resource server JWT) y S2-07 (autorización por pertenencia)

## Problema

`shipment-service` y `courier-service` validan los mismos JWT emitidos por `security-service`.
Antes de esta decisión cada uno tenía su propia copia de:

- `ProblemDetailWriter` (respuestas 401/403 en RFC 7807).
- `JwtAuthenticationEntryPoint` y `JwtAccessDeniedHandler`.
- El `JwtDecoder` HS256 y el converter del claim `roles` en `SecurityConfig`.

Las copias eran idénticas salvo el paquete. Cualquier cambio (por ejemplo, el formato del error o
un claim nuevo) obligaba a tocar dos servicios y era fácil que quedaran desincronizados.

## Decisión

Crear el módulo Maven `common-security` (librería `jar`, **no** un microservicio) con:

- `Roles`: constantes `ADMIN`, `CLIENTE`, `REPARTIDOR` y sus variantes `ROLE_*`.
- `ProblemDetailWriter`, `JwtAuthenticationEntryPoint` y `JwtAccessDeniedHandler`.
- `ResourceServerSecurityConfig`: beans de `JwtDecoder` y del converter de autoridades.

Cada microservicio:

1. Declara la dependencia `common-security`.
2. Importa `ResourceServerSecurityConfig` desde su `SecurityConfig`.
3. Conserva **solo** su matriz de autorización (`authorizeHttpRequests`).

## Alternativas descartadas

- **Duplicar y documentar:** mantiene el costo de sincronización y no aporta autonomía real,
  porque ambos servicios ya comparten el mismo contrato de token.
- **Un microservicio de seguridad:** agrega un salto de red y un punto único de falla para algo que
  es una librería de configuración, no un servicio de dominio.

## Consecuencias

- **Positivas:** una sola definición del comportamiento 401/403 y del contrato del JWT; menos
  líneas duplicadas; los roles dejan de ser strings sueltos.
- **Negativas:** el reactor tiene un módulo más. Cuando se construyan las imágenes (S2-08), cada
  Dockerfile debe compilar con `-pl <servicio> -am` para incluir la librería.
- **Alcance:** no cambia la API pública, ni los claims, ni los códigos HTTP. Los 210 tests siguen
  pasando sin modificaciones.
