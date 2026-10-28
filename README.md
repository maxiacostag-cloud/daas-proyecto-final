daas-proyecto-final · LogiTrack
Proyecto de Desarrollo y Arquitecturas Avanzadas de Software (FI UNJu 2026) organizado como monorepo Maven multi-módulo. El Sprint 1 entregó el microservicio de envíos completo; el Sprint 2 desacopla la lógica en microservicios independientes, agrega seguridad con JWT y centraliza la configuración.
LenguajeJava 25FrameworkSpring Boot 4.0.9 · Spring Cloud 2025.1.1Base de datosMySQL 8.4 (H2 en memoria para tests)Documentaciónspringdoc-openapi (Swagger UI) + Spring Cloud ConfigTestsJUnit 5, Mockito, AssertJ, @DataJpaTest, @WebMvcTest, @SpringBootTestInfraDocker multi-stage + docker-compose (en progreso, S2-08/S2-09)
Módulos
MóduloPuertoEstadoDescripciónshipment-service8081✅ Sprint 1 migrado (S2-01)Ciclo de vida de envíos, historial de estados y asignacióncourier-service8082✅ Repartidores y disponibilidad (S2-02)Alta, consulta y disponibilidad de repartidoressecurity-service8083✅ Usuarios, roles y JWT (S2-05)Registro, login, BCrypt y emisión de JWTconfig-server8888🚧 Esqueleto (S2-04)Configuración centralizada con perfiles dev/prodcommon-security—✅ Librería (S2-06)Resource server JWT compartido: decoder HS256, roles y 401/403
S2-03 ya desacopló los repartidores: shipment-service no tiene la entidad Repartidor; guarda un UUID repartidorId y consulta la disponibilidad a courier-service por REST (RestClient).
1. Requisitos
- JDK 25 (Temurin recomendado)
- Maven 3.9+ — o usar el wrapper incluido (mvnw / mvnw.cmd), que no requiere instalación
- Docker Desktop (o Docker Engine + Compose v2), solo para la base de datos y para los servicios containerizados del Sprint 2
2. Build del proyecto completo
# Linux / macOS
./mvnw -B clean verify# Windows (PowerShell)
.\mvnw.cmd -B clean verify
El reactor compila los cuatro módulos y ejecuta toda la suite de tests. Para saltar los tests durante un build rápido:
./mvnw -B clean package -DskipTests
3. Levantar los servicios en local
   Cada microservicio se ejecuta desde su propio módulo:
   .\mvnw.cmd -pl shipment-service spring-boot:run    # http://localhost:8081
   .\mvnw.cmd -pl courier-service  spring-boot:run    # http://localhost:8082
   .\mvnw.cmd -pl security-service spring-boot:run    # http://localhost:8083
   .\mvnw.cmd -pl config-server    spring-boot:run    # http://localhost:8888
   o directamente desde el IDE ejecutando cada clase *Application.
   RecursoURLSwagger UI (shipment)http://localhost:8081/swagger-ui.htmlOpenAPI JSON (shipment)http://localhost:8081/v3/api-docsHealth (shipment)http://localhost:8081/actuator/healthConfig serverhttp://localhost:8888/shipment-service/default
   Base de datos
   docker compose up -d mysql
- MySQL en localhost:3307 · usuario logitrack / logitrack
-
- Bases: logitrack_shipments (shipment) y logitrack_couriers (courier)
- El script db/init/01-schema.sql crea el esquema de envíos solo la primera vez que se crea el volumen. Para empezar de cero:
- docker compose down -v
  Los valores por defecto se pueden pisar con variables de entorno: DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD, SERVER_PORT, CONFIG_SERVER_URL y JWT_SECRET. Ver .env.example.
  S2-06: shipment-service y courier-service validan los JWT emitidos por security-service con firma HS256 y el secreto compartido JWT_SECRET. Si se levantan a mano, los tres servicios tienen que usar el mismo valor (el default ya coincide, así que en local no hace falta configurarlo).
  docker compose up --build todavía construye la imagen única del Sprint 1: los Dockerfiles por microservicio y el compose completo son S2-08 y S2-09.
