package ar.edu.unju.fi.logitrack.shipment.mapper;

import ar.edu.unju.fi.logitrack.shipment.dto.envio.EnvioResponse;
import ar.edu.unju.fi.logitrack.shipment.dto.envio.HistorialEstadoResponse;
import ar.edu.unju.fi.logitrack.shipment.entity.Envio;
import ar.edu.unju.fi.logitrack.shipment.entity.HistorialEstado;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class EnvioMapper {

    public EnvioResponse toResponse(Envio envio) {
        if (envio == null) {
            return null;
        }

        return new EnvioResponse(
            envio.getId(),
            envio.getCodigoSeguimiento(),
            envio.getPesoKg() != null ? envio.getPesoKg().doubleValue() : null,
            envio.getEstado(),
            envio.getCliente() != null ? envio.getCliente().getId() : null,
            envio.getRepartidor() != null ? envio.getRepartidor().getId() : null,
            envio.getOrigen() != null ? envio.getOrigen().getCiudad() : null,
            envio.getDestino() != null ? envio.getDestino().getCiudad() : null
        );
    }

    public HistorialEstadoResponse toHistorialResponse(HistorialEstado historial) {
        if (historial == null) {
            return null;
        }
        LocalDateTime fechaHoraLocal = historial.getFechaHora() != null
            ? LocalDateTime.ofInstant(historial.getFechaHora(), ZoneId.systemDefault())
            : null;
        return new HistorialEstadoResponse(
            historial.getEstadoAnterior(),
            historial.getEstadoNuevo(),
            fechaHoraLocal,
            "Transición de estado a " + historial.getEstadoNuevo()
        );
    }
}
