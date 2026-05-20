package com.denkitronik.pedidoservice.infrastructure.events;

import java.time.Instant;
import java.util.UUID;

public record EventoBase<T>(
    String eventoId,
    String eventoTipo,
    String version,
    Instant ocurrioEn,
    String servicioOrigen,
    T payload
) {
    /**
     * Factory method -- crea un EventoBase con metadata generada automaticamente.
     *
     * @param tipo   nombre del tipo de evento, ej: "PedidoCreado"
     * @param payload objeto con los datos del dominio
     */
    public static <T> EventoBase<T> of(String tipo, T payload) {
        return new EventoBase<>(
            UUID.randomUUID().toString(),
            tipo,
            "1.0",
            Instant.now(),
            "pedido-service",
            payload
        );
    }
}
