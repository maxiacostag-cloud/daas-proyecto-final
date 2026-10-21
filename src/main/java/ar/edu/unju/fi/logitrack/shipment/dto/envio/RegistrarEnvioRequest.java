package ar.edu.unju.fi.logitrack.shipment.dto.envio;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record RegistrarEnvioRequest(
    @NotNull UUID clienteId,
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
    @NotNull @Positive Double largo
) {}
