package com.course.config;

import com.course.event.CourseEvent;
import com.course.event.GymEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration Kafka pour le Course Service
 * Support Producer et Consumer pour messages String et Objet
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:course-service-group}")
    private String groupId;

    // ==================== PRODUCER CONFIGURATION ====================

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
     * Producer Factory pour messages OBJET (CourseEvent)
     */
    @Bean
    public ProducerFactory<String, CourseEvent> courseEventProducerFactory() {
        Map<String, Object> props = getCommonProducerProps();
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(props);
    }

    /**
     * KafkaTemplate pour messages OBJET (CourseEvent)
     */
    @Bean
    public KafkaTemplate<String, CourseEvent> courseEventKafkaTemplate() {
        return new KafkaTemplate<>(courseEventProducerFactory());
    }

    // ==================== CONSUMER CONFIGURATION ====================

    /**
     * Configuration commune pour les consumers
     */
    private Map<String, Object> getCommonConsumerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        return props;
    }

    /**
     * Consumer Factory pour messages STRING
     */
    @Bean
    public ConsumerFactory<String, String> stringConsumerFactory() {
        Map<String, Object> props = getCommonConsumerProps();
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Listener Container Factory pour messages STRING
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> stringKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(stringConsumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    /**
     * Consumer Factory pour messages OBJET (GymEvent)
     */
    @Bean
    public ConsumerFactory<String, GymEvent> gymEventConsumerFactory() {
        Map<String, Object> props = getCommonConsumerProps();
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        
        // Configuration JsonDeserializer pour GymEvent
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.course.event,com.gym.event");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, GymEvent.class.getName());
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Listener Container Factory pour messages OBJET (GymEvent)
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, GymEvent> gymEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, GymEvent> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(gymEventConsumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }
}