package ar.edu.unju.fi.logitrack.shipment.entity;

import ar.edu.unju.fi.logitrack.shipment.entity.enums.EstadoRepartidor;
import ar.edu.unju.fi.logitrack.shipment.entity.enums.TipoVehiculo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "repartidores")
public class Repartidor extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_vehiculo", nullable = false, length = 20)
    private TipoVehiculo tipoVehiculo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoRepartidor estado;

    protected Repartidor() {
        // requerido por JPA
    }

    /** Todo repartidor nuevo arranca ACTIVO. */
    public Repartidor(String nombre, TipoVehiculo tipoVehiculo) {
        this.nombre = Objects.requireNonNull(nombre, "nombre");
        this.tipoVehiculo = Objects.requireNonNull(tipoVehiculo, "tipoVehiculo");
        this.estado = EstadoRepartidor.ACTIVO;
    }

    public boolean estaActivo() {
        return estado == EstadoRepartidor.ACTIVO;
    }

    public void activar() {
        this.estado = EstadoRepartidor.ACTIVO;
    }

    public void desactivar() {
        this.estado = EstadoRepartidor.INACTIVO;
    }

    public String getNombre() {
        return nombre;
    }

    public TipoVehiculo getTipoVehiculo() {
        return tipoVehiculo;
    }

    public EstadoRepartidor getEstado() {
        return estado;
    }
}
