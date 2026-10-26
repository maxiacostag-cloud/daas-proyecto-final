package ar.edu.unju.fi.logitrack.courierservice.dto.repartidor;

import ar.edu.unju.fi.logitrack.courierservice.entity.enums.EstadoRepartidor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Respuesta del endpoint de disponibilidad que consume shipment-service antes de asignar un envío.
 * <p>
 * {@code disponible} es {@code true} solo si el repartidor está {@code ACTIVO}. Las reglas que dependen
 * de los envíos (por ejemplo, no desactivar a alguien con un envío {@code EN_RUTA}) viven en
 * shipment-service, que es quien conoce esa información.
 * </p>
 */
@Schema(description = "Disponibilidad de un repartidor para recibir envíos")
public record DisponibilidadResponse(
        UUID repartidorId,
        EstadoRepartidor estado,
        boolean disponible
) {
}
