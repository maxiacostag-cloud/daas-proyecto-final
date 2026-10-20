package ar.edu.unju.fi.logitrack.shipment.dto.repartidor;

import ar.edu.unju.fi.logitrack.shipment.entity.enums.TipoVehiculo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para registrar un repartidor")
public record RepartidorRequest(

        @Schema(example = "Juan Pérez")
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
        String nombre,

        @Schema(example = "MOTO")
        @NotNull(message = "El tipo de vehículo es obligatorio")
        TipoVehiculo tipoVehiculo
) {
}
