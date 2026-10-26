package ar.edu.unju.fi.logitrack.courierservice.repository;

import ar.edu.unju.fi.logitrack.courierservice.entity.Repartidor;
import ar.edu.unju.fi.logitrack.courierservice.entity.enums.EstadoRepartidor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RepartidorRepository extends JpaRepository<Repartidor, UUID> {

    List<Repartidor> findByEstadoOrderByNombreAsc(EstadoRepartidor estado);

    List<Repartidor> findAllByOrderByNombreAsc();

    boolean existsByEmailIgnoreCase(String email);

    /** Para actualizar: el email puede repetirse solo si pertenece al mismo repartidor. */
    boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);
}
