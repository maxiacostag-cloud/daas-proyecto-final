package com.example.logictrackeralientech.repository;

import com.example.logictrackeralientech.model.AuditoriaEnvio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para consultar la trazabilidad y auditoría de envíos.
 */
@Repository
public interface AuditoriaEnvioRepository extends JpaRepository<AuditoriaEnvio, UUID> {

    /** Obtiene el historial de auditoría de un envío ordenado en forma descendente por fecha */
    List<AuditoriaEnvio> findByEnvioIdOrderByFechaCambioDesc(UUID envioId);
}