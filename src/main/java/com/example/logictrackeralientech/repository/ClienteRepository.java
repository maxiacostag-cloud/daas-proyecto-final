package com.example.logictrackeralientech.repository;

import com.example.logictrackeralientech.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para la entidad Cliente (tb_clientes).
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, UUID> {

    /** Busca un cliente por su dirección de correo electrónico */
    Optional<Cliente> findByEmail(String email);
}