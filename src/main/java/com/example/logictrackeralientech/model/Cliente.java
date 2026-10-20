package com.example.logictrackeralientech.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Entidad JPA que representa a un Cliente dentro del sistema LogiTrack.
 * Mapea la tabla 'tb_clientes'.
 */
@Entity
@Table(name = "tb_clientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    /** Identificador único global de tipo UUID generado automáticamente */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Nombre completo o Razón Social del cliente */
    @Column(nullable = false)
    private String nombre;

    /** Correo electrónico único para notificaciones */
    @Column(nullable = false, unique = true)
    private String email;

    /** Teléfono de contacto */
    @Column(nullable = false)
    private String telefono;
}