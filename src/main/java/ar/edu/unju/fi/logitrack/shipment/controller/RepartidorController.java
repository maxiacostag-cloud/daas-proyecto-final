package ar.edu.unju.fi.logitrack.shipment.controller;

import ar.edu.unju.fi.logitrack.shipment.dto.repartidor.CambioEstadoRepartidorRequest;
import ar.edu.unju.fi.logitrack.shipment.dto.repartidor.RepartidorRequest;
import ar.edu.unju.fi.logitrack.shipment.dto.repartidor.RepartidorResponse;
import ar.edu.unju.fi.logitrack.shipment.entity.enums.EstadoRepartidor;
import ar.edu.unju.fi.logitrack.shipment.service.RepartidorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/repartidores")
@Tag(name = "Repartidores", description = "Alta y consulta del personal de entrega")
public class RepartidorController {

    private final RepartidorService repartidorService;

    public RepartidorController(RepartidorService repartidorService) {
        this.repartidorService = repartidorService;
    }

    @PostMapping
    @Operation(summary = "Registrar un repartidor (queda ACTIVO)")
    @ApiResponse(responseCode = "201", description = "Repartidor creado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
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
    @Operation(summary = "Obtener el detalle de un repartidor")
    @ApiResponse(responseCode = "200", description = "Repartidor encontrado")
    @ApiResponse(responseCode = "404", description = "No existe un repartidor con ese id")
    public RepartidorResponse obtener(@PathVariable UUID id) {
        return repartidorService.obtenerPorId(id);
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Activar o desactivar un repartidor",
            description = "Un repartidor INACTIVO no puede recibir envíos. No se puede desactivar con envíos EN_RUTA.")
    @ApiResponse(responseCode = "200", description = "Estado actualizado")
    @ApiResponse(responseCode = "400", description = "Estado inválido")
    @ApiResponse(responseCode = "404", description = "No existe el repartidor")
    @ApiResponse(responseCode = "422", description = "El repartidor tiene envíos EN_RUTA")
    public ResponseEntity<RepartidorResponse> cambiarEstado(
            @PathVariable UUID id, @Valid @RequestBody CambioEstadoRepartidorRequest request) {
        return ResponseEntity.ok(repartidorService.cambiarEstado(id, request.estado()));
    }
}
