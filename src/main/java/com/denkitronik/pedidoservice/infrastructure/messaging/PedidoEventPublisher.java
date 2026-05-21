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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PedidoEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Escucha PedidoCreadoDomainEvent y lo publica a Kafka SOLO despues del commit de BD.
     *
     * @Transactional(kafkaTransactionManager) abre una transaccion Kafka:
     * todos los sends dentro de este metodo son atomicos. Si falla el send
     * a pedidos.auditoria, el send a pedidos.creados tambien hace rollback.
     * El consumer con isolation.level=read_committed solo vera estos mensajes
     * cuando la transaccion Kafka haga commit.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(transactionManager = "kafkaTransactionManager", propagation = Propagation.REQUIRES_NEW)
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

        // Send 1: topic de negocio (consumers lo procesan)
        kafkaTemplate.send(KafkaConfig.TOPIC_PEDIDOS_CREADOS, pedido.getId().toString(), evento);

        // Send 2: topic de auditoria (ambos van en la misma transaccion Kafka)
        kafkaTemplate.send(KafkaConfig.TOPIC_PEDIDOS_AUDITORIA, pedido.getId().toString(), evento);

        log.info("Publicado PedidoCreado (tx Kafka): pedidoId={}", pedido.getId());
    }

    /**
     * Escucha PedidoActualizadoDomainEvent -- publicado cuando el estado del pedido cambia
     * como consecuencia de un evento de pago (PagoConfirmado, PagoRechazado, etc.).
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(transactionManager = "kafkaTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void onPedidoActualizado(PedidoActualizadoDomainEvent event) {
        Pedido pedido = event.pedido();
        var payload = new PedidoActualizadoPayload(
            pedido.getId(),
            pedido.getClienteId(),
            event.estadoAnterior(),
            pedido.getEstado().name()
        );
        var evento = EventoBase.of("PedidoActualizado", payload);

        // Send 1: topic de negocio
        kafkaTemplate.send(KafkaConfig.TOPIC_PEDIDOS_ACTUALIZADOS, pedido.getId().toString(), evento);

        // Send 2: auditoria
        kafkaTemplate.send(KafkaConfig.TOPIC_PEDIDOS_AUDITORIA, pedido.getId().toString(), evento);

        log.info("Publicado PedidoActualizado (tx Kafka): pedidoId={} {} -> {}",
            pedido.getId(), event.estadoAnterior(), pedido.getEstado().name());
    }
}
