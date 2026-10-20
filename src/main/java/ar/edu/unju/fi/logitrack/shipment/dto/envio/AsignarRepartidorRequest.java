package ar.edu.unju.fi.logitrack.shipment.dto.envio;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AsignarRepartidorRequest(
    @NotNull UUID repartidorId
) {}
