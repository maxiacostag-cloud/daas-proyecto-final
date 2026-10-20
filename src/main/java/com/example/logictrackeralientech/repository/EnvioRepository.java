package com.example.logictrackeralientech.repository;

import com.example.logictrackeralientech.enums.EstadoEnvio;
import com.example.logictrackeralientech.model.Envio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para la gestión de persistencia de la entidad Envio.
 */
@Repository
public interface EnvioRepository extends JpaRepository<Envio, UUID> {

    /** Busca un envío por su código único de seguimiento público */
    Optional<Envio> findByCodigoSeguimiento(String codigoSeguimiento);

    /** Lista todos los envíos que se encuentran en un estado determinado */
    List<Envio> findByEstado(EstadoEnvio estado);

    /** Lista los envíos realizados por un cliente específico */
    List<Envio> findByClienteId(UUID clienteId);
}