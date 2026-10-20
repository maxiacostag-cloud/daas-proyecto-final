package com.example.logictrackeralientech.service.impl;

import com.example.logictrackeralientech.dto.EnvioRequestDto;
import com.example.logictrackeralientech.dto.EnvioResponseDto;
import com.example.logictrackeralientech.enums.EstadoEnvio;

import java.util.List;
import java.util.UUID;

/**
 * Interfaz del servicio 'ShipmentService' que define el contrato de operaciones
 * para el ciclo de vida de los envíos, cálculo de tarifas, máquina de estados y auditoría.
 */
public interface ShipmentService {

    /**
     * Registra un nuevo envío en el sistema en estado PENDIENTE, calculando automáticamente su tarifa
     * e impactando la auditoría inicial en la base de datos.
     *
     * @param request DTO con la información obligatoria del cliente, direcciones y dimensiones
     * @return DTO con la representación completa del envío registrado
     */
    EnvioResponseDto registrarEnvio(EnvioRequestDto request);

    /**
     * Asigna un repartidor activo a un envío registrado.
     * Validación de negocio: El paquete debe estar en estado PENDIENTE y el repartidor debe estar ACTIVO.
     *
     * @param envioId Identificador UUID del envío
     * @param repartidorId Identificador UUID del repartidor asignado
     * @return DTO del envío actualizado con los datos del repartidor
     */
    EnvioResponseDto asignarRepartidor(UUID envioId, UUID repartidorId);

    /**
     * Avanza o modifica el estado de un envío respetando la Máquina de Estados:
     * PENDIENTE -> EN_RUTA -> ENTREGADO (o CANCELADO desde PENDIENTE).
     * Registra automáticamente una fila de auditoría con la fecha, hora y observación.
     *
     * @param envioId Identificador UUID del envío
     * @param nuevoEstado Estado al cual se desea realizar la transición
     * @param observacion Motivo o comentario explicativo del cambio
     * @return DTO del envío con el nuevo estado aplicado
     */
    EnvioResponseDto cambiarEstado(UUID envioId, EstadoEnvio nuevoEstado, String observacion);

    /**
     * Cancela un envío registrado en la plataforma.
     * Regla de negocio: La acción solo puede llevarse a cabo si el paquete aún se encuentra en estado PENDIENTE.
     *
     * @param envioId Identificador UUID del envío a cancelar
     * @return DTO del envío cancelado
     */
    EnvioResponseDto cancelarEnvio(UUID envioId);

    /**
     * Obtiene el detalle individual de un envío mediante su identificador único.
     *
     * @param envioId Identificador UUID del envío
     * @return DTO con la información del envío
     */
    EnvioResponseDto obtenerPorId(UUID envioId);

    /**
     * Consulta el detalle de un envío utilizando su código público de seguimiento (Tracking Code).
     *
     * @param codigoSeguimiento Código alfanumérico (ej: TRK-A1B2C3D4)
     * @return DTO con la información del envío
     */
    EnvioResponseDto obtenerPorCodigoSeguimiento(String codigoSeguimiento);

    /**
     * Consulta el listado de envíos filtrados según su estado actual.
     *
     * @param estado Estado por el cual se desea filtrar (PENDIENTE, EN_RUTA, ENTREGADO, CANCELADO)
     * @return Lista de DTOs de envíos correspondientes
     */
    List<EnvioResponseDto> listarPorEstado(EstadoEnvio estado);
}