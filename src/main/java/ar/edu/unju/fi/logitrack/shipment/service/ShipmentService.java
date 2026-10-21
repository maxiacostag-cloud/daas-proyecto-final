package ar.edu.unju.fi.logitrack.shipment.service;

import ar.edu.unju.fi.logitrack.shipment.dto.envio.*;
import ar.edu.unju.fi.logitrack.shipment.entity.enums.EstadoEnvio;

import java.util.List;
import java.util.UUID;

public interface ShipmentService {
    EnvioResponse registrarEnvio(RegistrarEnvioRequest request);
    EnvioResponse obtenerPorId(UUID id);
    EnvioResponse obtenerPorCodigoSeguimiento(String codigo);
    List<EnvioResponse> listarPorEstado(EstadoEnvio estado);
    EnvioResponse cambiarEstado(UUID id, CambiarEstadoRequest request);
    EnvioResponse cancelarEnvio(UUID id);
    List<HistorialEstadoResponse> obtenerHistorial(UUID id);
}
