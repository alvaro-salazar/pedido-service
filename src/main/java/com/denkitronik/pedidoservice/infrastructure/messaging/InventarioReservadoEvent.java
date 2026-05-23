package com.denkitronik.pedidoservice.infrastructure.messaging;

public record InventarioReservadoEvent(
    Long    pedidoId,
    Long    productoId,
    Integer cantidadReservada,
    Integer stockRestante
) {}
