package com.denkitronik.pedidoservice.infrastructure.pago;

public record PagoIniciarResponse(
    Long pagoId,
    String checkoutUrl,
    String estado
) {}
