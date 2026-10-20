package ar.edu.unju.fi.logitrack.shipment.entity;

import ar.edu.unju.fi.logitrack.shipment.entity.enums.EstadoEnvio;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

/**
 * Registro de auditoría de un cambio de estado. Solo lo crea {@link Envio#cambiarEstado}
 * (constructor de paquete), así ningún cambio de estado queda sin historial.
 */
@Entity
@Table(name = "historial_estados")
public class HistorialEstado extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "envio_id", nullable = false, updatable = false)
    private Envio envio;

    /** Nulo en el registro inicial (creación del envío). */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", length = 20, updatable = false)
    private EstadoEnvio estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false, length = 20, updatable = false)
    private EstadoEnvio estadoNuevo;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private Instant fechaHora;

    protected HistorialEstado() {
        // requerido por JPA
    }

    HistorialEstado(Envio envio, EstadoEnvio estadoAnterior, EstadoEnvio estadoNuevo, Instant fechaHora) {
        this.envio = Objects.requireNonNull(envio, "envio");
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = Objects.requireNonNull(estadoNuevo, "estadoNuevo");
        this.fechaHora = Objects.requireNonNull(fechaHora, "fechaHora");
    }

    public Envio getEnvio() {
        return envio;
    }

    public EstadoEnvio getEstadoAnterior() {
        return estadoAnterior;
    }

    public EstadoEnvio getEstadoNuevo() {
        return estadoNuevo;
    }

    public Instant getFechaHora() {
        return fechaHora;
    }
}
