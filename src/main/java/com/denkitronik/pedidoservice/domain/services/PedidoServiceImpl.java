package com.denkitronik.pedidoservice.domain.services;

import com.denkitronik.pedidoservice.delivery.rest.PedidoRequest;
import com.denkitronik.pedidoservice.delivery.rest.PedidoResponse;
import com.denkitronik.pedidoservice.domain.entities.EstadoPedido;
import com.denkitronik.pedidoservice.domain.entities.Pedido;
import com.denkitronik.pedidoservice.domain.repositories.IPedidoRepository;
import com.denkitronik.pedidoservice.infrastructure.clients.ClienteFeignClient;
import com.denkitronik.pedidoservice.infrastructure.clients.ClienteDTO;
import com.denkitronik.pedidoservice.infrastructure.clients.ProductoFeignClient;
import com.denkitronik.pedidoservice.infrastructure.clients.ProductoDTO;
import com.denkitronik.pedidoservice.infrastructure.clients.RecursoNoEncontradoException;
import com.denkitronik.pedidoservice.infrastructure.messaging.PedidoCreadoEvent;
import com.denkitronik.pedidoservice.infrastructure.messaging.PedidoEventPublisher;
import com.denkitronik.pedidoservice.infrastructure.pago.PagoIniciarRequest;
import com.denkitronik.pedidoservice.infrastructure.pago.PagoIniciarResponse;
import com.denkitronik.pedidoservice.infrastructure.pago.PagoServiceClient;
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
    private final ClienteFeignClient clienteClient;
    private final ProductoFeignClient productoClient;
    private final PedidoEventPublisher eventPublisher;
    private final PagoServiceClient pagoServiceClient;

    @Override
    @Transactional
    public PedidoResponse crearPedido(PedidoRequest request) {
        log.info("Creando pedido: clienteId={}, productoId={}, cantidad={}",
                request.clienteId(), request.productoId(), request.cantidad());

        ClienteDTO cliente = clienteClient.obtenerCliente(request.clienteId());
        log.debug("Cliente validado: {}", cliente.nombre());

        ProductoDTO producto = productoClient.obtenerProducto(request.productoId());
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

        PedidoCreadoEvent evento = new PedidoCreadoEvent(
                pedido.getId(),
                pedido.getClienteId(),
                cliente.nombre(),
                pedido.getProductoId(),
                producto.nombre(),
                pedido.getCantidad(),
                pedido.getTotal(),
                pedido.getFechaCreacion()
        );
        eventPublisher.publicarPedidoCreado(evento);

        return toResponse(pedido, cliente.nombre(), producto.nombre(), null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPedidos() {
        return pedidoRepository.findAll().stream()
                .map(p -> toResponse(p,
                        "Cliente #" + p.getClienteId(),
                        "Producto #" + p.getProductoId(),
                        null))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResponse obtenerPedido(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado: " + id));
        return toResponse(pedido,
                "Cliente #" + pedido.getClienteId(),
                "Producto #" + pedido.getProductoId(),
                null);
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
                "Producto #" + pedido.getProductoId(),
                null);
    }

    @Override
    @Transactional
    public PedidoResponse iniciarPago(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado: " + pedidoId));

        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException(
                "Solo se puede iniciar pago de pedidos en estado PENDIENTE. Estado actual: "
                + pedido.getEstado());
        }

        String descripcion = "Pedido #" + pedidoId + " - La Fogata Digital";
        PagoIniciarRequest request = new PagoIniciarRequest(pedidoId, pedido.getTotal(), descripcion);
        PagoIniciarResponse pagoResponse = pagoServiceClient.iniciarPago(request);

        pedido.setPagoPreferenciaId(String.valueOf(pagoResponse.pagoId()));
        pedidoRepository.save(pedido);

        log.info("Pago iniciado para pedido {}, checkoutUrl={}", pedidoId, pagoResponse.checkoutUrl());
        return toResponse(pedido,
                "Cliente #" + pedido.getClienteId(),
                "Producto #" + pedido.getProductoId(),
                pagoResponse.checkoutUrl());
    }

    private PedidoResponse toResponse(Pedido pedido, String clienteNombre, String productoNombre, String checkoutUrl) {
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
                pedido.getFechaCreacion(),
                checkoutUrl
        );
    }
}
