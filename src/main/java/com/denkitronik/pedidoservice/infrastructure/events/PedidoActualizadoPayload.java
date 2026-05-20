package com.denkitronik.pedidoservice.infrastructure.events;

public record PedidoActualizadoPayload(
    Long pedidoId,
    String estadoAnterior,
    String estadoNuevo
) {}
