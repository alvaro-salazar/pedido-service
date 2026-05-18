package com.denkitronik.pedidoservice.domain.services;

import com.denkitronik.pedidoservice.delivery.rest.PedidoRequest;
import com.denkitronik.pedidoservice.delivery.rest.PedidoResponse;
import com.denkitronik.pedidoservice.domain.entities.EstadoPedido;
import java.util.List;

public interface IPedidoService {
    PedidoResponse crearPedido(PedidoRequest request);
    List<PedidoResponse> listarPedidos();
    PedidoResponse obtenerPedido(Long id);
    PedidoResponse cambiarEstado(Long id, EstadoPedido nuevoEstado);
    PedidoResponse iniciarPago(Long pedidoId);
}
