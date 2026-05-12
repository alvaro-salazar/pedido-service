package com.denkitronik.pedidoservice.infrastructure.clients;

public record ClienteDTO(
    Long id,
    String nombre,
    String email,
    RegionDTO region
) {}
