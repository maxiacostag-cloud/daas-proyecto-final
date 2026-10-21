package ar.edu.unju.fi.logitrack.shipment.service;

import ar.edu.unju.fi.logitrack.shipment.dto.repartidor.RepartidorRequest;
import ar.edu.unju.fi.logitrack.shipment.dto.repartidor.RepartidorResponse;
import ar.edu.unju.fi.logitrack.shipment.entity.Repartidor;
import ar.edu.unju.fi.logitrack.shipment.entity.enums.EstadoEnvio;
import ar.edu.unju.fi.logitrack.shipment.entity.enums.EstadoRepartidor;
import ar.edu.unju.fi.logitrack.shipment.exception.BusinessRuleException;
import ar.edu.unju.fi.logitrack.shipment.exception.ResourceNotFoundException;
import ar.edu.unju.fi.logitrack.shipment.mapper.RepartidorMapper;
import ar.edu.unju.fi.logitrack.shipment.repository.EnvioRepository;
import ar.edu.unju.fi.logitrack.shipment.repository.RepartidorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Servicio de negocio para la gestión de repartidores.
 */
@Service
@Transactional(readOnly = true)
public class RepartidorService {

    private final RepartidorRepository repartidorRepository;
    private final EnvioRepository envioRepository;
    private final RepartidorMapper repartidorMapper;

    public RepartidorService(RepartidorRepository repartidorRepository,
                             EnvioRepository envioRepository,
                             RepartidorMapper repartidorMapper) {
        this.repartidorRepository = repartidorRepository;
        this.envioRepository = envioRepository;
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

    /** Para uso de otros servicios (ej. asignación de repartidor). */
    public Repartidor obtenerEntidad(UUID id) {
        return repartidorRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Repartidor", id));
    }

    /**
     * Activa o desactiva un repartidor.
     * Regla de negocio: un repartidor no puede ser desactivado si tiene envíos EN_RUTA.
     */
    @Transactional
    public RepartidorResponse cambiarEstado(UUID id, EstadoRepartidor nuevoEstado) {
        Repartidor repartidor = obtenerEntidad(id);

        if (nuevoEstado == EstadoRepartidor.INACTIVO
                && envioRepository.existsByRepartidorIdAndEstado(id, EstadoEnvio.EN_RUTA)) {
            throw new BusinessRuleException(
                    "No se puede desactivar al repartidor: tiene envíos EN_RUTA pendientes de entrega");
        }

        repartidor.cambiarEstado(nuevoEstado);
        Repartidor guardado = repartidorRepository.save(repartidor);
        return repartidorMapper.toResponse(guardado);
    }
}
