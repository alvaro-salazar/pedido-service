package com.denkitronik.pedidoservice.infrastructure.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "cliente-service", url = "${services.cliente.base-url}")
public interface ClienteFeignClient {

    @GetMapping("/api/v1/cliente-service/clientes/{id}")
    ClienteDTO obtenerCliente(@PathVariable Long id);
}
