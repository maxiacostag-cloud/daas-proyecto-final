package ar.edu.unju.fi.logitrack.courierservice.service.impl;

import ar.edu.unju.fi.logitrack.courierservice.TestDataFactory;
import ar.edu.unju.fi.logitrack.courierservice.dto.repartidor.DisponibilidadResponse;
import ar.edu.unju.fi.logitrack.courierservice.dto.repartidor.RepartidorRequest;
import ar.edu.unju.fi.logitrack.courierservice.dto.repartidor.RepartidorResponse;
import ar.edu.unju.fi.logitrack.courierservice.entity.Repartidor;
import ar.edu.unju.fi.logitrack.courierservice.entity.enums.EstadoRepartidor;
import ar.edu.unju.fi.logitrack.courierservice.entity.enums.TipoVehiculo;
import ar.edu.unju.fi.logitrack.courierservice.exception.DuplicateResourceException;
import ar.edu.unju.fi.logitrack.courierservice.exception.ResourceNotFoundException;
import ar.edu.unju.fi.logitrack.courierservice.mapper.RepartidorMapper;
import ar.edu.unju.fi.logitrack.courierservice.repository.RepartidorRepository;
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
import static org.mockito.Mockito.when;

/** Test unitario de servicio con el repositorio mockeado. */
@ExtendWith(MockitoExtension.class)
class RepartidorServiceImplTest {

    @Mock
    private RepartidorRepository repartidorRepository;

    private RepartidorServiceImpl repartidorService;

    @BeforeEach
    void setUp() {
        repartidorService = new RepartidorServiceImpl(repartidorRepository, new RepartidorMapper());
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

        RepartidorResponse respuesta = repartidorService.registrar(
                new RepartidorRequest("  Ana Torres  ", TipoVehiculo.AUTO, "  Ana.Torres@Logitrack.test ", " +54 388 400-5000 "));

        ArgumentCaptor<Repartidor> captor = ArgumentCaptor.forClass(Repartidor.class);
        verify(repartidorRepository).save(captor.capture());
        assertThat(captor.getValue().getNombre()).isEqualTo("Ana Torres");
        assertThat(captor.getValue().getEstado()).isEqualTo(EstadoRepartidor.ACTIVO);
        assertThat(captor.getValue().getEmail()).isEqualTo("ana.torres@logitrack.test");
        assertThat(captor.getValue().getTelefono()).isEqualTo("+54 388 400-5000");

        assertThat(respuesta.id()).isEqualTo(idGenerado);
        assertThat(respuesta.tipoVehiculo()).isEqualTo(TipoVehiculo.AUTO);
        assertThat(respuesta.estado()).isEqualTo(EstadoRepartidor.ACTIVO);
    }

