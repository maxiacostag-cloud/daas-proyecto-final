package com.example.logictrackeralientech.dto;

import com.example.logictrackeralientech.model.Direccion;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.UUID;

/**
 * DTO de entrada para la solicitud de creación de un nuevo envío.
 * Incluye anotaciones de Jakarta Validation.
 */
@Data
public class EnvioRequestDto {

    @NotNull(message = "El ID del cliente es obligatorio")
    private UUID clienteId;

    @NotNull(message = "La dirección de origen es obligatoria")
    @Valid
    private Direccion origen;

    @NotNull(message = "La dirección de destino es obligatoria")
    @Valid
    private Direccion destino;

    @NotNull(message = "El peso es obligatorio")
    @Positive(message = "El peso debe ser un valor positivo mayor a cero")
    private Double peso;

    @NotNull(message = "El ancho es obligatorio")
    @Positive(message = "El ancho debe ser mayor a cero")
    private Double ancho;

    @NotNull(message = "El alto es obligatorio")
    @Positive(message = "El alto debe ser mayor a cero")
    private Double alto;

    @NotNull(message = "El largo es obligatorio")
    @Positive(message = "El largo debe ser mayor a cero")
    private Double largo;
}