package ar.edu.unju.fi.logitrack.shipment.dto.envio;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

/**
 * Datos que se reciben para registrar un envío nuevo.
 * <p>
 * El cliente se puede indicar de dos formas: con el {@code clienteId} de un cliente que ya existe,
 * o mandando sus datos en {@code cliente}. Si llegan los dos, se usa el {@code clienteId}.
 * </p>
 *
 * @param clienteId id de un cliente ya registrado, puede venir vacío si se mandan los datos del cliente
 * @param origenCalle calle de origen
 * @param origenNumero número de la calle de origen
 * @param origenCiudad ciudad de origen
 * @param origenCodigoPostal código postal de origen
 * @param destinoCalle calle de destino
 * @param destinoNumero número de la calle de destino
 * @param destinoCiudad ciudad de destino
 * @param destinoCodigoPostal código postal de destino
 * @param peso peso real del paquete en kilos
 * @param ancho ancho del paquete en centímetros
 * @param alto alto del paquete en centímetros
 * @param largo largo del paquete en centímetros
 * @param cliente datos del cliente, se usan cuando no se manda el clienteId
 */
public record RegistrarEnvioRequest(
    @Schema(description = "Id de un cliente ya registrado. Puede quedar vacío si se mandan los datos del cliente.")
    UUID clienteId,
    @NotNull String origenCalle,
    @NotNull String origenNumero,
    @NotNull String origenCiudad,
    @NotNull String origenCodigoPostal,
    @NotNull String destinoCalle,
    @NotNull String destinoNumero,
    @NotNull String destinoCiudad,
    @NotNull String destinoCodigoPostal,
    @NotNull @Positive Double peso,
    @NotNull @Positive Double ancho,
    @NotNull @Positive Double alto,
    @NotNull @Positive Double largo,
    @Schema(description = "Datos del cliente. Se usan cuando no se manda el clienteId.")
    @Valid DatosClienteRequest cliente
) {

    /**
     * Crea el pedido solo con el {@code clienteId}, sin los datos del cliente.
     * <p>
     * Lo dejamos para que el código que ya registraba envíos de esta forma (por ejemplo los tests)
     * siga funcionando igual que antes.
     * </p>
     */
    public RegistrarEnvioRequest(UUID clienteId,
                                 String origenCalle, String origenNumero, String origenCiudad, String origenCodigoPostal,
                                 String destinoCalle, String destinoNumero, String destinoCiudad, String destinoCodigoPostal,
                                 Double peso, Double ancho, Double alto, Double largo) {
        this(clienteId, origenCalle, origenNumero, origenCiudad, origenCodigoPostal,
            destinoCalle, destinoNumero, destinoCiudad, destinoCodigoPostal,
            peso, ancho, alto, largo, null);
    }
}
