package com.denkitronik.pedidoservice.delivery.rest;

import com.denkitronik.pedidoservice.domain.entities.EstadoPedido;
import com.denkitronik.pedidoservice.domain.services.IPedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pedidos")
@RequiredArgsConstructor
public class PedidoRestController {

    private final IPedidoService pedidoService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PedidoResponse> crearPedido(
            @Valid @RequestBody PedidoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pedidoService.crearPedido(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<PedidoResponse>> listarPedidos() {
        return ResponseEntity.ok(pedidoService.listarPedidos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PedidoResponse> obtenerPedido(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.obtenerPedido(id));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PedidoResponse> cambiarEstado(
            @PathVariable Long id,
            @RequestParam EstadoPedido nuevoEstado) {
        return ResponseEntity.ok(pedidoService.cambiarEstado(id, nuevoEstado));
    }
}
