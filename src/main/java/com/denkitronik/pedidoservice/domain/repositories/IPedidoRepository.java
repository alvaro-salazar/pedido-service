package com.denkitronik.pedidoservice.domain.repositories;

import com.denkitronik.pedidoservice.domain.entities.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IPedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByClienteId(Long clienteId);
}
