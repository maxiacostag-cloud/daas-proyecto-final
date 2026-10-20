package com.example.logictrackeralientech.repository;


import com.example.logictrackeralientech.enums.EstadoRepartidor;
import com.example.logictrackeralientech.model.Repartidor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para la gestión de la entidad Repartidor en la base de datos 'tb_repartidores'.
 * Proporciona operaciones CRUD predeterminadas y consultas derivadas según el estado del personal de entrega.
 */
@Repository
public interface RepartidorRepository extends JpaRepository<Repartidor, UUID> {

    /**
     * Consulta derivada que obtiene la lista de repartidores filtrados por su disponibilidad operativa.
     *
     * @param estado Estado del repartidor (ACTIVO o INACTIVO)
     * @return Lista de repartidores que coinciden con el estado especificado
     */
    List<Repartidor> findByEstado(EstadoRepartidor estado);
}