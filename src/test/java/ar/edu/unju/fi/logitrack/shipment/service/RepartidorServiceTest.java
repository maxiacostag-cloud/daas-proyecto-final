package ar.edu.unju.fi.logitrack.shipment.service;

import ar.edu.unju.fi.logitrack.shipment.TestDataFactory;
import ar.edu.unju.fi.logitrack.shipment.dto.repartidor.RepartidorRequest;
import ar.edu.unju.fi.logitrack.shipment.dto.repartidor.RepartidorResponse;
import ar.edu.unju.fi.logitrack.shipment.entity.Repartidor;
import ar.edu.unju.fi.logitrack.shipment.entity.enums.EstadoEnvio;
import ar.edu.unju.fi.logitrack.shipment.entity.enums.EstadoRepartidor;
import ar.edu.unju.fi.logitrack.shipment.entity.enums.TipoVehiculo;
import ar.edu.unju.fi.logitrack.shipment.exception.BusinessRuleException;
import ar.edu.unju.fi.logitrack.shipment.exception.ResourceNotFoundException;
import ar.edu.unju.fi.logitrack.shipment.mapper.RepartidorMapper;
import ar.edu.unju.fi.logitrack.shipment.repository.EnvioRepository;
import ar.edu.unju.fi.logitrack.shipment.repository.RepartidorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** Test unitario de servicio con repositorios mockeados. */
@ExtendWith(MockitoExtension.class)
class RepartidorServiceTest {

    @Mock
    private RepartidorRepository repartidorRepository;

    @Mock
    private EnvioRepository envioRepository;

    private RepartidorService repartidorService;

    @BeforeEach
    void setUp() {
        repartidorService = new RepartidorService(repartidorRepository, envioRepository, new RepartidorMapper());
    }

    @Test
    @DisplayName("registrar: guarda repartidor activo y devuelve DTO")
    void registrar_guardaRepartidorActivoYDevuelveDto() {
        UUID idGenerado = UUID.randomUUID();
        when(repartidorRepository.save(any(Repartidor.class))).thenAnswer(invocation -> {
            Repartidor repartidor = invocation.getArgument(0);
            ReflectionTestUtils.setField(repartidor, "id", idGenerado);
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
    @DisplayName("listar sin filtro: devuelve todos los repartidores")
    void listar_sinFiltro_devuelveTodos() {
        when(repartidorRepository.findAllByOrderByNombreAsc())
                .thenReturn(List.of(TestDataFactory.repartidorActivo(), TestDataFactory.repartidorInactivo()));

        List<RepartidorResponse> resultado = repartidorService.listar(null);

        assertThat(resultado).hasSize(2);
        verify(repartidorRepository, never()).findByEstadoOrderByNombreAsc(any());
    }

    @Test
    @DisplayName("listar con filtro: consulta solo los repartidores del estado solicitado")
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
    @DisplayName("obtenerPorId: lanza 404 si el repartidor no existe")
    void obtenerPorId_inexistente_lanzaNotFound() {
        UUID id = UUID.randomUUID();
        when(repartidorRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> repartidorService.obtenerPorId(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    @DisplayName("cambiarEstado: desactiva un repartidor sin envíos EN_RUTA")
    void cambiarEstado_desactivarRepartidorSinEnviosEnRuta_actualizaAInactivo() {
        UUID id = UUID.randomUUID();
        Repartidor repartidor = TestDataFactory.repartidorActivo();
        when(repartidorRepository.findById(id)).thenReturn(Optional.of(repartidor));
        when(envioRepository.existsByRepartidorIdAndEstado(id, EstadoEnvio.EN_RUTA)).thenReturn(false);
        when(repartidorRepository.save(any(Repartidor.class))).thenAnswer(inv -> inv.getArgument(0));

        RepartidorResponse respuesta = repartidorService.cambiarEstado(id, EstadoRepartidor.INACTIVO);

        assertThat(respuesta.estado()).isEqualTo(EstadoRepartidor.INACTIVO);
        assertThat(repartidor.getEstado()).isEqualTo(EstadoRepartidor.INACTIVO);
    }

    @Test
    @DisplayName("cambiarEstado: rechaza desactivar a un repartidor con envíos EN_RUTA")
    void cambiarEstado_desactivarRepartidorConEnviosEnRuta_lanzaBusinessRuleException() {
        UUID id = UUID.randomUUID();
        Repartidor repartidor = TestDataFactory.repartidorActivo();
        when(repartidorRepository.findById(id)).thenReturn(Optional.of(repartidor));
        when(envioRepository.existsByRepartidorIdAndEstado(id, EstadoEnvio.EN_RUTA)).thenReturn(true);

        assertThatThrownBy(() -> repartidorService.cambiarEstado(id, EstadoRepartidor.INACTIVO))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("EN_RUTA");

        assertThat(repartidor.getEstado()).isEqualTo(EstadoRepartidor.ACTIVO);
        verify(repartidorRepository, never()).save(any());
    }

    @Test
    @DisplayName("cambiarEstado: reactivar un repartidor no verifica envíos")
    void cambiarEstado_reactivarRepartidor_actualizaAActivo() {
        UUID id = UUID.randomUUID();
        Repartidor repartidor = TestDataFactory.repartidorInactivo();
        when(repartidorRepository.findById(id)).thenReturn(Optional.of(repartidor));
        when(repartidorRepository.save(any(Repartidor.class))).thenAnswer(inv -> inv.getArgument(0));

        RepartidorResponse respuesta = repartidorService.cambiarEstado(id, EstadoRepartidor.ACTIVO);

        assertThat(respuesta.estado()).isEqualTo(EstadoRepartidor.ACTIVO);
        verifyNoInteractions(envioRepository);
    }

    @Test
    @DisplayName("cambiarEstado: lanza 404 si el repartidor no existe")
    void cambiarEstado_repartidorInexistente_lanzaNotFound() {
        UUID id = UUID.randomUUID();
        when(repartidorRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> repartidorService.cambiarEstado(id, EstadoRepartidor.INACTIVO))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
