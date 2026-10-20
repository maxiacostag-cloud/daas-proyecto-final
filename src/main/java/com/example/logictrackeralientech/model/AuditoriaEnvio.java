package com.example.logictrackeralientech.model;


import com.example.logictrackeralientech.enums.EstadoEnvio;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad de Auditoría para almacenar la trazabilidad e historial de cambios de estado.
 * Mapea la tabla 'tb_auditoria_envios'.
 */
@Entity
@Table(name = "tb_auditoria_envios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditoriaEnvio {

    /** Clave primaria UUID */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Referencia al envío auditado */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "envio_id", nullable = false)
    private Envio envio;

    /** Estado previo a la transición (puede ser null en el registro inicial) */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior")
    private EstadoEnvio estadoAnterior;

    /** Nuevo estado asignado tras la transición */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false)
    private EstadoEnvio estadoNuevo;

    /** Marca temporal exacta de la transacción */
    @Column(nullable = false)
    private LocalDateTime fechaCambio;

    /** Observación o motivo del cambio de estado */
    @Column(nullable = false)
    private String observacion;
}