package com.denkitronik.pedidoservice.domain.events;

import com.denkitronik.pedidoservice.domain.entities.Pedido;

/**
 * Evento de dominio (interno) -- captura el cambio de estado de un pedido.
 * Incluye el estado anterior para que los consumers puedan razonar sobre la transicion.
 */
public record PedidoActualizadoDomainEvent(Pedido pedido, String estadoAnterior) {}
