package com.denkitronik.pedidoservice.delivery.rest;

import com.denkitronik.pedidoservice.domain.entities.EstadoPedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PedidoResponse(
    Long id,
    Long clienteId,
    String clienteNombre,
    Long productoId,
    String productoNombre,
    Integer cantidad,
    BigDecimal precioUnitario,
    BigDecimal total,
    EstadoPedido estado,
    LocalDateTime fechaCreacion
) {}
