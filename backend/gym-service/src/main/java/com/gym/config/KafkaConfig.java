package com.gym.config;

import com.gym.event.GymEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration Kafka pour le Gym Service
 * Support des messages String et Objet (JSON)
 */
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    /**
     * Configuration commune pour les producers
     */
    private Map<String, Object> getCommonProducerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        return props;
    }

    /**
     * Producer Factory pour messages STRING
     */
    @Bean
    public ProducerFactory<String, String> stringProducerFactory() {
        Map<String, Object> props = getCommonProducerProps();
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    /**
     * KafkaTemplate pour messages STRING
     */
    @Bean
    public KafkaTemplate<String, String> stringKafkaTemplate() {
        return new KafkaTemplate<>(stringProducerFactory());
    }

    /**
     * Producer Factory pour messages OBJET (JSON)
     */
    @Bean
    public ProducerFactory<String, GymEvent> gymEventProducerFactory() {
        Map<String, Object> props = getCommonProducerProps();
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        // Configuration JSON Serializer
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        
        return new DefaultKafkaProducerFactory<>(props);
    }

    /**
     * KafkaTemplate pour messages OBJET (GymEvent)
     */
    @Bean
    public KafkaTemplate<String, GymEvent> gymEventKafkaTemplate() {
        return new KafkaTemplate<>(gymEventProducerFactory());
    }
}