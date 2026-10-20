package ar.edu.unju.fi.logitrack.shipment.entity;

import ar.edu.unju.fi.logitrack.shipment.entity.enums.EstadoRepartidor;
import ar.edu.unju.fi.logitrack.shipment.entity.enums.TipoVehiculo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.util.Objects;

/**
 * Entidad de dominio que representa a un Repartidor en el sistema.
 * Se mapea directamente con la tabla 'repartidores' en la base de datos.
 */
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

    /**
     * Constructor por defecto requerido por el estándar de JPA (Hibernate).
     */
    protected Repartidor() {
        // Requerido por JPA
    }

    /**
     * Constructor principal de dominio. Todo repartidor nuevo inicia siempre en estado ACTIVO.
     *
     * @param nombre Nombre completo del repartidor.
     * @param tipoVehiculo Tipo de vehículo asignado (MOTO, AUTO, CAMIONETA, etc.).
     */
    public Repartidor(String nombre, TipoVehiculo tipoVehiculo) {
        this.nombre = Objects.requireNonNull(nombre, "nombre");
        this.tipoVehiculo = Objects.requireNonNull(tipoVehiculo, "tipoVehiculo");
        this.estado = EstadoRepartidor.ACTIVO;
    }

    /**
     * Constructor de conveniencia utilizado en las pruebas unitarias (ej. ShipmentServiceTest).
     * Permite instanciar un repartidor con datos rápidos asignando un tipo de vehículo por defecto.
     *
     * @param nombre Nombre del repartidor.
     * @param email Correo electrónico de contacto.
     * @param telefono Teléfono de contacto.
     */
    public Repartidor(String nombre, String email, String telefono) {
        this.nombre = Objects.requireNonNull(nombre, "nombre");
        this.tipoVehiculo = TipoVehiculo.AUTO; // Vehículo por defecto para las pruebas
        this.estado = EstadoRepartidor.ACTIVO;
    }

    // =========================================================================
    // MÉTODOS DE DOMINIO Y REGLAS DE NEGOCIO
    // =========================================================================

    /**
     * Comprueba si el repartidor está en estado activo para poder asignarle envíos.
     *
     * @return true si el estado es ACTIVO, false en caso contrario.
     */
    public boolean estaActivo() {
        return estado == EstadoRepartidor.ACTIVO;
    }

    /**
     * Activa el estado del repartidor en el sistema.
     */
    public void activar() {
        this.estado = EstadoRepartidor.ACTIVO;
    }

    /**
     * Desactiva el estado del repartidor impidiendo la asignación de nuevos envíos.
     */
    public void desactivar() {
        this.estado = EstadoRepartidor.INACTIVO;
    }

    // =========================================================================
    // GETTERS (LECTORES DE PROPIEDADES)
    // =========================================================================

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
