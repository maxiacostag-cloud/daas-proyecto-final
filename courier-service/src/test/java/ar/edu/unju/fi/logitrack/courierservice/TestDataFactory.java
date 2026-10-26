package ar.edu.unju.fi.logitrack.courierservice;

import ar.edu.unju.fi.logitrack.courierservice.entity.Repartidor;
import ar.edu.unju.fi.logitrack.courierservice.entity.enums.EstadoRepartidor;
import ar.edu.unju.fi.logitrack.courierservice.entity.enums.TipoVehiculo;

import java.util.UUID;

/** Datos de prueba reutilizables para que los tests no repitan constructores largos. */
public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static Repartidor repartidor(String nombre, TipoVehiculo tipoVehiculo, EstadoRepartidor estado) {
        Repartidor repartidor = new Repartidor(
                nombre, tipoVehiculo, UUID.randomUUID() + "@logitrack.test", "+54 388 400-2000");
        if (estado == EstadoRepartidor.INACTIVO) {
            repartidor.desactivar();
        }
        return repartidor;
    }

    public static Repartidor repartidorActivo() {
        return repartidor("Lucía Fernández", TipoVehiculo.MOTO, EstadoRepartidor.ACTIVO);
    }

    public static Repartidor repartidorInactivo() {
        return repartidor("Sofía Mamaní", TipoVehiculo.BICICLETA, EstadoRepartidor.INACTIVO);
    }
}