4. Tests
   ./mvnw -B test                            # todos los módulos
   ./mvnw -B -pl shipment-service test       # un módulo
   La suite no necesita MySQL ni Docker: shipment-service usa H2 en memoria (modo MySQL) y courier-service desactiva Spring Cloud Config en el perfil de test. El pipeline de GitHub Actions (.github/workflows/ci.yml) corre mvn -B verify en cada push y pull request; el resultado se ve en la pestaña Actions del repositorio.
   El mismo verify genera el reporte de cobertura JaCoCo de cada módulo (<módulo>/target/site/jacoco/index.html) y GitHub Actions lo publica como artefacto cobertura-jacoco, junto a los surefire-reports de los tests.
   Test (shipment-service)TipoQué validaEstadoEnvioTestUnitarioMatriz completa de transiciones de la máquina de estadosEnvioTestUnitarioCálculo de tarifa, cancelación, asignación, historialShipmentServiceTestUnitario (Mockito)Registro, consultas y reglas de negocio de envíosAsignacionServiceImplTestUnitario (Mockito)Asignación, reasignación, repartidor no disponible y estados de envíoCourierClientImplTestUnitario (MockRestServiceServer)Contrato HTTP con courier-service: 200, 404 y 503EnvioRepositoryTestIntegración (@DataJpaTest)Mapeo JPA, UUID, auditoría y filtro por repartidorAsignacionControllerTest / ShipmentControllerTestIntegración (@WebMvcTest)Contrato HTTP y códigos 200/201/400/404/409/422/503GestionEnviosIntegrationTestIntegración (@SpringBootTest)Flujo completo e2e sobre H2, con courier-service mockeadoFlujoCompletoHttpE2ETestIntegración E2E (@SpringBootTest + HttpServer)Registrar envío → asignar repartidor → EN_RUTA → ENTREGADO contra un courier-service falso por HTTP real (sin mockear el cliente); el courier falso exige Authorization y el test comprueba el relay del tokenShipmentSecurityTestIntegración de seguridad (@SpringBootTest, JWT reales)401 sin token / vencido / firma inválida, 403 por rol, 200/201 con token válido y endpoints públicosShipmentOwnershipTestIntegración de seguridad (@SpringBootTest, JWT reales)16 casos de pertenencia: un CLIENTE solo ve y cancela lo propio; un REPARTIDOR solo su hoja de rutaAutorizacionEnvioTestUnitario (JWT decodificado)Reglas de lectura, cancelación, cambio de estado, asignación y filtro de listado por rolShipmentServiceApplicationTests@SpringBootTestEl contexto completo levanta
   courier-service tiene su propia suite (unitarios, @DataJpaTest, @WebMvcTest e integración con H2). config-server tiene por ahora su test de contextLoads; su cobertura llega con S2-04.
   Tests de security-service (perfil test con H2, sin MySQL ni Config Server):
   TestTipoQué validaJwtUtilsTestUnitarioFirma, claims (sub, roles, repartidorId), token expirado, firma alterada y texto malformadoAuthControllerIntegrationTestIntegración (@SpringBootTest)Registro (201/409/422/400), login por username y email, 401 por credenciales o usuario inexistente, /me con token válido, sin token, inválido y expiradoSecurityServiceApplicationTests@SpringBootTestEl contexto levanta con el perfil test (H2)
   S2-07 agregó la autorización por pertenencia: un CLIENTE solo ve, cancela y asigna sus propios envíos, y un REPARTIDOR solo opera los asignados a su repartidorId. La cubren ShipmentOwnershipTest (16 casos HTTP) y AutorizacionEnvioTest (10 casos unitarios).
   Todos los tests viven dentro de su módulo (src/test/java): no quedan tests del Sprint 1 huérfanos en la raíz del reactor.
   Tests de courier-service:
   TestTipoQué validaRepartidorServiceImplTestUnitario (Mockito)Registro, listados, actualización, cambio de estado y disponibilidadRepartidorControllerTestIntegración (@WebMvcTest)Contrato HTTP de los seis endpoints (200/201/400/404/409)RepartidorRepositoryTestIntegración (@DataJpaTest)Mapeo JPA, UUID, auditoría, email únicoGestionRepartidoresIntegrationTestIntegración (@SpringBootTest)Flujo completo y disponibilidad sobre H2CourierSecurityTestIntegración de seguridad (@SpringBootTest, JWT reales)401 sin token / vencido / firma inválida, 403 al gestionar repartidores sin ADMIN, consultas autenticadas y endpoints públicos
