package ar.edu.unju.fi.logitrack.shipment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

/**
 * Value Object embebido: no tiene identidad propia ni tabla; se guarda dentro de "envios"
 * con prefijo origen_ / destino_. Es inmutable: para cambiar una dirección se crea otra.
 */
@Embeddable
public class Direccion {

    @Column(nullable = false, length = 120)
    private String calle;

    @Column(nullable = false, length = 10)
    private String numero;

    @Column(nullable = false, length = 80)
    private String ciudad;

    @Column(name = "codigo_postal", nullable = false, length = 10)
    private String codigoPostal;

    protected Direccion() {
        // requerido por JPA
    }

    public Direccion(String calle, String numero, String ciudad, String codigoPostal) {
        this.calle = Objects.requireNonNull(calle, "calle");
        this.numero = Objects.requireNonNull(numero, "numero");
        this.ciudad = Objects.requireNonNull(ciudad, "ciudad");
        this.codigoPostal = Objects.requireNonNull(codigoPostal, "codigoPostal");
    }

    public String getCalle() {
        return calle;
    }

    public String getNumero() {
        return numero;
    }

    public String getCiudad() {
        return ciudad;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Direccion that)) return false;
        return Objects.equals(calle, that.calle)
                && Objects.equals(numero, that.numero)
                && Objects.equals(ciudad, that.ciudad)
                && Objects.equals(codigoPostal, that.codigoPostal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(calle, numero, ciudad, codigoPostal);
    }
}
