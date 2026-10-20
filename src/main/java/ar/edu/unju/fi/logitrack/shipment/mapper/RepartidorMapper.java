package ar.edu.unju.fi.logitrack.shipment.mapper;

import ar.edu.unju.fi.logitrack.shipment.dto.repartidor.RepartidorRequest;
import ar.edu.unju.fi.logitrack.shipment.dto.repartidor.RepartidorResponse;
import ar.edu.unju.fi.logitrack.shipment.entity.Repartidor;
import org.springframework.stereotype.Component;

/** Convierte entre la entidad JPA y los DTOs de la API (la entidad nunca sale del servicio). */
@Component
public class RepartidorMapper {

    public Repartidor toEntity(RepartidorRequest request) {
        return new Repartidor(request.nombre().trim(), request.tipoVehiculo());
    }

    public RepartidorResponse toResponse(Repartidor repartidor) {
        return new RepartidorResponse(
                repartidor.getId(),
                repartidor.getNombre(),
                repartidor.getTipoVehiculo(),
                repartidor.getEstado(),
                repartidor.getCreatedAt(),
                repartidor.getUpdatedAt());
    }
}
