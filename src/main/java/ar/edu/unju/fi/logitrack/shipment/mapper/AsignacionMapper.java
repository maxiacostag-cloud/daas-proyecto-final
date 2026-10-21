package ar.edu.unju.fi.logitrack.shipment.mapper;

import ar.edu.unju.fi.logitrack.shipment.dto.repartidor.AsignacionResponse;
import ar.edu.unju.fi.logitrack.shipment.entity.Envio;
import org.springframework.stereotype.Component;

/**
 * Mapeador para armar la respuesta de la asignación de un repartidor a un envío.
 */
@Component
public class AsignacionMapper {

    private final RepartidorMapper repartidorMapper;

    public AsignacionMapper(RepartidorMapper repartidorMapper) {
        this.repartidorMapper = repartidorMapper;
    }

    public AsignacionResponse toResponse(Envio envio) {
        return new AsignacionResponse(
                envio.getId(),
                envio.getCodigoSeguimiento(),
                envio.getEstado(),
                repartidorMapper.toResponse(envio.getRepartidor())
        );
    }
}