    @Test
    @DisplayName("registrar: rechaza un email que ya usa otro repartidor")
    void registrar_emailDuplicado_lanzaConflicto() {
        when(repartidorRepository.existsByEmailIgnoreCase("ana@logitrack.test")).thenReturn(true);

        assertThatThrownBy(() -> repartidorService.registrar(
                new RepartidorRequest("Ana Torres", TipoVehiculo.AUTO, "ana@logitrack.test", "+54 388 400-5000")))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("ana@logitrack.test");

        verify(repartidorRepository, never()).save(any());
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
    @DisplayName("obtenerPorId: devuelve el repartidor existente")
    void obtenerPorId_existente_devuelveDto() {
        UUID id = UUID.randomUUID();
        Repartidor repartidor = TestDataFactory.repartidorActivo();
        ReflectionTestUtils.setField(repartidor, "id", id);
        when(repartidorRepository.findById(id)).thenReturn(Optional.of(repartidor));

        RepartidorResponse respuesta = repartidorService.obtenerPorId(id);

        assertThat(respuesta.id()).isEqualTo(id);
        assertThat(respuesta.nombre()).isEqualTo("Lucía Fernández");
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
    @DisplayName("actualizar: reemplaza nombre, vehículo, email y teléfono")
    void actualizar_cambiaLosDatos() {
        Repartidor repartidor = TestDataFactory.repartidorActivo();
        UUID id = UUID.randomUUID();
        when(repartidorRepository.findById(id)).thenReturn(Optional.of(repartidor));
        when(repartidorRepository.save(repartidor)).thenReturn(repartidor);

        RepartidorResponse respuesta = repartidorService.actualizar(id,
                new RepartidorRequest("Lucía F. Díaz", TipoVehiculo.AUTO, "Lucia.Diaz@logitrack.test", "+54 388 400-7000"));

        assertThat(repartidor.getNombre()).isEqualTo("Lucía F. Díaz");
        assertThat(repartidor.getTipoVehiculo()).isEqualTo(TipoVehiculo.AUTO);
        assertThat(repartidor.getEmail()).isEqualTo("lucia.diaz@logitrack.test");
        assertThat(respuesta.telefono()).isEqualTo("+54 388 400-7000");
    }

    @Test
    @DisplayName("actualizar: rechaza un email que pertenece a otro repartidor")
    void actualizar_emailDeOtro_lanzaConflicto() {
        UUID id = UUID.randomUUID();
        when(repartidorRepository.findById(id)).thenReturn(Optional.of(TestDataFactory.repartidorActivo()));
        when(repartidorRepository.existsByEmailIgnoreCaseAndIdNot("otro@logitrack.test", id)).thenReturn(true);

        assertThatThrownBy(() -> repartidorService.actualizar(id,
                new RepartidorRequest("Lucía", TipoVehiculo.MOTO, "otro@logitrack.test", "+54 388 400-7000")))
                .isInstanceOf(DuplicateResourceException.class);

        verify(repartidorRepository, never()).save(any());
    }

    @Test
    @DisplayName("actualizar: lanza 404 si el repartidor no existe")
    void actualizar_inexistente_lanzaNotFound() {
        UUID id = UUID.randomUUID();
        when(repartidorRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> repartidorService.actualizar(id,
                new RepartidorRequest("Lucía", TipoVehiculo.MOTO, "lucia@logitrack.test", "+54 388 400-7000")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("cambiarEstado: desactiva un repartidor")
    void cambiarEstado_desactivar_actualizaAInactivo() {
        UUID id = UUID.randomUUID();
        Repartidor repartidor = TestDataFactory.repartidorActivo();
        when(repartidorRepository.findById(id)).thenReturn(Optional.of(repartidor));
        when(repartidorRepository.save(any(Repartidor.class))).thenAnswer(inv -> inv.getArgument(0));

        RepartidorResponse respuesta = repartidorService.cambiarEstado(id, EstadoRepartidor.INACTIVO);

        assertThat(respuesta.estado()).isEqualTo(EstadoRepartidor.INACTIVO);
        assertThat(repartidor.getEstado()).isEqualTo(EstadoRepartidor.INACTIVO);
    }

    @Test
    @DisplayName("cambiarEstado: reactiva un repartidor")
    void cambiarEstado_reactivar_actualizaAActivo() {
        UUID id = UUID.randomUUID();
        Repartidor repartidor = TestDataFactory.repartidorInactivo();
        when(repartidorRepository.findById(id)).thenReturn(Optional.of(repartidor));
        when(repartidorRepository.save(any(Repartidor.class))).thenAnswer(inv -> inv.getArgument(0));

        RepartidorResponse respuesta = repartidorService.cambiarEstado(id, EstadoRepartidor.ACTIVO);

        assertThat(respuesta.estado()).isEqualTo(EstadoRepartidor.ACTIVO);
    }

    @Test
    @DisplayName("cambiarEstado: lanza 404 si el repartidor no existe")
    void cambiarEstado_repartidorInexistente_lanzaNotFound() {
        UUID id = UUID.randomUUID();
        when(repartidorRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> repartidorService.cambiarEstado(id, EstadoRepartidor.INACTIVO))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("disponibilidad: un repartidor ACTIVO está disponible")
    void disponibilidad_activo_devuelveDisponible() {
        UUID id = UUID.randomUUID();
        Repartidor repartidor = TestDataFactory.repartidorActivo();
        ReflectionTestUtils.setField(repartidor, "id", id);
        when(repartidorRepository.findById(id)).thenReturn(Optional.of(repartidor));

        DisponibilidadResponse respuesta = repartidorService.consultarDisponibilidad(id);

        assertThat(respuesta.repartidorId()).isEqualTo(id);
        assertThat(respuesta.estado()).isEqualTo(EstadoRepartidor.ACTIVO);
        assertThat(respuesta.disponible()).isTrue();
    }

    @Test
    @DisplayName("disponibilidad: un repartidor INACTIVO no está disponible")
    void disponibilidad_inactivo_noEstaDisponible() {
        UUID id = UUID.randomUUID();
        Repartidor repartidor = TestDataFactory.repartidorInactivo();
        ReflectionTestUtils.setField(repartidor, "id", id);
        when(repartidorRepository.findById(id)).thenReturn(Optional.of(repartidor));

        DisponibilidadResponse respuesta = repartidorService.consultarDisponibilidad(id);

        assertThat(respuesta.estado()).isEqualTo(EstadoRepartidor.INACTIVO);
        assertThat(respuesta.disponible()).isFalse();
    }

    @Test
    @DisplayName("disponibilidad: lanza 404 si el repartidor no existe")
    void disponibilidad_inexistente_lanzaNotFound() {
        UUID id = UUID.randomUUID();
        when(repartidorRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> repartidorService.consultarDisponibilidad(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
