package com.lovelypets.crm.temp;

import com.lovelypets.auth.port.OtpEventPublisher;
import com.lovelypets.events.OtpRequestedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MockOtpEventPublisher implements OtpEventPublisher {
    @Override
    public void publish(OtpRequestedEvent event) {
        log.info("This is MockOtpEventPublisher, dont call it in production!");
    }
}
