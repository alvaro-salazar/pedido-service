package com.denkitronik.pedidoservice.infrastructure.messaging;

import com.denkitronik.pedidoservice.domain.entities.EstadoPedido;
import com.denkitronik.pedidoservice.domain.repositories.IPedidoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PagoEventListener {

    private final IPedidoRepository pedidoRepository;

    @KafkaListener(topics = "pagos.confirmados", groupId = "pedido-pago-group")
    @Transactional
    public void onPagoConfirmado(PagoConfirmadoEvent evento) {
        log.info("Pago confirmado para pedido {}, monto: {}", evento.pedidoId(), evento.monto());
        pedidoRepository.findById(evento.pedidoId()).ifPresent(pedido -> {
            pedido.setEstado(EstadoPedido.CONFIRMADO);
            pedidoRepository.save(pedido);
            log.info("Pedido {} actualizado a CONFIRMADO", evento.pedidoId());
        });
    }

    @KafkaListener(topics = "pagos.rechazados", groupId = "pedido-pago-group")
    @Transactional
    public void onPagoRechazado(PagoRechazadoEvent evento) {
        log.info("Pago rechazado para pedido {}", evento.pedidoId());
        pedidoRepository.findById(evento.pedidoId()).ifPresent(pedido -> {
            pedido.setEstado(EstadoPedido.CANCELADO);
            pedidoRepository.save(pedido);
            log.info("Pedido {} actualizado a CANCELADO", evento.pedidoId());
        });
    }

    @KafkaListener(topics = "pagos.reembolsados", groupId = "pedido-pago-group")
    @Transactional
    public void onPagoReembolsado(PagoReembolsadoEvent evento) {
        log.info("Reembolso de {} para pedido {}", evento.montoReembolsado(), evento.pedidoId());
        pedidoRepository.findById(evento.pedidoId()).ifPresent(pedido -> {
            pedido.setEstado(EstadoPedido.REEMBOLSADO);
            pedidoRepository.save(pedido);
            log.info("Pedido {} actualizado a REEMBOLSADO", evento.pedidoId());
        });
    }
}
