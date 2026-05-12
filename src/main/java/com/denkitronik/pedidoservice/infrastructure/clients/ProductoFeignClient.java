package com.denkitronik.pedidoservice.infrastructure.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "producto-service", url = "${services.producto.base-url}")
public interface ProductoFeignClient {

    @GetMapping("/api/v1/producto-service/productos/{id}")
    ProductoDTO obtenerProducto(@PathVariable Long id);
}
