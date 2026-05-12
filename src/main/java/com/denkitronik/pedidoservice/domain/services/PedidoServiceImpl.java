package com.denkitronik.pedidoservice.domain.services;

import com.denkitronik.pedidoservice.delivery.rest.PedidoRequest;
import com.denkitronik.pedidoservice.delivery.rest.PedidoResponse;
import com.denkitronik.pedidoservice.domain.entities.EstadoPedido;
import com.denkitronik.pedidoservice.domain.entities.Pedido;
import com.denkitronik.pedidoservice.domain.repositories.IPedidoRepository;
import com.denkitronik.pedidoservice.infrastructure.clients.ClienteClient;
import com.denkitronik.pedidoservice.infrastructure.clients.ClienteDTO;
import com.denkitronik.pedidoservice.infrastructure.clients.ProductoClient;
import com.denkitronik.pedidoservice.infrastructure.clients.ProductoDTO;
import com.denkitronik.pedidoservice.infrastructure.clients.RecursoNoEncontradoException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements IPedidoService {

    private final IPedidoRepository pedidoRepository;
    private final ClienteClient clienteClient;
    private final ProductoClient productoClient;

    @Override
    @Transactional
    public PedidoResponse crearPedido(PedidoRequest request, String bearerToken) {
        log.info("Creando pedido: clienteId={}, productoId={}, cantidad={}",
                request.clienteId(), request.productoId(), request.cantidad());

        ClienteDTO cliente = clienteClient.obtenerCliente(request.clienteId(), bearerToken);
        log.debug("Cliente validado: {}", cliente.nombre());

        ProductoDTO producto = productoClient.obtenerProducto(request.productoId(), bearerToken);
        log.debug("Producto obtenido: {} a precio {}", producto.nombre(), producto.precio());

        BigDecimal total = producto.precio()
                .multiply(BigDecimal.valueOf(request.cantidad()));

        Pedido pedido = Pedido.builder()
                .clienteId(request.clienteId())
                .productoId(request.productoId())
                .cantidad(request.cantidad())
                .precioUnitario(producto.precio())
                .total(total)
                .estado(EstadoPedido.PENDIENTE)
                .build();

        pedido = pedidoRepository.save(pedido);
        log.info("Pedido creado con id={}, total={}", pedido.getId(), pedido.getTotal());

        return toResponse(pedido, cliente.nombre(), producto.nombre());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPedidos() {
        return pedidoRepository.findAll().stream()
                .map(p -> toResponse(p, "Cliente #" + p.getClienteId(),
                                        "Producto #" + p.getProductoId()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResponse obtenerPedido(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado: " + id));
        return toResponse(pedido,
                "Cliente #" + pedido.getClienteId(),
                "Producto #" + pedido.getProductoId());
    }

    @Override
    @Transactional
    public PedidoResponse cambiarEstado(Long id, EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado: " + id));
        pedido.setEstado(nuevoEstado);
        pedido = pedidoRepository.save(pedido);
        log.info("Estado del pedido {} cambiado a {}", id, nuevoEstado);
        return toResponse(pedido,
                "Cliente #" + pedido.getClienteId(),
                "Producto #" + pedido.getProductoId());
    }

    private PedidoResponse toResponse(Pedido pedido, String clienteNombre, String productoNombre) {
        return new PedidoResponse(
                pedido.getId(),
                pedido.getClienteId(),
                clienteNombre,
                pedido.getProductoId(),
                productoNombre,
                pedido.getCantidad(),
                pedido.getPrecioUnitario(),
                pedido.getTotal(),
                pedido.getEstado(),
                pedido.getFechaCreacion()
        );
    }
}
