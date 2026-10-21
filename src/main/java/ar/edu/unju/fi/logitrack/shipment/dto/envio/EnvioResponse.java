package ar.edu.unju.fi.logitrack.shipment.dto.envio;


import ar.edu.unju.fi.logitrack.shipment.entity.enums.EstadoEnvio;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Datos de un envío que devuelve la API.
 *
 * @param id identificador del envío
 * @param codigoSeguimiento código que usa el cliente para seguir su paquete
 * @param peso peso real del paquete en kilos
 * @param estado estado actual del envío
 * @param clienteId id del cliente que mandó el paquete
 * @param repartidorId id del repartidor asignado, o vacío si todavía no tiene
 * @param origenCiudad ciudad desde donde sale el paquete
 * @param destinoCiudad ciudad a donde va el paquete
 * @param tarifa precio que se le cobra al cliente por el envío
 */
public record EnvioResponse(
    UUID id,
    String codigoSeguimiento,
    Double peso,
    EstadoEnvio estado,
    UUID clienteId,
    UUID repartidorId,
    String origenCiudad,
    String destinoCiudad,
    BigDecimal tarifa
) {}
