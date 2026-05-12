package com.denkitronik.pedidoservice.infrastructure.clients;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
public class ClienteClient {

    private final RestClient restClient;

    public ClienteClient(@Qualifier("clienteServiceClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public ClienteDTO obtenerCliente(Long clienteId, String bearerToken) {
        log.debug("Consultando cliente-service para clienteId={}", clienteId);
        try {
            return restClient.get()
                    .uri("/api/v1/cliente-service/clientes/{id}", clienteId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                    .retrieve()
                    .body(ClienteDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Cliente no encontrado: id={}", clienteId);
            throw new RecursoNoEncontradoException("Cliente no encontrado: " + clienteId);
        } catch (RestClientException e) {
            log.error("Error al contactar cliente-service: {}", e.getMessage());
            throw new ServicioNoDisponibleException("cliente-service no disponible");
        }
    }
}
