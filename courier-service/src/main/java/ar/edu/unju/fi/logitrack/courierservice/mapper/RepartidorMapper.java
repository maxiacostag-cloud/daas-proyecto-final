package ar.edu.unju.fi.logitrack.courierservice.mapper;

import ar.edu.unju.fi.logitrack.courierservice.dto.repartidor.DisponibilidadResponse;
import ar.edu.unju.fi.logitrack.courierservice.dto.repartidor.RepartidorRequest;
import ar.edu.unju.fi.logitrack.courierservice.dto.repartidor.RepartidorResponse;
import ar.edu.unju.fi.logitrack.courierservice.entity.Repartidor;
import org.springframework.stereotype.Component;

/** Convierte entre la entidad JPA y los DTOs de la API (la entidad nunca sale del servicio). */
@Component
public class RepartidorMapper {

    public Repartidor toEntity(RepartidorRequest request) {
        return new Repartidor(
                request.nombre().trim(),
                request.tipoVehiculo(),
                request.email().trim(),
                request.telefono().trim());
    }

    public RepartidorResponse toResponse(Repartidor repartidor) {
        return new RepartidorResponse(
                repartidor.getId(),
                repartidor.getNombre(),
                repartidor.getTipoVehiculo(),
                repartidor.getEmail(),
                repartidor.getTelefono(),
                repartidor.getEstado(),
                repartidor.getCreatedAt(),
                repartidor.getUpdatedAt());
    }

    public DisponibilidadResponse toDisponibilidad(Repartidor repartidor) {
        return new DisponibilidadResponse(
                repartidor.getId(),
                repartidor.getEstado(),
                repartidor.estaActivo());
    }
}