5. Estructura del repositorio
   daas-proyecto-final
   ├── pom.xml                  agregador: Spring Boot 4.0.9 + Spring Cloud 2025.1.1
   ├── common-security/         librería compartida del resource server JWT (S2-06)
   │   └── src/main/java/ar/edu/unju/fi/logitrack/common/security
   ├── config-server/           Spring Cloud Config Server
   ├── shipment-service/        envíos (código del Sprint 1)
   │   └── src/main/java/ar/edu/unju/fi/logitrack/shipment
   │       ├── client/          cliente REST hacia courier-service (RestClient + disponibilidad)
   │       ├── config/          JPA auditing, OpenAPI y matriz de autorización
   │       ├── controller/      REST controllers (solo reciben/devuelven DTOs)
   │       ├── dto/             records de request/response (dto/envio, dto/asignacion, dto/courier)
   │       ├── entity/          entidades JPA + enums + Value Object Direccion
   │       ├── exception/       excepciones de dominio + GlobalExceptionHandler (RFC 7807)
   │       ├── mapper/          conversión entidad ⇄ DTO
   │       ├── repository/      Spring Data JPA
   │       ├── security/        identidad del JWT y autorización por pertenencia (S2-07)
   │       └── service/         contratos de la lógica de aplicación
   │           └── impl/        implementaciones (@Service) con las transacciones
   ├── courier-service/         repartidores + disponibilidad (puerto 8082)
   ├── security-service/        autenticación y JWT (S2-05)
   ├── db/init/                 esquema y datos iniciales de MySQL
   └── http/                    requests de demo para IntelliJ / REST Client
   El código del Sprint 1 se movió de src/ a shipment-service/src/ en S2-01, sin cambios de paquete (ar.edu.unju.fi.logitrack.shipment).
   Modelo de dominio (shipment-service)

repartidorId es solo un UUID: no hay relación JPA ni FK. El repartidor (nombre, vehículo, estado) vive en courier-service, que es el dueño de esos datos.
Los cambios respecto del modelo guía del PDF están explicados en la sección 8.
6. API (shipment-service)
   Base: /api/v1. Errores en formato application/problem+json: 400 (validación / formato), 404 (no existe), 409 (dato duplicado), 422 (regla de negocio) y 503 (courier-service no disponible).
   MétodoEndpointDescripciónRol mínimoPUT/envios/{id}/repartidorAsignar repartidor { "repartidorId": "..." }; consulta courier-serviceADMIN o CLIENTEPOST/enviosRegistrar envío (cliente, origen, destino, peso, medidas)ADMIN o CLIENTEGET/envios/{id}Consultar por idautenticado (pertenencia)GET/envios/seguimiento/{codigo}Consultar por código de seguimientoautenticado (pertenencia)GET/envios?estado=PENDIENTE&repartidorId=...Listar por estado y/o por repartidor asignado (hoja de ruta); el CLIENTE solo ve lo propio y el REPARTIDOR su hoja de rutaautenticado (pertenencia)PATCH/envios/{id}/estadoCambiar estado { "estado": "EN_RUTA" }ADMIN o repartidor asignadoPOST/envios/{id}/cancelacionCancelar (solo PENDIENTE)ADMIN o cliente dueñoGET/envios/{id}/historialHistorial de estadosautenticado (pertenencia)
   Seguridad (S2-06)
   shipment-service y courier-service son resource servers: no emiten tokens, solo validan los JWT que emite security-service.
