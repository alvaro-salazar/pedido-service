package com.denkitronik.pedidoservice.infrastructure.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${services.cliente.base-url}")
    private String clienteBaseUrl;

    @Value("${services.producto.base-url}")
    private String productoBaseUrl;

    @Bean("clienteServiceClient")
    public RestClient clienteServiceClient() {
        return RestClient.builder()
                .baseUrl(clienteBaseUrl)
                .build();
    }

    @Bean("productoServiceClient")
    public RestClient productoServiceClient() {
        return RestClient.builder()
                .baseUrl(productoBaseUrl)
                .build();
    }
}
