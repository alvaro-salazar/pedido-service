package com.denkitronik.pedidoservice.infrastructure.messaging;

public record InventarioInsuficienteEvent(
    Long    pedidoId,
    Long    productoId,
    Integer cantidadSolicitada,
    Integer stockDisponible
) {}
