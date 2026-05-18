package com.denkitronik.pedidoservice.infrastructure.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class PedidoEventPublisher {

    private final KafkaTemplate<String, PedidoCreadoEvent> kafkaTemplate;

    public void publicarPedidoCreado(PedidoCreadoEvent evento) {
        log.info("Publicando evento PedidoCreado: pedidoId={}", evento.pedidoId());

        CompletableFuture<SendResult<String, PedidoCreadoEvent>> future =
                kafkaTemplate.send(KafkaConfig.TOPIC,
                        String.valueOf(evento.pedidoId()),
                        evento);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Error al publicar PedidoCreado pedidoId={}: {}",
                        evento.pedidoId(), ex.getMessage());
            } else {
                log.info("PedidoCreado publicado — topic={}, partition={}, offset={}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
