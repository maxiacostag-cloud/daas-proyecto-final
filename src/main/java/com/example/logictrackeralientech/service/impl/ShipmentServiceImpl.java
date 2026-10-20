package com.example.logictrackeralientech.service.impl;

import com.example.logictrackeralientech.dto.EnvioRequestDto;
import com.example.logictrackeralientech.dto.EnvioResponseDto;
import com.example.logictrackeralientech.enums.EstadoEnvio;
import com.example.logictrackeralientech.enums.EstadoRepartidor;
import com.example.logictrackeralientech.model.*;
import com.example.logictrackeralientech.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementación de la lógica de negocio, máquina de estados y auditoría para envíos.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentServiceImpl implements ShipmentService {

    private final EnvioRepository envioRepository;
    private final ClienteRepository clienteRepository;
    private final RepartidorRepository repartidorRepository;
    private final AuditoriaEnvioRepository auditoriaRepository;

    @Override
    @Transactional
    public EnvioResponseDto registrarEnvio(EnvioRequestDto request) {
        log.info("Registrando envío para el cliente ID: {}", request.getClienteId());

        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + request.getClienteId()));

        Envio envio = Envio.builder()
                .codigoSeguimiento("TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .peso(request.getPeso())
                .ancho(request.getAncho())
                .alto(request.getAlto())
                .largo(request.getLargo())
                .estado(EstadoEnvio.PENDIENTE)
                .cliente(cliente)
                .origen(request.getOrigen())
                .destino(request.getDestino())
                .build();

        envio.setTarifa(envio.calcularTarifa());

        Envio guardado = envioRepository.save(envio);
        registrarAuditoria(guardado, null, EstadoEnvio.PENDIENTE, "Registro inicial del envío");

        return mapToDto(guardado);
    }

    @Override
    @Transactional
    public EnvioResponseDto asignarRepartidor(UUID envioId, UUID repartidorId) {
        log.info("Asignando repartidor ID: {} al envío ID: {}", repartidorId, envioId);

        Envio envio = envioRepository.findById(envioId)
                .orElseThrow(() -> new IllegalArgumentException("Envío no encontrado"));

        if (envio.getEstado() != EstadoEnvio.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden asignar repartidores a envíos en estado PENDIENTE");
        }

        Repartidor repartidor = repartidorRepository.findById(repartidorId)
                .orElseThrow(() -> new IllegalArgumentException("Repartidor no encontrado"));

        if (repartidor.getEstado() != EstadoRepartidor.ACTIVO) {
            throw new IllegalStateException("El repartidor asignado debe estar en estado ACTIVO");
        }

        envio.setRepartidor(repartidor);
        return mapToDto(envioRepository.save(envio));
    }

    @Override
    @Transactional
    public EnvioResponseDto cambiarEstado(UUID envioId, EstadoEnvio nuevoEstado, String observacion) {
        log.info("Cambiando estado del envío ID: {} a {}", envioId, nuevoEstado);

        Envio envio = envioRepository.findById(envioId)
                .orElseThrow(() -> new IllegalArgumentException("Envío no encontrado"));

        EstadoEnvio estadoAnterior = envio.getEstado();
        validarTransicionEstado(estadoAnterior, nuevoEstado);

        envio.setEstado(nuevoEstado);
        Envio actualizado = envioRepository.save(envio);

        registrarAuditoria(actualizado, estadoAnterior, nuevoEstado, observacion);

        return mapToDto(actualizado);
    }

    @Override
    @Transactional
    public EnvioResponseDto cancelarEnvio(UUID envioId) {
        Envio envio = envioRepository.findById(envioId)
                .orElseThrow(() -> new IllegalArgumentException("Envío no encontrado"));

        if (!envio.puedeCancelarse()) {
            throw new IllegalStateException("El envío solo puede cancelarse si está en estado PENDIENTE");
        }

        return cambiarEstado(envioId, EstadoEnvio.CANCELADO, "Cancelado por solicitud del usuario");
    }

    // =========================================================================
    // MÉTODOS DE CONSULTA FALTANTES (Implementación del Contrato)
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public EnvioResponseDto obtenerPorId(UUID envioId) {
        Envio envio = envioRepository.findById(envioId)
                .orElseThrow(() -> new IllegalArgumentException("Envío no encontrado con ID: " + envioId));
        return mapToDto(envio);
    }

    @Override
    @Transactional(readOnly = true)
    public EnvioResponseDto obtenerPorCodigoSeguimiento(String codigoSeguimiento) {
        Envio envio = envioRepository.findByCodigoSeguimiento(codigoSeguimiento)
                .orElseThrow(() -> new IllegalArgumentException("Envío no encontrado con código: " + codigoSeguimiento));
        return mapToDto(envio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnvioResponseDto> listarPorEstado(EstadoEnvio estado) {
        return envioRepository.findByEstado(estado).stream()
                .map(this::mapToDto)
                .toList();
    }

    // =========================================================================
    // MÉTODOS AUXILIARES PRIVADOS
    // =========================================================================

    private void validarTransicionEstado(EstadoEnvio actual, EstadoEnvio nuevo) {
        if (actual == EstadoEnvio.PENDIENTE && (nuevo == EstadoEnvio.EN_RUTA || nuevo == EstadoEnvio.CANCELADO)) {
            return;
        }
        if (actual == EstadoEnvio.EN_RUTA && nuevo == EstadoEnvio.ENTREGADO) {
            return;
        }
        throw new IllegalStateException("Transición de estado inválida de " + actual + " a " + nuevo);
    }

    private void registrarAuditoria(Envio envio, EstadoEnvio anterior, EstadoEnvio nuevo, String observacion) {
        AuditoriaEnvio auditoria = AuditoriaEnvio.builder()
                .envio(envio)
                .estadoAnterior(anterior)
                .estadoNuevo(nuevo)
                .fechaCambio(LocalDateTime.now())
                .observacion(observacion)
                .build();

        auditoriaRepository.save(auditoria);
    }

    private EnvioResponseDto mapToDto(Envio envio) {
        return EnvioResponseDto.builder()
                .id(envio.getId())
                .codigoSeguimiento(envio.getCodigoSeguimiento())
                .peso(envio.getPeso())
                .tarifa(envio.getTarifa())
                .estado(envio.getEstado())
                .clienteNombre(envio.getCliente().getNombre())
                .repartidorNombre(envio.getRepartidor() != null ? envio.getRepartidor().getNombre() : "SIN ASIGNAR")
                .origen(envio.getOrigen())
                .destino(envio.getDestino())
                .build();
    }
}