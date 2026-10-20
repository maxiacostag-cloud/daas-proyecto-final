package com.example.logictrackeralientech.model;


import com.example.logictrackeralientech.enums.EstadoRepartidor;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Entidad JPA que representa al personal operativo de reparto.
 * Mapea la tabla 'tb_repartidores'.
 */
@Entity
@Table(name = "tb_repartidores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Repartidor {

    /** Identificador único global del repartidor */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Nombre y Apellido del repartidor */
    @Column(nullable = false)
    private String nombre;

    /** Tipo de vehículo asignado (ej: Moto, Camioneta, Bicicleta) */
    @Column(nullable = false)
    private String tipoVehiculo;

    /** Estado actual de disponibilidad del repartidor (ACTIVO / INACTIVO) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoRepartidor estado;
}