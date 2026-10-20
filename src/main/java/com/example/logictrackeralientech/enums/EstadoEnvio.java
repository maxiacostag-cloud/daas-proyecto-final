package com.example.logictrackeralientech.enums;

/**
 * Enumeración que representa los estados posibles en el ciclo de vida de un envío.
 * Flujo permitido: PENDIENTE -> EN_RUTA -> ENTREGADO (o CANCELADO desde PENDIENTE).
 */
public enum EstadoEnvio {
    PENDIENTE,  // Registrado, a la espera de asignación o inicio de trayecto
    EN_RUTA,    // En proceso de transporte por un repartidor
    ENTREGADO,  // Paquete entregado exitosamente en destino
    CANCELADO   // Envío cancelado antes de iniciar ruta
}