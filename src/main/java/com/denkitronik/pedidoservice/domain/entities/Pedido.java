package com.denkitronik.pedidoservice.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long clienteId;

    @Column(nullable = false)
    private Long productoId;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "pago_preferencia_id")
    private String pagoPreferenciaId;

    public void avanzarEstado(EstadoPedido nuevoEstado) {
        boolean valida = switch (this.estado) {
            case PENDIENTE       -> nuevoEstado == EstadoPedido.PAGO_CONFIRMADO
                                 || nuevoEstado == EstadoPedido.CANCELADO;
            case PAGO_CONFIRMADO -> nuevoEstado == EstadoPedido.CONFIRMADO
                                 || nuevoEstado == EstadoPedido.COMPENSANDO;
            case COMPENSANDO     -> nuevoEstado == EstadoPedido.REEMBOLSADO;
            default              -> false;
        };
        if (!valida) {
            throw new IllegalStateException(
                "Transición inválida: " + this.estado + " → " + nuevoEstado +
                " para pedido " + this.id);
        }
        this.estado = nuevoEstado;
    }
}
