package com.lovelypets.mobileapi.message.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lovelypets.events.OtpRequestedEvent;
import org.apache.kafka.common.serialization.Serializer;

public class OtpEventSerializer implements Serializer<OtpRequestedEvent> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(String topic, OtpRequestedEvent event) {
        if (event == null) return null;
        try {
            return objectMapper.writeValueAsBytes(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize OtpRequestedEvent", e);
        }
    }
}