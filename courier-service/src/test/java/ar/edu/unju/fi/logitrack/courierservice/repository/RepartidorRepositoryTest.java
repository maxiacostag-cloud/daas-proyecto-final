package ar.edu.unju.fi.logitrack.courierservice.repository;

import ar.edu.unju.fi.logitrack.courierservice.TestDataFactory;
import ar.edu.unju.fi.logitrack.courierservice.config.JpaAuditingConfig;
import ar.edu.unju.fi.logitrack.courierservice.entity.Repartidor;
import ar.edu.unju.fi.logitrack.courierservice.entity.enums.EstadoRepartidor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class RepartidorRepositoryTest {

    @Autowired
    private RepartidorRepository repartidorRepository;

    @Test
    void guardar_asignaUuidYCamposDeAuditoria() {
        Repartidor guardado = repartidorRepository.saveAndFlush(TestDataFactory.repartidorActivo());

        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getCreatedAt()).isNotNull();
        assertThat(guardado.getUpdatedAt()).isNotNull();
        assertThat(guardado.getVersion()).isZero();
    }

    @Test
    void findByEstado_devuelveSoloLosDelEstadoPedido() {
        repartidorRepository.save(TestDataFactory.repartidorActivo());
        repartidorRepository.save(TestDataFactory.repartidorInactivo());

        List<Repartidor> activos = repartidorRepository.findByEstadoOrderByNombreAsc(EstadoRepartidor.ACTIVO);

        assertThat(activos).singleElement()
                .extracting(Repartidor::getEstado)
                .isEqualTo(EstadoRepartidor.ACTIVO);
    }

    @Test
    void existsByEmail_ignoraMayusculas() {
        Repartidor guardado = repartidorRepository.save(TestDataFactory.repartidorActivo());

        assertThat(repartidorRepository.existsByEmailIgnoreCase(guardado.getEmail().toUpperCase())).isTrue();
        assertThat(repartidorRepository.existsByEmailIgnoreCase("nadie@logitrack.test")).isFalse();
    }

    @Test
    void existsByEmailAndIdNot_permiteElPropioEmail() {
        Repartidor guardado = repartidorRepository.save(TestDataFactory.repartidorActivo());

        assertThat(repartidorRepository.existsByEmailIgnoreCaseAndIdNot(guardado.getEmail(), guardado.getId())).isFalse();
        assertThat(repartidorRepository.existsByEmailIgnoreCaseAndIdNot(guardado.getEmail(), UUID.randomUUID())).isTrue();
    }
}
