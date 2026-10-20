package ar.edu.unju.fi.logitrack.shipment.service;

import ar.edu.unju.fi.logitrack.shipment.TestDataFactory;
import ar.edu.unju.fi.logitrack.shipment.dto.repartidor.RepartidorRequest;
import ar.edu.unju.fi.logitrack.shipment.dto.repartidor.RepartidorResponse;
import ar.edu.unju.fi.logitrack.shipment.entity.Repartidor;
import ar.edu.unju.fi.logitrack.shipment.entity.enums.EstadoRepartidor;
import ar.edu.unju.fi.logitrack.shipment.entity.enums.TipoVehiculo;
import ar.edu.unju.fi.logitrack.shipment.exception.ResourceNotFoundException;
import ar.edu.unju.fi.logitrack.shipment.mapper.RepartidorMapper;
import ar.edu.unju.fi.logitrack.shipment.repository.RepartidorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Ejemplo de test unitario de servicio: el repositorio es un mock, no hay base de datos. */
@ExtendWith(MockitoExtension.class)
class RepartidorServiceTest {

    @Mock
    private RepartidorRepository repartidorRepository;

    private RepartidorService repartidorService;

    @BeforeEach
    void setUp() {
        repartidorService = new RepartidorService(repartidorRepository, new RepartidorMapper());
    }

    @Test
    void registrar_guardaRepartidorActivoYDevuelveDto() {
        UUID idGenerado = UUID.randomUUID();
        when(repartidorRepository.save(any(Repartidor.class))).thenAnswer(invocation -> {
            Repartidor repartidor = invocation.getArgument(0);
            ReflectionTestUtils.setField(repartidor, "id", idGenerado); // simula el id que asigna JPA
            return repartidor;
        });

        RepartidorResponse respuesta = repartidorService.registrar(new RepartidorRequest("  Ana Torres  ", TipoVehiculo.AUTO));

        ArgumentCaptor<Repartidor> captor = ArgumentCaptor.forClass(Repartidor.class);
        verify(repartidorRepository).save(captor.capture());
        assertThat(captor.getValue().getNombre()).isEqualTo("Ana Torres");
        assertThat(captor.getValue().getEstado()).isEqualTo(EstadoRepartidor.ACTIVO);

        assertThat(respuesta.id()).isEqualTo(idGenerado);
        assertThat(respuesta.tipoVehiculo()).isEqualTo(TipoVehiculo.AUTO);
        assertThat(respuesta.estado()).isEqualTo(EstadoRepartidor.ACTIVO);
    }

    @Test
    void listar_sinFiltro_devuelveTodos() {
        when(repartidorRepository.findAllByOrderByNombreAsc())
                .thenReturn(List.of(TestDataFactory.repartidorActivo(), TestDataFactory.repartidorInactivo()));

        List<RepartidorResponse> resultado = repartidorService.listar(null);

        assertThat(resultado).hasSize(2);
        verify(repartidorRepository, never()).findByEstadoOrderByNombreAsc(any());
    }

    @Test
    void listar_conFiltro_consultaPorEstado() {
        when(repartidorRepository.findByEstadoOrderByNombreAsc(EstadoRepartidor.ACTIVO))
                .thenReturn(List.of(TestDataFactory.repartidorActivo()));

        List<RepartidorResponse> resultado = repartidorService.listar(EstadoRepartidor.ACTIVO);

        assertThat(resultado).singleElement()
                .extracting(RepartidorResponse::estado)
                .isEqualTo(EstadoRepartidor.ACTIVO);
        verify(repartidorRepository, never()).findAllByOrderByNombreAsc();
    }

    @Test
    void obtenerPorId_inexistente_lanzaNotFound() {
        UUID id = UUID.randomUUID();
        when(repartidorRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> repartidorService.obtenerPorId(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(id.toString());
    }
}
