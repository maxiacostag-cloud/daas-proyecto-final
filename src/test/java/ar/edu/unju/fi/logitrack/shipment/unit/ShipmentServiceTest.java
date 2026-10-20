package ar.edu.unju.fi.logitrack.shipment.unit;

import ar.edu.unju.fi.logitrack.shipment.dto.envio.EnvioResponse;
import ar.edu.unju.fi.logitrack.shipment.dto.envio.RegistrarEnvioRequest;
import ar.edu.unju.fi.logitrack.shipment.entity.*;
import ar.edu.unju.fi.logitrack.shipment.entity.enums.EstadoEnvio;
import ar.edu.unju.fi.logitrack.shipment.exception.BusinessRuleException;
import ar.edu.unju.fi.logitrack.shipment.mapper.EnvioMapper;
import ar.edu.unju.fi.logitrack.shipment.repository.*;
import ar.edu.unju.fi.logitrack.shipment.service.ShipmentServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para la clase {@link ShipmentServiceImpl}.
 * <p>
 * Esta clase valida las reglas de negocio del servicio de envíos aislando
 * la capa de persistencia y mapeo mediante dobles de prueba (Mocks).
 * </p>
 */
@ExtendWith(MockitoExtension.class) // Habilita la integración del framework Mockito con JUnit 5
class ShipmentServiceTest {

    // =========================================================================
    // DEPENDENCIAS MOCKEADAS (MOCKS)
    // =========================================================================

    @Mock
    private EnvioRepository envioRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private RepartidorRepository repartidorRepository;

    @Mock
    private HistorialEstadoRepository historialEstadoRepository;

    @Mock
    private EnvioMapper envioMapper;

    // =========================================================================
    // INYECCIÓN DEL SERVICIO BAJO PRUEBA
    // =========================================================================

    /**
     * Inyecta automáticamente los Mocks declarados arriba dentro de la
     * implementación del servicio ShipmentServiceImpl.
     */
    @InjectMocks
    private ShipmentServiceImpl shipmentService;

    // Fixtures o datos de prueba compartidos
    private Cliente clienteEjemplo;
    private Envio envioEjemplo;

    /**
     * Configuración previa a la ejecución de cada prueba unitaria (@Test).
     * Prepara las entidades básicas con datos válidos para evitar duplicidad de código.
     */
    @BeforeEach
    void setUp() {
        // Inicialización de un cliente simulado
        clienteEjemplo = new Cliente("Juan Perez", "juan@gmail.com", "3881234567");

        // Creación de las direcciones de origen y destino
        Direccion origen = new Direccion("Calle 1", "123", "Jujuy", "4600");
        Direccion destino = new Direccion("Calle 2", "456", "Jujuy", "4600");

        // Creación del envío de prueba utilizando el constructor de dominio
        envioEjemplo = new Envio(
            "TRK-12345678",
            clienteEjemplo,
            origen,
            destino,
            new BigDecimal("2.500"),
            new BigDecimal("10.00"),
            new BigDecimal("10.00"),
            new BigDecimal("10.00")
        );
    }

    // =========================================================================
    // CASOS DE PRUEBA UNITARIOS
    // =========================================================================

    /**
     * Verifica que un envío se registre correctamente cuando el cliente existe
     * y los datos recibidos en el DTO son válidos.
     */
    @Test
    @DisplayName("Registrar envío exitosamente")
    void testRegistrarEnvioExitoso() {
        // GIVEN (Dado): Configuración del escenario de prueba
        UUID clienteId = UUID.randomUUID();
        RegistrarEnvioRequest request = new RegistrarEnvioRequest(
            clienteId, "Calle 1", "123", "Jujuy", "4600",
            "Calle 2", "456", "Jujuy", "4600",
            2.5, 10.0, 10.0, 10.0
        );

        // Definición de comportamientos esperados de los Mocks (Stubbing)
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(clienteEjemplo));
        when(envioRepository.save(any(Envio.class))).thenReturn(envioEjemplo);
        when(envioMapper.toResponse(any(Envio.class))).thenReturn(new EnvioResponse(
            UUID.randomUUID(), "TRK-12345678", 2.5, EstadoEnvio.PENDIENTE, clienteId, null, "Jujuy", "Jujuy"
        ));

        // WHEN (Cuando): Ejecución del método bajo prueba
        EnvioResponse response = shipmentService.registrarEnvio(request);

        // THEN (Entonces): Verificación de los resultados esperados (Aserciones)
        assertNotNull(response, "La respuesta no debe ser nula");
        assertEquals(EstadoEnvio.PENDIENTE, response.estado(), "El estado inicial del envío debe ser PENDIENTE");

        // Verificación de llamadas a las dependencias
        verify(envioRepository, times(1)).save(any(Envio.class));
    }

    /**
     * Verifica que al cancelar un envío en estado PENDIENTE, su estado cambie
     * exitosamente a CANCELADO y se persistan los cambios en la base de datos.
     */
    @Test
    @DisplayName("Cancelar envío en estado PENDIENTE modifica su estado correctamente")
    void testCancelarEnvioExitoso() {
        // GIVEN (Dado): El envío existe en la BD y está en estado PENDIENTE
        UUID envioId = UUID.randomUUID();
        when(envioRepository.findById(envioId)).thenReturn(Optional.of(envioEjemplo));
        when(envioRepository.save(any(Envio.class))).thenReturn(envioEjemplo);

        // WHEN (Cuando): Se solicita la cancelación del envío
        shipmentService.cancelarEnvio(envioId);

        // THEN (Entonces): Se valida que la entidad cambió su estado interno a CANCELADO
        assertEquals(EstadoEnvio.CANCELADO, envioEjemplo.getEstado(), "El estado debería haber cambiado a CANCELADO");

        // Se confirma que se llamó al método save del repositorio
        verify(envioRepository, times(1)).save(envioEjemplo);
    }

    /**
     * Verifica que se lance una excepción de regla de negocio (BusinessRuleException)
     * al intentar cancelar un envío que ya no está en estado PENDIENTE (por ejemplo, EN_RUTA).
     */
    @Test
    @DisplayName("Lanzar BusinessRuleException al intentar cancelar un envío que no está en PENDIENTE")
    void testCancelarEnvioInvalidoLanzaExcepcion() {
        // GIVEN (Dado): Un envío que ya posee un repartidor asignado y se encuentra EN_RUTA
        UUID envioId = UUID.randomUUID();
        Repartidor repartidor = new Repartidor("Pedro", "pedro@gmail.com", "3889999999");
        repartidor.activar();

        // Transiciones del modelo de dominio
        envioEjemplo.asignarRepartidor(repartidor);
        envioEjemplo.cambiarEstado(EstadoEnvio.EN_RUTA);

        when(envioRepository.findById(envioId)).thenReturn(Optional.of(envioEjemplo));

        // WHEN & THEN (Cuando y Entonces): Se espera que la invocación lance la excepción
        assertThrows(BusinessRuleException.class,
            () -> shipmentService.cancelarEnvio(envioId),
            "Debe lanzar BusinessRuleException al intentar cancelar un envío EN_RUTA");

        // Se verifica que NUNCA se haya guardado un cambio en el repositorio
        verify(envioRepository, never()).save(any(Envio.class));
    }
}
