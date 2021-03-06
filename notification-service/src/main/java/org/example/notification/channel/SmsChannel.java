package org.example.notification.channel;

import lombok.extern.slf4j.Slf4j;
import org.example.common.dto.NotificationChannel;
import org.example.common.dto.SendingMessageRequest;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsChannel implements IChannel {
    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SMS;
    }

    @Override
    public boolean send(SendingMessageRequest request) {
        return sendSMS(request.getPhoneNumber(), request.getContent());
    }

    private boolean sendSMS(String phone, String content) {
        log.info("Sending SMS. To: {}; content: {}", phone, content);
        return true;
    }
}
