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
public class ProductoClient {

    private final RestClient restClient;

    public ProductoClient(@Qualifier("productoServiceClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public ProductoDTO obtenerProducto(Long productoId, String bearerToken) {
        log.debug("Consultando producto-service para productoId={}", productoId);
        try {
            return restClient.get()
                    .uri("/api/v1/producto-service/productos/{id}", productoId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                    .retrieve()
                    .body(ProductoDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Producto no encontrado: id={}", productoId);
            throw new RecursoNoEncontradoException("Producto no encontrado: " + productoId);
        } catch (RestClientException e) {
            log.error("Error al contactar producto-service: {}", e.getMessage());
            throw new ServicioNoDisponibleException("producto-service no disponible");
        }
    }
}
