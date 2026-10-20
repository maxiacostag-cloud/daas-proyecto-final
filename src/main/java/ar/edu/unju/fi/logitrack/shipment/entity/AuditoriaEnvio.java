package ar.edu.unju.fi.logitrack.shipment.entity;

import ar.edu.unju.fi.logitrack.shipment.entity.enums.EstadoEnvio;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_auditoria_envios")
public class AuditoriaEnvio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "envio_id", nullable = false)
    private Envio envio;

    @Enumerated(EnumType.STRING)
    private EstadoEnvio estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoEnvio estadoNuevo;

    @Column(nullable = false)
    private LocalDateTime fechaCambio;

    private String observacion;

    public AuditoriaEnvio() {
    }

    public AuditoriaEnvio(UUID id, Envio envio, EstadoEnvio estadoAnterior, EstadoEnvio estadoNuevo, LocalDateTime fechaCambio, String observacion) {
        this.id = id;
        this.envio = envio;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.fechaCambio = fechaCambio;
        this.observacion = observacion;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Envio getEnvio() { return envio; }
    public void setEnvio(Envio envio) { this.envio = envio; }

    public EstadoEnvio getEstadoAnterior() { return estadoAnterior; }
    public void setEstadoAnterior(EstadoEnvio estadoAnterior) { this.estadoAnterior = estadoAnterior; }

    public EstadoEnvio getEstadoNuevo() { return estadoNuevo; }
    public void setEstadoNuevo(EstadoEnvio estadoNuevo) { this.estadoNuevo = estadoNuevo; }

    public LocalDateTime getFechaCambio() { return fechaCambio; }
    public void setFechaCambio(LocalDateTime fechaCambio) { this.fechaCambio = fechaCambio; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }

    public static AuditoriaEnvioBuilder builder() {
        return new AuditoriaEnvioBuilder();
    }

    public static class AuditoriaEnvioBuilder {
        private UUID id;
        private Envio envio;
        private EstadoEnvio estadoAnterior;
        private EstadoEnvio estadoNuevo;
        private LocalDateTime fechaCambio;
        private String observacion;

        public AuditoriaEnvioBuilder id(UUID id) { this.id = id; return this; }
        public AuditoriaEnvioBuilder envio(Envio envio) { this.envio = envio; return this; }
        public AuditoriaEnvioBuilder estadoAnterior(EstadoEnvio estadoAnterior) { this.estadoAnterior = estadoAnterior; return this; }
        public AuditoriaEnvioBuilder estadoNuevo(EstadoEnvio estadoNuevo) { this.estadoNuevo = estadoNuevo; return this; }
        public AuditoriaEnvioBuilder fechaCambio(LocalDateTime fechaCambio) { this.fechaCambio = fechaCambio; return this; }
        public AuditoriaEnvioBuilder observacion(String observacion) { this.observacion = observacion; return this; }

        public AuditoriaEnvio build() {
            return new AuditoriaEnvio(id, envio, estadoAnterior, estadoNuevo, fechaCambio, observacion);
        }
    }
}
