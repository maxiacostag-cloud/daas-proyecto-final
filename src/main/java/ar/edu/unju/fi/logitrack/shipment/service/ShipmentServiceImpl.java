package ar.edu.unju.fi.logitrack.shipment.service;

import ar.edu.unju.fi.logitrack.shipment.dto.envio.*;
import ar.edu.unju.fi.logitrack.shipment.entity.*;
import ar.edu.unju.fi.logitrack.shipment.entity.enums.EstadoEnvio;
import ar.edu.unju.fi.logitrack.shipment.exception.BusinessRuleException;
import ar.edu.unju.fi.logitrack.shipment.exception.ResourceNotFoundException;
import ar.edu.unju.fi.logitrack.shipment.mapper.EnvioMapper;
import ar.edu.unju.fi.logitrack.shipment.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class ShipmentServiceImpl implements ShipmentService {

    private final EnvioRepository envioRepository;
    private final ClienteRepository clienteRepository;
    private final RepartidorRepository repartidorRepository;
    private final HistorialEstadoRepository historialEstadoRepository;
    private final EnvioMapper envioMapper;
    private final TrackingCodeGenerator trackingCodeGenerator;

    public ShipmentServiceImpl(EnvioRepository envioRepository,
                               ClienteRepository clienteRepository,
                               RepartidorRepository repartidorRepository,
                               HistorialEstadoRepository historialEstadoRepository,
                               EnvioMapper envioMapper,
                               TrackingCodeGenerator trackingCodeGenerator) {
        this.envioRepository = envioRepository;
        this.clienteRepository = clienteRepository;
        this.repartidorRepository = repartidorRepository;
        this.historialEstadoRepository = historialEstadoRepository;
        this.envioMapper = envioMapper;
        this.trackingCodeGenerator = trackingCodeGenerator;
    }

    @Override
    @Transactional
    public EnvioResponse registrarEnvio(RegistrarEnvioRequest request) {
        Cliente cliente = obtenerOCrearCliente(request);

        Direccion origen = new Direccion(request.origenCalle(), request.origenNumero(), request.origenCiudad(), request.origenCodigoPostal());
        Direccion destino = new Direccion(request.destinoCalle(), request.destinoNumero(), request.destinoCiudad(), request.destinoCodigoPostal());

        String codigoSeguimiento = generarCodigoUnico();

        // Construcción mediante el constructor de dominio (calcula tarifa e historial internamente)
        Envio envio = new Envio(
            codigoSeguimiento,
            cliente,
            origen,
            destino,
            BigDecimal.valueOf(request.peso()),
            BigDecimal.valueOf(request.alto()),
            BigDecimal.valueOf(request.ancho()),
            BigDecimal.valueOf(request.largo())
        );

        Envio guardado = envioRepository.save(envio);
        return envioMapper.toResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public EnvioResponse obtenerPorId(UUID id) {
        Envio envio = envioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Envío no encontrado con ID: " + id));
         return envioMapper.toResponse(envio);
    }

    @Override
    @Transactional(readOnly = true)
    public EnvioResponse obtenerPorCodigoSeguimiento(String codigo) {
        Envio envio = envioRepository.findByCodigoSeguimiento(codigo)
            .orElseThrow(() -> new ResourceNotFoundException("Envío no encontrado con código: " + codigo));
        return envioMapper.toResponse(envio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnvioResponse> listarPorEstado(EstadoEnvio estado) {
        List<Envio> envios = (estado != null) ? envioRepository.findByEstado(estado) : envioRepository.findAll();
        return envios.stream().map(envioMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public EnvioResponse cambiarEstado(UUID id, CambiarEstadoRequest request) {
        Envio envio = envioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Envío no encontrado con ID: " + id));

        // Invocación del método de dominio que valida la transición y registra el historial
        envio.cambiarEstado(request.estado());
        Envio actualizado = envioRepository.save(envio);

        return envioMapper.toResponse(actualizado);
    }

    @Override
    @Transactional
    public EnvioResponse cancelarEnvio(UUID id) {
        Envio envio = envioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Envío no encontrado con ID: " + id));

        // Invocación de regla de negocio encapsulated
        envio.cancelar();
        Envio actualizado = envioRepository.save(envio);

        return envioMapper.toResponse(actualizado);
    }

    @Override
    @Transactional
    public EnvioResponse asignarRepartidor(UUID id, AsignarRepartidorRequest request) {
        Envio envio = envioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Envío no encontrado con ID: " + id));

        Repartidor repartidor = repartidorRepository.findById(request.repartidorId())
            .orElseThrow(() -> new ResourceNotFoundException("Repartidor no encontrado con ID: " + request.repartidorId()));

        // Invocación de asignación encapsulada (comprueba estado ACTIVO y estado PENDIENTE)
        envio.asignarRepartidor(repartidor);
        Envio actualizado = envioRepository.save(envio);

        return envioMapper.toResponse(actualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialEstadoResponse> obtenerHistorial(UUID id) {
        if (!envioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Envío no encontrado con ID: " + id);
        }
        return historialEstadoRepository.findByEnvioIdOrderByFechaHoraDesc(id)
            .stream()
            .map(envioMapper::toHistorialResponse)
            .toList();
    }

    /**
     * Busca el cliente al que pertenece el envío.
     * <p>
     * Si el pedido trae el {@code clienteId}, se busca ese cliente. Si en cambio trae los datos del cliente,
     * primero se busca por email para no cargarlo dos veces y, si no existe, se registra uno nuevo.
     * </p>
     *
     * @param request pedido de registro del envío
     * @return el cliente del envío
     * @throws ResourceNotFoundException si se mandó un clienteId que no existe
     * @throws BusinessRuleException si no se mandó ni el clienteId ni los datos del cliente
     */
    private Cliente obtenerOCrearCliente(RegistrarEnvioRequest request) {
        if (request.clienteId() != null) {
            return clienteRepository.findById(request.clienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + request.clienteId()));
        }
        DatosClienteRequest datos = request.cliente();
        if (datos == null) {
            throw new BusinessRuleException("Para registrar un envío hay que indicar el clienteId o los datos del cliente.");
        }
        String email = datos.email().trim();
        return clienteRepository.findByEmailIgnoreCase(email)
            .orElseGet(() -> clienteRepository.save(new Cliente(datos.nombre().trim(), email, datos.telefono())));
    }

    /**
     * Pide códigos de seguimiento al {@link TrackingCodeGenerator} hasta conseguir uno
     * que no lo tenga ningún otro envío.
     *
     * @return un código de seguimiento que no se repite
     */
    private String generarCodigoUnico() {
        String codigo;
        do {
            codigo = trackingCodeGenerator.generar();
        } while (envioRepository.existsByCodigoSeguimiento(codigo));
        return codigo;
    }
}
