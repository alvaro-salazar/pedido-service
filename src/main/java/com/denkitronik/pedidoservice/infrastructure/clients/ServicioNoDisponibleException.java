package com.denkitronik.pedidoservice.infrastructure.clients;

public class ServicioNoDisponibleException extends RuntimeException {
    public ServicioNoDisponibleException(String message) {
        super(message);
    }
}
