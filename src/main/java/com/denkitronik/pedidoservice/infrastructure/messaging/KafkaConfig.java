package com.denkitronik.pedidoservice.infrastructure.messaging;

import jakarta.persistence.EntityManagerFactory;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class KafkaConfig {

    /**
     * Marca el JPA TransactionManager como @Primary para que Spring lo use por defecto
     * cuando no se especifica un transactionManager explicitamente.
     * El kafkaTransactionManager se usa solo en @Transactional(transactionManager="kafkaTransactionManager").
     */
    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    public static final String TOPIC_PEDIDOS_CREADOS      = "pedidos.creados";
    public static final String TOPIC_PEDIDOS_ACTUALIZADOS = "pedidos.actualizados";
    public static final String TOPIC_PEDIDOS_AUDITORIA    = "pedidos.auditoria";

    @Bean
    public NewTopic topicPedidosCreados() {
        return TopicBuilder.name(TOPIC_PEDIDOS_CREADOS)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic topicPedidosActualizados() {
        return TopicBuilder.name(TOPIC_PEDIDOS_ACTUALIZADOS)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic topicPedidosAuditoria() {
        return TopicBuilder.name(TOPIC_PEDIDOS_AUDITORIA)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
