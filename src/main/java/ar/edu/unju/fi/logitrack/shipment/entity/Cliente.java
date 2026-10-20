package ar.edu.unju.fi.logitrack.shipment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "clientes")
public class Cliente extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 30)
    private String telefono;

    protected Cliente() {
        // requerido por JPA
    }

    public Cliente(String nombre, String email, String telefono) {
        this.nombre = Objects.requireNonNull(nombre, "nombre");
        this.email = Objects.requireNonNull(email, "email").toLowerCase();
        this.telefono = telefono;
    }

    public void actualizarContacto(String nombre, String telefono) {
        this.nombre = Objects.requireNonNull(nombre, "nombre");
        this.telefono = telefono;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefono() {
        return telefono;
    }
}
