package com.denkitronik.pedidoservice.infrastructure.clients;

import java.math.BigDecimal;

public record ProductoDTO(
    Long id,
    String nombre,
    String descripcion,
    BigDecimal precio,
    String categoria,
    String imagenUrl
) {}
