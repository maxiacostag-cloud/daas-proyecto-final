package ar.edu.unju.fi.logitrack.courierservice.controller;

import ar.edu.unju.fi.logitrack.courierservice.dto.repartidor.CambioEstadoRepartidorRequest;
import ar.edu.unju.fi.logitrack.courierservice.dto.repartidor.DisponibilidadResponse;
import ar.edu.unju.fi.logitrack.courierservice.dto.repartidor.RepartidorRequest;
import ar.edu.unju.fi.logitrack.courierservice.dto.repartidor.RepartidorResponse;
import ar.edu.unju.fi.logitrack.courierservice.entity.enums.EstadoRepartidor;
import ar.edu.unju.fi.logitrack.courierservice.service.RepartidorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * API REST del módulo de repartidores, en el puerto 8082.
 * <p>
 * Además del CRUD, expone {@code GET /{id}/disponibilidad}, que shipment-service consume por REST
 * para validar si un repartidor puede recibir un envío.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/repartidores")
@Tag(name = "Repartidores", description = "Alta, consulta y disponibilidad del personal de entrega")
@RequiredArgsConstructor
public class RepartidorController {

    private final RepartidorService repartidorService;

    @PostMapping
    @Operation(summary = "Registrar un repartidor (queda ACTIVO)")
    @ApiResponse(responseCode = "201", description = "Repartidor creado exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos de solicitud inválidos o incompletos")
    @ApiResponse(responseCode = "409", description = "Ya existe un repartidor con ese email")
    public ResponseEntity<RepartidorResponse> registrar(@Valid @RequestBody RepartidorRequest request) {
        RepartidorResponse creado = repartidorService.registrar(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.id())
                .toUri();

        return ResponseEntity.created(location).body(creado);
    }

    @GetMapping
    @Operation(summary = "Listar repartidores, opcionalmente filtrando por estado (disponibles con ?estado=ACTIVO)")
    @ApiResponse(responseCode = "200", description = "Lista de repartidores")
    public List<RepartidorResponse> listar(
            @Parameter(description = "Filtro opcional: ACTIVO o INACTIVO") @RequestParam(required = false) EstadoRepartidor estado) {
        return repartidorService.listar(estado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener el detalle de un repartidor por su ID")
    @ApiResponse(responseCode = "200", description = "Repartidor encontrado")
    @ApiResponse(responseCode = "404", description = "No existe un repartidor con el ID especificado")
    public ResponseEntity<RepartidorResponse> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(repartidorService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar los datos de un repartidor (nombre, vehículo, email y teléfono)")
    @ApiResponse(responseCode = "200", description = "Repartidor actualizado")
    @ApiResponse(responseCode = "400", description = "Datos de solicitud inválidos o incompletos")
    @ApiResponse(responseCode = "404", description = "No existe el repartidor")
    @ApiResponse(responseCode = "409", description = "El email ya pertenece a otro repartidor")
    public ResponseEntity<RepartidorResponse> actualizar(
            @PathVariable UUID id, @Valid @RequestBody RepartidorRequest request) {
        return ResponseEntity.ok(repartidorService.actualizar(id, request));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Activar o desactivar un repartidor",
            description = "Un repartidor INACTIVO no puede recibir envíos.")
    @ApiResponse(responseCode = "200", description = "Estado actualizado")
    @ApiResponse(responseCode = "400", description = "Estado inválido")
    @ApiResponse(responseCode = "404", description = "No existe el repartidor")
    public ResponseEntity<RepartidorResponse> cambiarEstado(
            @PathVariable UUID id, @Valid @RequestBody CambioEstadoRepartidorRequest request) {
        return ResponseEntity.ok(repartidorService.cambiarEstado(id, request.estado()));
    }

    @GetMapping("/{id}/disponibilidad")
    @Operation(summary = "Consultar si un repartidor puede recibir envíos",
            description = "Endpoint remoto para shipment-service: devuelve 200 con disponible=true/false y 404 si no existe.")
    @ApiResponse(responseCode = "200", description = "Disponibilidad consultada")
    @ApiResponse(responseCode = "404", description = "No existe un repartidor con el ID especificado")
    public ResponseEntity<DisponibilidadResponse> disponibilidad(@PathVariable UUID id) {
        return ResponseEntity.ok(repartidorService.consultarDisponibilidad(id));
    }
}
