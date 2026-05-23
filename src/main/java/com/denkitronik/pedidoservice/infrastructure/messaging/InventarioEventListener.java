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

@Slf4j
@Component
@RequiredArgsConstructor
public class InventarioEventListener {

    private final IPedidoRepository         pedidoRepository;
    private final ApplicationEventPublisher  applicationEventPublisher;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "inventario.reservado", groupId = "pedido-inventario-group",
        properties = {"spring.json.value.default.type=com.denkitronik.pedidoservice.infrastructure.messaging.InventarioReservadoEvent"})
    @Transactional
    public void onInventarioReservado(InventarioReservadoEvent evento) {
        log.info("Inventario reservado para pedido {}. Confirmando...", evento.pedidoId());
        pedidoRepository.findById(evento.pedidoId()).ifPresent(pedido -> {
            try {
                pedido.avanzarEstado(EstadoPedido.CONFIRMADO);
                pedidoRepository.save(pedido);
                applicationEventPublisher.publishEvent(
                    new PedidoActualizadoDomainEvent(pedido, "PAGO_CONFIRMADO"));
                log.info("Pedido {} CONFIRMADO", pedido.getId());
            } catch (IllegalStateException e) {
                log.warn("Evento duplicado ignorado para pedido {}: {}", evento.pedidoId(), e.getMessage());
            }
        });
    }

    @KafkaListener(topics = "inventario.insuficiente", groupId = "pedido-inventario-group",
        properties = {"spring.json.value.default.type=com.denkitronik.pedidoservice.infrastructure.messaging.InventarioInsuficienteEvent"})
    @Transactional
    public void onInventarioInsuficiente(InventarioInsuficienteEvent evento) {
        log.warn("Stock insuficiente para pedido {}. Compensando...", evento.pedidoId());
        pedidoRepository.findById(evento.pedidoId()).ifPresent(pedido -> {
            try {
                pedido.avanzarEstado(EstadoPedido.COMPENSANDO);
                pedidoRepository.save(pedido);
                applicationEventPublisher.publishEvent(
                    new PedidoActualizadoDomainEvent(pedido, "PAGO_CONFIRMADO"));

                var reembolso = new ReembolsoSolicitadoEvent(pedido.getId(), pedido.getClienteId());
                kafkaTemplate.send("pagos.reembolsos.solicitados",
                                   pedido.getId().toString(), reembolso);
                log.info("ReembolsoSolicitado publicado para pedido {}", pedido.getId());
            } catch (IllegalStateException e) {
                log.warn("Evento duplicado ignorado para pedido {}", evento.pedidoId());
            }
        });
    }
}
