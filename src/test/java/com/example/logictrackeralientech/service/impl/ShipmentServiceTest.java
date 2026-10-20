package com.example.logictrackeralientech.service.impl;
import com.example.logictrackeralientech.model.Envio;
import com.example.logictrackeralientech.enums.EstadoEnvio;
import com.example.logictrackeralientech.repository.AuditoriaEnvioRepository;
import com.example.logictrackeralientech.repository.EnvioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Suite de Pruebas Unitarias con JUnit 5 y Mockito para la capa de servicio ShipmentServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @Mock
    private EnvioRepository envioRepository;

    @Mock
    private AuditoriaEnvioRepository auditoriaRepository;

    @InjectMocks
    private ShipmentServiceImpl shipmentService;

    private Envio envioPendiente;
    private UUID envioId;

    @BeforeEach
    void setUp() {
        envioId = UUID.randomUUID();
        envioPendiente = Envio.builder()
                .id(envioId)
                .codigoSeguimiento("TRK-TEST1234")
                .peso(2.5)
                .ancho(10.0)
                .alto(10.0)
                .largo(10.0)
                .estado(EstadoEnvio.PENDIENTE)
                .build();
    }

    @Test
    @DisplayName("Debe calcular correctamente la tarifa según peso y dimensiones")
    void testCalcularTarifa() {
        // Base ($1000) + (2.5kg * $200) + (1000cm3 * 0.01) = 1000 + 500 + 10 = 1510.00
        Double tarifaCalculada = envioPendiente.calcularTarifa();
        assertEquals(1510.00, tarifaCalculada, 0.01);
    }

    @Test
    @DisplayName("Debe permitir cancelar un envío cuando está en estado PENDIENTE")
    void testCancelarEnvioExitoso() {
        when(envioRepository.findById(envioId)).thenReturn(Optional.of(envioPendiente));
        when(envioRepository.save(any(Envio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        shipmentService.cancelarEnvio(envioId);

        assertEquals(EstadoEnvio.CANCELADO, envioPendiente.getEstado());
        verify(auditoriaRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción al intentar pasar de PENDIENTE a ENTREGADO directamente")
    void testTransicionInvalidaLanzaExcepcion() {
        when(envioRepository.findById(envioId)).thenReturn(Optional.of(envioPendiente));

        assertThrows(IllegalStateException.class, () -> {
            shipmentService.cambiarEstado(envioId, EstadoEnvio.ENTREGADO, "Intento directo invalido");
        });
    }
}