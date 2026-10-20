package ar.edu.unju.fi.logitrack.shipment.service;

import ar.edu.unju.fi.logitrack.shipment.dto.repartidor.RepartidorRequest;
import ar.edu.unju.fi.logitrack.shipment.dto.repartidor.RepartidorResponse;
import ar.edu.unju.fi.logitrack.shipment.entity.Repartidor;
import ar.edu.unju.fi.logitrack.shipment.entity.enums.EstadoRepartidor;
import ar.edu.unju.fi.logitrack.shipment.exception.ResourceNotFoundException;
import ar.edu.unju.fi.logitrack.shipment.mapper.RepartidorMapper;
import ar.edu.unju.fi.logitrack.shipment.repository.RepartidorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Slice de referencia: muestra el patrón Controller → Service → Repository con DTOs
 * que el resto de los módulos (envíos, estados, asignación) debería seguir.
 */
@Service
@Transactional(readOnly = true)
public class RepartidorService {

    private final RepartidorRepository repartidorRepository;
    private final RepartidorMapper repartidorMapper;

    public RepartidorService(RepartidorRepository repartidorRepository, RepartidorMapper repartidorMapper) {
        this.repartidorRepository = repartidorRepository;
        this.repartidorMapper = repartidorMapper;
    }

    @Transactional
    public RepartidorResponse registrar(RepartidorRequest request) {
        Repartidor guardado = repartidorRepository.save(repartidorMapper.toEntity(request));
        return repartidorMapper.toResponse(guardado);
    }

    /** @param estado filtro opcional; si es null devuelve todos. */
    public List<RepartidorResponse> listar(EstadoRepartidor estado) {
        List<Repartidor> repartidores = (estado == null)
                ? repartidorRepository.findAllByOrderByNombreAsc()
                : repartidorRepository.findByEstadoOrderByNombreAsc(estado);
        return repartidores.stream().map(repartidorMapper::toResponse).toList();
    }

    public RepartidorResponse obtenerPorId(UUID id) {
        return repartidorMapper.toResponse(obtenerEntidad(id));
    }

    /** Para uso de otros servicios (ej. asignación de repartidor en ShipmentService). */
    public Repartidor obtenerEntidad(UUID id) {
        return repartidorRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Repartidor", id));
    }
}
