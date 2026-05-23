package com.denkitronik.pedidoservice.infrastructure.messaging;

import com.denkitronik.pedidoservice.domain.entities.EstadoPedido;
import com.denkitronik.pedidoservice.domain.events.PedidoActualizadoDomainEvent;
import com.denkitronik.pedidoservice.domain.repositories.IPedidoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PagoEventListener {

    private final IPedidoRepository pedidoRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * pago-service publica envuelto en EventoBase; se deserializa como Map para
     * extraer el payload anidado sin depender del tipo concreto del envelope.
     */
    @KafkaListener(topics = "pagos.confirmados", groupId = "pedido-pago-group",
        properties = {"spring.json.value.default.type=java.util.HashMap"})
    @Transactional
    public void onPagoConfirmado(Map<String, Object> message) {
        Map<String, Object> p = extractPayload(message);
        Long pedidoId = toLong(p.get("pedidoId"));
        if (pedidoId == null) { log.warn("PagoConfirmado sin pedidoId, ignorado"); return; }
        log.info("Pago confirmado para pedido {}. Iniciando reserva de inventario...", pedidoId);

        pedidoRepository.findById(pedidoId).ifPresent(pedido -> {
            try {
                pedido.avanzarEstado(EstadoPedido.PAGO_CONFIRMADO);
                pedidoRepository.save(pedido);
                applicationEventPublisher.publishEvent(
                    new PedidoActualizadoDomainEvent(pedido, "PENDIENTE"));

                // Publicar payload plano (sin EventoBase) para que InventarioSagaListener lo deserialice directamente
                var solicitud = new ReservaInventarioSolicitadaEvent(
                    pedido.getId(), pedido.getProductoId(),
                    pedido.getCantidad(), pedido.getClienteId());
                kafkaTemplate.send("inventario.reservas", pedido.getId().toString(), solicitud);
                log.info("ReservaInventarioSolicitada publicada para pedido {}", pedido.getId());

            } catch (IllegalStateException e) {
                log.warn("Transición inválida ignorada para pedido {}: {}", pedidoId, e.getMessage());
            }
        });
    }

    @KafkaListener(topics = "pagos.rechazados", groupId = "pedido-pago-group",
        properties = {"spring.json.value.default.type=java.util.HashMap"})
    @Transactional
    public void onPagoRechazado(Map<String, Object> message) {
        Map<String, Object> p = extractPayload(message);
        Long pedidoId = toLong(p.get("pedidoId"));
        if (pedidoId == null) { log.warn("PagoRechazado sin pedidoId, ignorado"); return; }
        log.info("Pago rechazado para pedido {}", pedidoId);
        pedidoRepository.findById(pedidoId).ifPresent(pedido -> {
            String estadoAnterior = pedido.getEstado().name();
            pedido.setEstado(EstadoPedido.CANCELADO);
            pedidoRepository.save(pedido);
            log.info("Pedido {} actualizado a CANCELADO", pedidoId);
            applicationEventPublisher.publishEvent(
                new PedidoActualizadoDomainEvent(pedido, estadoAnterior));
        });
    }

    @KafkaListener(topics = "pagos.reembolsados", groupId = "pedido-pago-group",
        properties = {"spring.json.value.default.type=java.util.HashMap"})
    @Transactional
    public void onPagoReembolsado(Map<String, Object> message) {
        Map<String, Object> p = extractPayload(message);
        Long pedidoId = toLong(p.get("pedidoId"));
        if (pedidoId == null) { log.warn("PagoReembolsado sin pedidoId, ignorado"); return; }
        BigDecimal monto = p.get("montoReembolsado") != null
            ? new BigDecimal(p.get("montoReembolsado").toString()) : null;
        log.info("Reembolso de {} para pedido {}", monto, pedidoId);
        pedidoRepository.findById(pedidoId).ifPresent(pedido -> {
            try {
                String estadoAnterior = pedido.getEstado().name();
                pedido.avanzarEstado(EstadoPedido.REEMBOLSADO);
                pedidoRepository.save(pedido);
                log.info("Pedido {} actualizado a REEMBOLSADO", pedidoId);
                applicationEventPublisher.publishEvent(
                    new PedidoActualizadoDomainEvent(pedido, estadoAnterior));
            } catch (IllegalStateException e) {
                log.warn("Transición inválida ignorada para pedido {}: {}", pedidoId, e.getMessage());
            }
        });
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractPayload(Map<String, Object> message) {
        Object p = message.get("payload");
        return (p instanceof Map) ? (Map<String, Object>) p : message;
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.longValue();
        try { return Long.parseLong(value.toString()); } catch (NumberFormatException e) { return null; }
    }
}
