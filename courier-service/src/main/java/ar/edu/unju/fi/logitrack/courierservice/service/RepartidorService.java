package ar.edu.unju.fi.logitrack.courierservice.service;

import ar.edu.unju.fi.logitrack.courierservice.dto.repartidor.DisponibilidadResponse;
import ar.edu.unju.fi.logitrack.courierservice.dto.repartidor.RepartidorRequest;
import ar.edu.unju.fi.logitrack.courierservice.dto.repartidor.RepartidorResponse;
import ar.edu.unju.fi.logitrack.courierservice.entity.enums.EstadoRepartidor;

import java.util.List;
import java.util.UUID;

/**
 * Contrato de la gestión de repartidores.
 */
public interface RepartidorService {

    /** Registra un repartidor ACTIVO; el email debe ser único. */
    RepartidorResponse registrar(RepartidorRequest request);

    /** @param estado filtro opcional; si es nulo devuelve todos. */
    List<RepartidorResponse> listar(EstadoRepartidor estado);

    RepartidorResponse obtenerPorId(UUID id);

    /** Reemplaza nombre, vehículo, email y teléfono; el email no puede pertenecer a otro repartidor. */
    RepartidorResponse actualizar(UUID id, RepartidorRequest request);

    /** Activa o desactiva un repartidor. */
    RepartidorResponse cambiarEstado(UUID id, EstadoRepartidor nuevoEstado);

    /**
     * Consulta si un repartidor puede recibir envíos.
     * Es el endpoint que consume shipment-service de forma remota antes de asignar.
     */
    DisponibilidadResponse consultarDisponibilidad(UUID id);
}
