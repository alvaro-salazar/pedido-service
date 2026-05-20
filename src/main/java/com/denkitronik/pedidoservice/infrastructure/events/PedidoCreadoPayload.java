package com.denkitronik.pedidoservice.infrastructure.events;

import java.math.BigDecimal;

public record PedidoCreadoPayload(
    Long pedidoId,
    Long clienteId,
    Long productoId,
    Integer cantidad,
    BigDecimal total,
    String estado
) {}
