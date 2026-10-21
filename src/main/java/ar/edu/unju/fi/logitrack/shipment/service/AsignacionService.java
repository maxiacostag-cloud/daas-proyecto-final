package ar.edu.unju.fi.logitrack.shipment.service;

import ar.edu.unju.fi.logitrack.shipment.dto.repartidor.AsignacionResponse;
import ar.edu.unju.fi.logitrack.shipment.entity.Envio;
import ar.edu.unju.fi.logitrack.shipment.entity.Repartidor;
import ar.edu.unju.fi.logitrack.shipment.exception.ResourceNotFoundException;
import ar.edu.unju.fi.logitrack.shipment.mapper.AsignacionMapper;
import ar.edu.unju.fi.logitrack.shipment.repository.EnvioRepository;
import ar.edu.unju.fi.logitrack.shipment.repository.RepartidorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso: asignación de repartidores a envíos.
 */
@Service
@Transactional(readOnly = true)
public class AsignacionService {

    private final EnvioRepository envioRepository;
    private final RepartidorRepository repartidorRepository;
    private final AsignacionMapper asignacionMapper;

    public AsignacionService(EnvioRepository envioRepository,
                             RepartidorRepository repartidorRepository,
                             AsignacionMapper asignacionMapper) {
        this.envioRepository = envioRepository;
        this.repartidorRepository = repartidorRepository;
        this.asignacionMapper = asignacionMapper;
    }

    /**
     * Asigna un repartidor a un envío.
     * El envío debe estar PENDIENTE y el repartidor ACTIVO (validado por el agregado Envio).
     * Si el envío ya tenía un repartidor asignado, queda reasignado al nuevo.
     */
    @Transactional
    public AsignacionResponse asignarRepartidor(UUID envioId, UUID repartidorId) {
        Envio envio = envioRepository.findById(envioId)
                .orElseThrow(() -> ResourceNotFoundException.of("Envio", envioId));
        Repartidor repartidor = repartidorRepository.findById(repartidorId)
                .orElseThrow(() -> ResourceNotFoundException.of("Repartidor", repartidorId));

        envio.asignarRepartidor(repartidor);
        Envio guardado = envioRepository.save(envio);

        return asignacionMapper.toResponse(guardado);
    }
}
