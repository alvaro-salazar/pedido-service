package com.denkitronik.pedidoservice.infrastructure.pago;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
public class PagoServiceClient {

    private final RestClient client;

    public PagoServiceClient(@Qualifier("pagoServiceClient") RestClient client) {
        this.client = client;
    }

    public PagoIniciarResponse iniciarPago(PagoIniciarRequest request) {
        try {
            return client.post()
                .uri("/pagos")
                .body(request)
                .retrieve()
                .body(PagoIniciarResponse.class);
        } catch (RestClientException e) {
            log.error("Error al iniciar pago para pedido {}: {}", request.pedidoId(), e.getMessage());
            throw new RuntimeException("No se pudo iniciar el pago. Intenta nuevamente.", e);
        }
    }
}
