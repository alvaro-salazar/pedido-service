package com.denkitronik.pedidoservice.domain.events;

import com.denkitronik.pedidoservice.domain.entities.Pedido;

/**
 * Evento de dominio (interno) -- publicado via ApplicationEventPublisher.
 * NO se serializa a Kafka directamente.
 * PedidoEventPublisher lo escucha y construye el EventoBase para Kafka.
 */
public record PedidoCreadoDomainEvent(Pedido pedido) {}