- Firma: HS256 con el secreto compartido JWT_SECRET (mismo default en los tres módulos, así que en local funciona sin configuración extra).
- Roles: el claim roles del token (["ADMIN"], ["CLIENTE"], ["REPARTIDOR"]) se traduce a autoridades ROLE_*.
- Endpoints públicos (sin token): /actuator/health, /actuator/info, /v3/api-docs/**, /swagger-ui/** y /error.
- Resto: requiere Authorization: Bearer <token>. Las respuestas de seguridad también son ProblemDetail (401 con título No autorizado, 403 con Acceso denegado).
- Token relay: cuando shipment-service llama a courier-service (disponibilidad del repartidor) reenvía el Authorization del request original, de modo que la llamada interna conserva la identidad del usuario y courier sigue exigiendo token.
1. #Obtener el token en security-service

curl -X POST http://localhost:8083/api/v1/auth/login \
-H "Content-Type: application/json" \
-d '{"usernameOrEmail":"admin.demo","password":"admin123"}'# 2. Usarlo en shipment o courier
curl http://localhost:8081/api/v1/envios -H "Authorization: Bearer <token>"
Autorización por pertenencia (S2-07)
El rol no alcanza: además se verifica que el recurso pertenezca al usuario autenticado. Las reglas viven en security/AutorizacionEnvio (capa de servicio), así valen para todos los puntos de entrada, y una violación devuelve 403 con ProblemDetail (Acceso denegado).
RolLectura (detalle, seguimiento, historial, listado)CancelarCambiar estadoAsignar repartidorADMINTodos los envíosSíSíSíCLIENTESolo los de su clienteSolo los propiosNoSolo los propiosREPARTIDORSolo los asignados a élNoSolo los asignados a élNo
Identidad del CLIENTE. El sub del JWT (UUID del usuario de security-service) se guarda en clientes.usuario_id. Al registrar un envío, el cliente que se crea —o que se reutiliza por email— queda vinculado a ese usuario, y a partir de ahí solo el dueño puede verlo, cancelarlo o asignarle repartidor. Si el cliente ya pertenece a otro usuario, el registro responde 403.
Simplificación: el primer CLIENTE que usa un email sin dueño reclama el registro. Alcanza para el alcance del sprint; un modelo completo necesitaría un alta de clientes explícita en security-service.
Identidad del REPARTIDOR. El token de un repartidor incluye el claim repartidorId, que se compara con envios.repartidor_id. El listado (GET /envios) queda atado a la hoja de ruta propia: pedir ?repartidorId=<otro> devuelve 403, y un token sin el claim tampoco puede listar.
# CLIENTE: solo ve sus propios envíos
curl http://localhost:8081/api/v1/envios -H "Authorization: Bearer <token-cliente>"# REPARTIDOR: solo los asignados a él
curl http://localhost:8081/api/v1/envios -H "Authorization: Bearer <token-repartidor>"# Envío de otro cliente → 403
curl -i http://localhost:8081/api/v1/envios/<id-ajeno> -H "Authorization: Bearer <token-cliente>"
Los tests que cubren la matriz son AutorizacionEnvioTest (unitario, por rol), ShipmentOwnershipTest (HTTP con JWT reales y H2) y ShipmentSecurityTest (401/403 por rol).
El CRUD de repartidores no vive acá: está en courier-service (ver más abajo). shipment-service solo guarda el UUID del repartidor asignado y consulta su disponibilidad por REST.
Demo de endpoints
Los archivos http/envios.http (ciclo de vida del envío), http/repartidores.http (asignación desde shipment) y http/couriers.http (alta y estado de repartidores) tienen los requests listos para ejecutar desde IntelliJ IDEA o VS Code (extensión REST Client). Para probar la asignación tienen que estar levantados shipment-service (8081) y courier-service (8082). http/pertenencia.http recorre el flujo completo de S2-07: alta y login de dos clientes y un repartidor, y los 403 por pertenencia.
1. Asignación exitosa: PUT /api/v1/envios/{envioId}/repartidor con repartidor activo y envío en PENDIENTE (200 OK).
2. Reasignación: sobre un envío que ya tiene repartidor pero sigue PENDIENTE (200 OK).
3. Rechazo por inactivo: repartidor en INACTIVO (422, lo informa courier-service).
4. Rechazo por estado de envío: envío en EN_RUTA o CANCELADO (422).
5. Repartidor inexistente: courier-service responde 404 y shipment lo propaga (404).
6. courier-service caído: shipment responde 503.
   API de courier-service (8082)
   MétodoEndpointDescripciónRol mínimoPOST/api/v1/repartidoresRegistrar repartidor (queda ACTIVO)ADMINGET/api/v1/repartidores?estado=ACTIVOListar repartidores; filtro opcional por estadoautenticadoGET/api/v1/repartidores/{id}Detalle de un repartidorautenticadoPUT/api/v1/repartidores/{id}Actualizar nombre, vehículo, email y teléfonoADMINPATCH/api/v1/repartidores/{id}/estadoActivar o desactivarADMINGET/api/v1/repartidores/{id}/disponibilidadDisponibilidad para shipment-serviceautenticado
   Respuesta del endpoint de disponibilidad:
   { "repartidorId": "3f6c1a52-8e0b-4d7a-9c11-0a1b2c3d4e01", "estado": "ACTIVO", "disponible": true }
   disponible es true solo si el repartidor está ACTIVO. Demo: http/couriers.http.
   Limitación conocida: courier-service no conoce los envíos, así que no puede impedir que se desactive un repartidor que tiene un envío EN_RUTA. La validación que existía en el shipment-service monolítico se pierde en este desacople; si se necesita, hay que resolverla con un evento o con una consulta de shipment a courier (follow-up).
7. Reglas de negocio (shipment-service)
- Flujo: PENDIENTE → EN_RUTA → ENTREGADO, o PENDIENTE → CANCELADO. Cualquier otra transición → 422.
- Solo se cancela en PENDIENTE.
- Para pasar a EN_RUTA el envío debe tener repartidorId; solo se asignan repartidores que courier-service informa como disponibles a envíos PENDIENTE.
- La asignación consulta GET /api/v1/repartidores/{id}/disponibilidad con courier.service.url (COURIER_SERVICE_URL, por defecto http://localhost:8082). Un 404 remoto se traduce a 404 y un error de conexión o 5xx a 503.
- Tarifa (provisoria, a validar con el equipo): 1500 + 450 × máx(peso real, peso volumétrico), con peso volumétrico = alto × ancho × largo / 5000. Las constantes están en Envio.
8. Modelo de dominio — cambios respecto del modelo guía
   CambioMotivoBaseEntity con id UUID, createdAt, updatedAt y versionRequisito de UUID + auditoría en todas las entidades; @Version evita actualizaciones perdidasNueva entidad HistorialEstadoTrazabilidad de cada cambio de estado (estado anterior, nuevo, fecha/hora)BigDecimal en lugar de Double para peso, medidas y tarifaEvitar errores de redondeo con dineroTipoVehiculo enum (en courier-service)Valores consistentes y validablesDireccion como @Embeddable inmutableValue Object sin identidad; se guarda en columnas origen_* / destino_*Máquina de estados en EstadoEnvio.transicionesPermitidas()Una sola fuente de verdad para las transicionesEnvio sin setters: cambiarEstado(), cancelar(), asignarRepartidor()Las reglas no se pueden saltear; cada cambio de estado agrega historial automáticamenteCódigo de seguimiento LT-XXXXXXXXXX (TrackingCodeGenerator)Legible, sin caracteres ambiguos
9. Flujo de trabajo en Git
- main: versión entregable, protegida (solo pull requests).
- develop: integración del sprint.
- feature/<sprint>-<issue>-<descripcion-corta> → pull request hacia develop, con al menos una revisión.
- Commits estilo Conventional Commits: feat: ..., fix: ..., test: ..., docs: ..., chore: ....
- Una rama por issue; el número de issue va en el commit (Closes #17) para cerrarla al mergear.
  Issues del Sprint 2
  IssueTareaRamaS2-01Migrar el código del Sprint 1 al módulo shipment-service y unificar el buildfeature/s2-01-migrar-shipment-serviceS2-02courier-service: repartidores y endpoint de disponibilidadfeature/s2-02-courier-serviceS2-03shipment-service: quitar Repartidor y cliente REST hacia courierfeature/s2-03-shipment-rest-courierS2-04config-server: configuración centralizada con perfiles dev/prodfeature/s2-04-config-serverS2-05security-service: usuarios, roles, BCrypt y JWTfeature/s2-05-security-serviceS2-06Proteger endpoints con resource server JWTfeature/s2-06-resource-serverS2-07Autorización por pertenencia (CLIENTE / REPARTIDOR)feature/s2-07-autorizacion-pertenenciaS2-08Dockerfiles multietapa por microserviciofeature/s2-08-dockerfilesS2-09docker-compose completo (4 servicios + MySQL)feature/s2-09-composeS2-10Publicar las imágenes en Docker Hubfeature/s2-10-docker-hubS2-11Tests integrales y CI multi-módulofeature/s2-11-tests-ciS2-12Demo E2E y README finalfeature/s2-12-demo-readme
10. Créditos
- Brian Fabalos — estructura multi-módulo Maven y esqueletos base de shipment-service, courier-service, security-service y config-server (commit 464e276, punto de partida del Sprint 2).
- Facundo Arancibia — migración del Sprint 1 a shipment-service, unificación de Spring Boot y Spring Cloud, y suite de tests del microservicio de envíos.
- Maximiliano Acosta — coordinación del repositorio, tablero de issues y despliegue.
