package com.denkitronik.pedidoservice.delivery.rest;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PedidoRequest(
    @NotNull(message = "clienteId es obligatorio")
    Long clienteId,

    @NotNull(message = "productoId es obligatorio")
    Long productoId,

    @NotNull(message = "cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    Integer cantidad
) {}
