package com.denkitronik.pedidoservice.infrastructure.clients;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FeignClientErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        log.warn("Error en llamada Feign — método: {}, status: {}", methodKey, response.status());
        return switch (response.status()) {
            case 404 -> new RecursoNoEncontradoException(
                    "Recurso no encontrado al llamar: " + methodKey);
            default -> new ServicioNoDisponibleException(
                    "Servicio no disponible (HTTP " + response.status() + ")");
        };
    }
}
