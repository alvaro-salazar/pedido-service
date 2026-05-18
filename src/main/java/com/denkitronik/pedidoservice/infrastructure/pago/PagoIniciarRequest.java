package com.denkitronik.pedidoservice.infrastructure.pago;

import java.math.BigDecimal;

public record PagoIniciarRequest(
    Long pedidoId,
    BigDecimal monto,
    String descripcion
) {}
