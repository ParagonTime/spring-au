package org.pt.project.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.pt.project.event.TaskStreamEvent;
import org.pt.project.event.UserStreamEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    public ConsumerFactory<String, UserStreamEvent> userConsumerFactory() {
        JacksonJsonDeserializer<UserStreamEvent> jacksonDeserializer =
                new JacksonJsonDeserializer<>(UserStreamEvent.class);
        jacksonDeserializer.setUseTypeHeaders(false);

        ErrorHandlingDeserializer<UserStreamEvent> errorHandlingDeserializer =
                new ErrorHandlingDeserializer<>(jacksonDeserializer);

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "search-service-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), errorHandlingDeserializer);
    }

    @Bean
    public ConsumerFactory<String, TaskStreamEvent> taskConsumerFactory() {
        JacksonJsonDeserializer<TaskStreamEvent> jacksonDeserializer =
                new JacksonJsonDeserializer<>(TaskStreamEvent.class);
        jacksonDeserializer.setUseTypeHeaders(false);

        ErrorHandlingDeserializer<TaskStreamEvent> errorHandlingDeserializer =
                new ErrorHandlingDeserializer<>(jacksonDeserializer);

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "search-service-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), errorHandlingDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserStreamEvent> userListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UserStreamEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(userConsumerFactory());
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TaskStreamEvent> taskListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, TaskStreamEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(taskConsumerFactory());
        return factory;
    }
}