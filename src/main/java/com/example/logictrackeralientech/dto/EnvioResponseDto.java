package com.example.logictrackeralientech.dto;


import com.example.logictrackeralientech.enums.EstadoEnvio;
import com.example.logictrackeralientech.model.Direccion;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * DTO de salida para exponer la información de un envío hacia la API REST sin comprometer la Entidad JPA.
 */
@Data
@Builder
public class EnvioResponseDto {

    private UUID id;                  // ID único del envío
    private String codigoSeguimiento; // Código público TRK-XXXX
    private Double peso;              // Peso en kg
    private Double tarifa;            // Tarifa final calculada
    private EstadoEnvio estado;       // Estado actual
    private String clienteNombre;     // Nombre del cliente solicitante
    private String repartidorNombre;  // Nombre del repartidor asignado
    private Direccion origen;         // Dirección de origen
    private Direccion destino;        // Dirección de destino
}