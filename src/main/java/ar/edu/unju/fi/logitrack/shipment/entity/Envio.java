package ar.edu.unju.fi.logitrack.shipment.entity;

import ar.edu.unju.fi.logitrack.shipment.entity.enums.EstadoEnvio;
import ar.edu.unju.fi.logitrack.shipment.exception.BusinessRuleException;
import ar.edu.unju.fi.logitrack.shipment.exception.InvalidStateTransitionException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "envios")
public class Envio extends BaseEntity {

    /**
     * Monto fijo que se cobra por cualquier envío, sin importar el paquete.
     * TODO: confirmar con el profe o con el equipo si este valor está bien o hay que cambiarlo.
     */
    static final BigDecimal TARIFA_BASE = new BigDecimal("1500.00");

    /**
     * Precio que se suma por cada kilo que se cobra.
     * TODO: confirmar con el profe o con el equipo si este valor está bien o hay que cambiarlo.
     */
    static final BigDecimal PRECIO_POR_KG = new BigDecimal("450.00");

    /**
     * Número por el que se divide el volumen del paquete (en cm³) para sacar su peso volumétrico.
     * Es el valor que usan normalmente las empresas de envíos.
     */
    static final BigDecimal DIVISOR_VOLUMETRICO = new BigDecimal("5000");

