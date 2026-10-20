package ar.edu.unju.fi.logitrack.shipment.dto.envio;


import ar.edu.unju.fi.logitrack.shipment.entity.enums.EstadoEnvio;
import java.util.UUID;

public record EnvioResponse(
    UUID id,
    String codigoSeguimiento,
    Double peso,
    EstadoEnvio estado,
    UUID clienteId,
    UUID repartidorId,
    String origenCiudad,
    String destinoCiudad
) {}
