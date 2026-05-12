package com.denkitronik.pedidoservice.infrastructure.clients;

public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String message) {
        super(message);
    }
}
