package com.lovelypets.mobileapi.message;

import com.lovelypets.auth.port.OtpEventPublisher;
import com.lovelypets.events.OtpRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Инфраструктурная реализация OtpEventPublisher.
 * Публикует OtpRequestedEvent в Kafka-топик.
 *
 * <p>KafkaTemplate<String, OtpRequestedEvent> настраивается через
 * стандартные spring.kafka.* свойства в application.yaml.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaOtpEventPublisher implements OtpEventPublisher {

    private final KafkaTemplate<String, OtpRequestedEvent> kafkaTemplate;

    @Value("${kafka.topics.otp-requested:otp-requested}")
    private String topic;

    @Override
    public void publish(OtpRequestedEvent event) {
        kafkaTemplate.send(topic, event.email(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish OtpRequestedEvent for email={}", event.email(), ex);
                    } else {
                        log.debug("OtpRequestedEvent published: topic={}, email={}", topic, event.email());
                    }
                });
    }
}
