package org.example.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.example.common.dto.dto.SmsMessageRequest;
import org.example.common.dto.dto.SmsResult;
import org.example.notification.configuration.RabbitProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    private RabbitProcessor rabbitProcessor;

    @Autowired
    public NotificationService(RabbitProcessor rabbitProcessor) {
        this.rabbitProcessor = rabbitProcessor;
    }

    @StreamListener(RabbitProcessor.INPUT)
    public void sendNotification(SmsMessageRequest message) {
        log.info("A message received: {}", message.toString());
        boolean sent = sendSMS(message.getPhoneNumber(), message.getContent());
        SmsResult smsResult = SmsResult.builder().messageId(message.getMessageId()).isSuccess(sent).build();
        rabbitProcessor.output().send(MessageBuilder.withPayload(smsResult).build());
    }

    private boolean sendSMS(String phone, String content) {
        log.info("Sending SMS. To: {}; content: {}", phone, content);
        return true;
    }
}
