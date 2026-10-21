package ar.edu.unju.fi.logitrack.shipment.controller;

import ar.edu.unju.fi.logitrack.shipment.dto.envio.*;
import ar.edu.unju.fi.logitrack.shipment.entity.enums.EstadoEnvio;
import ar.edu.unju.fi.logitrack.shipment.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST encargado de exponer las operaciones del módulo de envíos bajo /api/v1/envios.
 */
@RestController
@RequestMapping("/api/v1/envios")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    /**
     * POST /api/v1/envios - Registrar un nuevo envío.
     */
    @PostMapping
    public ResponseEntity<EnvioResponse> registrarEnvio(@Valid @RequestBody RegistrarEnvioRequest request) {
        EnvioResponse nuevoEnvio = shipmentService.registrarEnvio(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoEnvio);
    }

    /**
     * GET /api/v1/envios/{id} - Consultar un envío por su ID (UUID).
     */
    @GetMapping("/{id}")
    public ResponseEntity<EnvioResponse> obtenerPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(shipmentService.obtenerPorId(id));
    }

    /**
     * GET /api/v1/envios/seguimiento/{codigo} - Consultar un envío por su código público de seguimiento.
     */
    @GetMapping("/seguimiento/{codigo}")
    public ResponseEntity<EnvioResponse> obtenerPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(shipmentService.obtenerPorCodigoSeguimiento(codigo));
    }

    /**
     * GET /api/v1/envios?estado=PENDIENTE - Listar envíos (filtrado opcional por estado).
     */
    @GetMapping
    public ResponseEntity<List<EnvioResponse>> listarPorEstado(@RequestParam(required = false) EstadoEnvio estado) {
        return ResponseEntity.ok(shipmentService.listarPorEstado(estado));
    }

    /**
     * PATCH /api/v1/envios/{id}/estado - Cambiar el estado de un envío.
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<EnvioResponse> cambiarEstado(
        @PathVariable UUID id,
        @Valid @RequestBody CambiarEstadoRequest request) {
        return ResponseEntity.ok(shipmentService.cambiarEstado(id, request));
    }

    /**
     * POST /api/v1/envios/{id}/cancelacion - Cancelar un envío (solo si está en PENDIENTE).
     */
    @PostMapping("/{id}/cancelacion")
    public ResponseEntity<EnvioResponse> cancelarEnvio(@PathVariable UUID id) {
        return ResponseEntity.ok(shipmentService.cancelarEnvio(id));
    }

    /**
     * PUT /api/v1/envios/{id}/repartidor - Asignar un repartidor a un envío.
     */
    @PutMapping("/{id}/repartidor")
    public ResponseEntity<EnvioResponse> asignarRepartidor(
        @PathVariable UUID id,
        @Valid @RequestBody AsignarRepartidorRequest request) {
        return ResponseEntity.ok(shipmentService.asignarRepartidor(id, request));
    }

    /**
     * GET /api/v1/envios/{id}/historial - Consultar el historial cronológico de cambios de estado.
     */
    @GetMapping("/{id}/historial")
    public ResponseEntity<List<HistorialEstadoResponse>> obtenerHistorial(@PathVariable UUID id) {
        return ResponseEntity.ok(shipmentService.obtenerHistorial(id));
    }
}
