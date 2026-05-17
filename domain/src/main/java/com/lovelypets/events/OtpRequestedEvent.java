package com.lovelypets.events;

/**
 * Kafka-событие, которое отправляется после сохранения OTP.
 * Consumer в другом модуле отправляет email с кодом клиенту.
 */
public record OtpRequestedEvent(String email, String otp) {
}
