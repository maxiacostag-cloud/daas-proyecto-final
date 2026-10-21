package ar.edu.unju.fi.logitrack.shipment.repository;

import ar.edu.unju.fi.logitrack.shipment.entity.Envio;
import ar.edu.unju.fi.logitrack.shipment.entity.enums.EstadoEnvio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnvioRepository extends JpaRepository<Envio, UUID> {

    Optional<Envio> findByCodigoSeguimiento(String codigoSeguimiento);

    boolean existsByCodigoSeguimiento(String codigoSeguimiento);

    List<Envio> findByEstadoOrderByCreatedAtDesc(EstadoEnvio estado);

    boolean existsByRepartidorIdAndEstado(UUID repartidorId, EstadoEnvio estado);
}
