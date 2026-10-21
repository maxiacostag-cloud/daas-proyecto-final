package ar.edu.unju.fi.logitrack.shipment.dto.envio;

import ar.edu.unju.fi.logitrack.shipment.entity.enums.EstadoEnvio;
import java.time.LocalDateTime;

public record HistorialEstadoResponse(
    EstadoEnvio estadoAnterior,
    EstadoEnvio estadoNuevo,
    LocalDateTime fechaCambio,
    String observacion
) {}
