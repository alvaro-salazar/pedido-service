package com.denkitronik.pedidoservice.infrastructure.messaging;

public record PagoRechazadoEvent(
    Long pagoId,
    Long pedidoId
) {}