    @Column(name = "codigo_seguimiento", nullable = false, unique = true, length = 20, updatable = false)
    private String codigoSeguimiento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repartidor_id")
    private Repartidor repartidor;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "calle", column = @Column(name = "origen_calle", nullable = false, length = 120)),
        @AttributeOverride(name = "numero", column = @Column(name = "origen_numero", nullable = false, length = 10)),
        @AttributeOverride(name = "ciudad", column = @Column(name = "origen_ciudad", nullable = false, length = 80)),
        @AttributeOverride(name = "codigoPostal", column = @Column(name = "origen_codigo_postal", nullable = false, length = 10))
    })
    private Direccion origen;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "calle", column = @Column(name = "destino_calle", nullable = false, length = 120)),
        @AttributeOverride(name = "numero", column = @Column(name = "destino_numero", nullable = false, length = 10)),
        @AttributeOverride(name = "ciudad", column = @Column(name = "destino_ciudad", nullable = false, length = 80)),
        @AttributeOverride(name = "codigoPostal", column = @Column(name = "destino_codigo_postal", nullable = false, length = 10))
    })
    private Direccion destino;

    @Column(name = "peso_kg", nullable = false, precision = 10, scale = 3)
    private BigDecimal pesoKg;

    @Column(name = "alto_cm", nullable = false, precision = 10, scale = 2)
    private BigDecimal altoCm;

    @Column(name = "ancho_cm", nullable = false, precision = 10, scale = 2)
    private BigDecimal anchoCm;

    @Column(name = "largo_cm", nullable = false, precision = 10, scale = 2)
    private BigDecimal largoCm;

    /**
     * Precio final del envío. Se calcula una sola vez cuando se crea el envío.
     * Usamos BigDecimal y no Double para que no haya errores de redondeo con la plata.
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal tarifa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoEnvio estado;

    @OneToMany(mappedBy = "envio", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fechaHora ASC")
    private List<HistorialEstado> historial = new ArrayList<>();

    protected Envio() {
    }

    public Envio(String codigoSeguimiento, Cliente cliente, Direccion origen, Direccion destino,
                 BigDecimal pesoKg, BigDecimal altoCm, BigDecimal anchoCm, BigDecimal largoCm) {
        this.codigoSeguimiento = Objects.requireNonNull(codigoSeguimiento, "codigoSeguimiento");
        this.cliente = Objects.requireNonNull(cliente, "cliente");
        this.origen = Objects.requireNonNull(origen, "origen");
        this.destino = Objects.requireNonNull(destino, "destino");
        this.pesoKg = requerirPositivo(pesoKg, "peso");
        this.altoCm = requerirPositivo(altoCm, "alto");
        this.anchoCm = requerirPositivo(anchoCm, "ancho");
        this.largoCm = requerirPositivo(largoCm, "largo");
        this.tarifa = calcularTarifa();
        this.estado = EstadoEnvio.PENDIENTE;
        registrarHistorial(null, EstadoEnvio.PENDIENTE);
    }

    /**
     * Calcula cuánto cuesta el envío.
     * <p>
     * Primero se saca el peso volumétrico del paquete (alto x ancho x largo / 5000).
     * Después se toma el mayor entre ese peso y el peso real, así un paquete grande pero liviano
     * no sale más barato de lo que debería. Por último se aplica la fórmula:
     * tarifa base + precio por kilo x peso que se cobra, redondeado a 2 decimales.
     * </p>
     *
     * @return el precio del envío con 2 decimales
     */
    public BigDecimal calcularTarifa() {
        BigDecimal pesoVolumetrico = altoCm.multiply(anchoCm).multiply(largoCm)
            .divide(DIVISOR_VOLUMETRICO, 3, RoundingMode.HALF_UP);
        BigDecimal pesoQueSeCobra = pesoKg.max(pesoVolumetrico);
        return TARIFA_BASE.add(PRECIO_POR_KG.multiply(pesoQueSeCobra))
            .setScale(2, RoundingMode.HALF_UP);
    }

    public boolean puedeCancelarse() {
        return estado == EstadoEnvio.PENDIENTE;
    }

    public void cancelar() {
        if (!puedeCancelarse()) {
            throw new BusinessRuleException(
                "El envío %s no puede cancelarse porque está en estado %s; solo se cancela en PENDIENTE."
                    .formatted(codigoSeguimiento, estado));
        }
        cambiarEstado(EstadoEnvio.CANCELADO);
    }

    public void asignarRepartidor(Repartidor nuevoRepartidor) {
        Objects.requireNonNull(nuevoRepartidor, "repartidor");
        if (estado != EstadoEnvio.PENDIENTE) {
            throw new BusinessRuleException(
                "Solo se puede asignar repartidor a envíos PENDIENTES (estado actual: %s).".formatted(estado));
        }
        if (!nuevoRepartidor.estaActivo()) {
            throw new BusinessRuleException("El repartidor %s no está ACTIVO.".formatted(nuevoRepartidor.getNombre()));
        }
        this.repartidor = nuevoRepartidor;
    }

    public void cambiarEstado(EstadoEnvio nuevoEstado) {
        Objects.requireNonNull(nuevoEstado, "nuevoEstado");
        if (!estado.puedeTransicionarA(nuevoEstado)) {
            throw new InvalidStateTransitionException(estado, nuevoEstado);
        }
        if (nuevoEstado == EstadoEnvio.EN_RUTA && repartidor == null) {
            throw new BusinessRuleException("No se puede despachar el envío sin un repartidor asignado.");
        }
        EstadoEnvio anterior = this.estado;
        this.estado = nuevoEstado;
        registrarHistorial(anterior, nuevoEstado);
    }

    private void registrarHistorial(EstadoEnvio anterior, EstadoEnvio nuevo) {
        historial.add(new HistorialEstado(this, anterior, nuevo, Instant.now()));
    }

    private static BigDecimal requerirPositivo(BigDecimal valor, String campo) {
        if (valor == null || valor.signum() <= 0) {
            throw new BusinessRuleException("El %s del paquete debe ser mayor a cero.".formatted(campo));
        }
        return valor;
    }

    public String getCodigoSeguimiento() { return codigoSeguimiento; }
    public Cliente getCliente() { return cliente; }
    public Repartidor getRepartidor() { return repartidor; }
    public Direccion getOrigen() { return origen; }
    public Direccion getDestino() { return destino; }
    public BigDecimal getPesoKg() { return pesoKg; }
    public BigDecimal getTarifa() { return tarifa; }
    public EstadoEnvio getEstado() { return estado; }
    public List<HistorialEstado> getHistorial() { return Collections.unmodifiableList(historial); }
}
