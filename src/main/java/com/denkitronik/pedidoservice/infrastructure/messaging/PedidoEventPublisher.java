package com.denkitronik.pedidoservice.infrastructure.messaging;

import com.denkitronik.pedidoservice.domain.entities.Pedido;
import com.denkitronik.pedidoservice.domain.events.PedidoActualizadoDomainEvent;
import com.denkitronik.pedidoservice.domain.events.PedidoCreadoDomainEvent;
import com.denkitronik.pedidoservice.infrastructure.events.EventoBase;
import com.denkitronik.pedidoservice.infrastructure.events.PedidoActualizadoPayload;
import com.denkitronik.pedidoservice.infrastructure.events.PedidoCreadoPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PedidoEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Escucha PedidoCreadoDomainEvent y lo publica a Kafka SOLO despues del commit de BD.
     * TransactionPhase.AFTER_COMMIT garantiza que si la transaccion hace rollback,
     * este metodo nunca se ejecuta -- no hay eventos fantasma.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPedidoCreado(PedidoCreadoDomainEvent event) {
        Pedido pedido = event.pedido();
        var payload = new PedidoCreadoPayload(
            pedido.getId(),
            pedido.getClienteId(),
            pedido.getProductoId(),
            pedido.getCantidad(),
            pedido.getTotal(),
            pedido.getEstado().name()
        );
        var evento = EventoBase.of("PedidoCreado", payload);
        kafkaTemplate.send(KafkaConfig.TOPIC_PEDIDOS_CREADOS, pedido.getId().toString(), evento)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Publicado PedidoCreado: pedidoId={} partition={} offset={}",
                        pedido.getId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                } else {
                    log.error("Error publicando PedidoCreado para pedidoId={}: {}",
                        pedido.getId(), ex.getMessage());
                }
            });
    }

    /**
     * Escucha PedidoActualizadoDomainEvent -- publicado cuando el estado del pedido cambia
     * como consecuencia de un evento de pago (PagoConfirmado, PagoRechazado, etc.).
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPedidoActualizado(PedidoActualizadoDomainEvent event) {
        Pedido pedido = event.pedido();
        var payload = new PedidoActualizadoPayload(
            pedido.getId(),
            event.estadoAnterior(),
            pedido.getEstado().name()
        );
        var evento = EventoBase.of("PedidoActualizado", payload);
        kafkaTemplate.send(KafkaConfig.TOPIC_PEDIDOS_ACTUALIZADOS, pedido.getId().toString(), evento)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Publicado PedidoActualizado: pedidoId={} {} -> {} partition={} offset={}",
                        pedido.getId(),
                        event.estadoAnterior(),
                        pedido.getEstado().name(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                } else {
                    log.error("Error publicando PedidoActualizado para pedidoId={}: {}",
                        pedido.getId(), ex.getMessage());
                }
            });
    }
}
