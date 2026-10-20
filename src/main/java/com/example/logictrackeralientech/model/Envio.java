package com.example.logictrackeralientech.model;

import com.example.logictrackeralientech.enums.EstadoEnvio;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Entidad principal del dominio que representa una transacción de envío de paquete.
 * Mapea la tabla 'tb_envios' e incluye métodos con reglas de negocio.
 */
@Entity
@Table(name = "tb_envios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Envio {

    /** Clave primaria UUID */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Código alfanumérico único para seguimiento público (ej: TRK-A1B2C3D4) */
    @Column(nullable = false, unique = true)
    private String codigoSeguimiento;

    /** Peso del paquete en Kilogramos (kg) */
    @Column(nullable = false)
    private Double peso;

    /** Ancho en centímetros (cm) */
    @Column(nullable = false)
    private Double ancho;

    /** Alto en centímetros (cm) */
    @Column(nullable = false)
    private Double alto;

    /** Largo en centímetros (cm) */
    @Column(nullable = false)
    private Double largo;

    /** Tarifa calculada automáticamente en moneda local */
    @Column(nullable = false)
    private Double tarifa;

    /** Estado actual del envío en la máquina de estados */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoEnvio estado;

    /** Relación Muchas a Uno: Un cliente puede realizar múltiples envíos */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    /** Relación Muchas a Uno opcional: Repartidor asignado para el traslado */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repartidor_id")
    private Repartidor repartidor;

    /** Dirección geográfica de Origen (sobrescribe los nombres de columna en BD) */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "calle", column = @Column(name = "origen_calle")),
            @AttributeOverride(name = "numero", column = @Column(name = "origen_numero")),
            @AttributeOverride(name = "ciudad", column = @Column(name = "origen_ciudad")),
            @AttributeOverride(name = "codigoPostal", column = @Column(name = "origen_codigo_postal"))
    })
    private Direccion origen;

    /** Dirección geográfica de Destino (sobrescribe los nombres de columna en BD) */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "calle", column = @Column(name = "destino_calle")),
            @AttributeOverride(name = "numero", column = @Column(name = "destino_numero")),
            @AttributeOverride(name = "ciudad", column = @Column(name = "destino_ciudad")),
            @AttributeOverride(name = "codigoPostal", column = @Column(name = "destino_codigo_postal"))
    })
    private Direccion destino;

    /**
     * Regla de Negocio: Calcula la tarifa del envío según peso y volumen.
     * Fórmula: Base ($1000) + (Peso * $200) + (Volumen cm³ * 0.01)
     * @return Importe en Double
     */
    public Double calcularTarifa() {
        double volumen = ancho * alto * largo;
        return 1000.00 + (peso * 200.00) + (volumen * 0.01);
    }

    /**
     * Regla de Negocio: Evalúa si el envío cumple con las condiciones para ser cancelado.
     * @return true si el paquete está en estado PENDIENTE, false de lo contrario.
     */
    public Boolean puedeCancelarse() {
        return this.estado == EstadoEnvio.PENDIENTE;
    }
}