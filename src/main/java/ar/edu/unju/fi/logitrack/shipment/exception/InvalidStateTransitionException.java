package ar.edu.unju.fi.logitrack.shipment.exception;

import ar.edu.unju.fi.logitrack.shipment.entity.enums.EstadoEnvio;

/** Intento de cambio de estado que no respeta el flujo PENDIENTE → EN_RUTA → ENTREGADO (o CANCELADO). */
public class InvalidStateTransitionException extends BusinessRuleException {

    private final EstadoEnvio actual;
    private final EstadoEnvio solicitado;

    public InvalidStateTransitionException(EstadoEnvio actual, EstadoEnvio solicitado) {
        super("Transición de estado inválida: %s → %s. Permitidas desde %s: %s"
                .formatted(actual, solicitado, actual, actual.transicionesPermitidas()));
        this.actual = actual;
        this.solicitado = solicitado;
    }

    public EstadoEnvio getActual() {
        return actual;
    }

    public EstadoEnvio getSolicitado() {
        return solicitado;
    }
}
