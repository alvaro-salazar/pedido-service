package com.denkitronik.pedidoservice.infrastructure.events;

public record PedidoActualizadoPayload(
    Long pedidoId,
    Long clienteId,
    String estadoAnterior,
    String estadoNuevo
) {}
