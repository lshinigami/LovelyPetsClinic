package com.lovelypets.auth.port;

import com.lovelypets.events.OtpRequestedEvent;

/**
 * Порт (выходящий адаптер) для отправки OTP-события в Kafka.
 */
public interface OtpEventPublisher {

    void publish(OtpRequestedEvent event);
}
