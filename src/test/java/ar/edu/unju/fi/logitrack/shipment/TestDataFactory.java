package ar.edu.unju.fi.logitrack.shipment;

import ar.edu.unju.fi.logitrack.shipment.entity.Cliente;
import ar.edu.unju.fi.logitrack.shipment.entity.Direccion;
import ar.edu.unju.fi.logitrack.shipment.entity.Envio;
import ar.edu.unju.fi.logitrack.shipment.entity.Repartidor;
import ar.edu.unju.fi.logitrack.shipment.entity.enums.TipoVehiculo;

import java.math.BigDecimal;

/** Datos de prueba reutilizables para que los tests no repitan constructores largos. */
public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static Cliente cliente() {
        return new Cliente("Tienda Norte SRL", "ventas@tiendanorte.com", "+54 388 400-1000");
    }

    public static Direccion origen() {
        return new Direccion("Belgrano", "450", "San Salvador de Jujuy", "4600");
    }

    public static Direccion destino() {
        return new Direccion("Av. Fascio", "1200", "San Salvador de Jujuy", "4600");
    }

    public static Repartidor repartidorActivo() {
        return new Repartidor("Lucía Fernández", TipoVehiculo.MOTO);
    }

    public static Repartidor repartidorInactivo() {
        Repartidor repartidor = new Repartidor("Sofía Mamaní", TipoVehiculo.BICICLETA);
        repartidor.desactivar();
        return repartidor;
    }

    public static Envio envioPendiente(String codigo, Cliente cliente) {
        return envio(codigo, cliente, "2.5", "30", "20", "10");
    }

    public static Envio envio(String codigo, Cliente cliente,
                              String pesoKg, String altoCm, String anchoCm, String largoCm) {
        return new Envio(codigo, cliente, origen(), destino(),
                new BigDecimal(pesoKg), new BigDecimal(altoCm), new BigDecimal(anchoCm), new BigDecimal(largoCm));
    }
}
