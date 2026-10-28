package ar.edu.unju.fi.logitrack.courierservice.dto.repartidor;

import ar.edu.unju.fi.logitrack.courierservice.entity.enums.EstadoRepartidor;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Petición para cambiar el estado de un repartidor")
public record CambioEstadoRepartidorRequest(
        @Schema(description = "Nuevo estado del repartidor", example = "INACTIVO")
        @NotNull(message = "El estado es obligatorio")
        EstadoRepartidor estado
) {
}
