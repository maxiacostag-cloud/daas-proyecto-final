package com.example.logictrackeralientech.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Componente incrustable (Value Object / Embeddable) que abstrae una dirección geográfica.
 * Se reutiliza tanto para la dirección de Origen como de Destino dentro de la entidad Envio.
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Direccion {

    private String calle;        // Nombre de la calle
    private String numero;       // Altura / Numeración
    private String ciudad;       // Ciudad o Localidad
    private String codigoPostal; // Código Postal
}
