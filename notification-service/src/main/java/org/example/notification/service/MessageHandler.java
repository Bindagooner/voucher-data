package org.example.notification.service;

import com.fasterxml.jackson.databind.util.JSONPObject;
import lombok.extern.slf4j.Slf4j;
import org.example.common.dto.NotificationChannel;
import org.example.common.dto.SendingMessageRequest;
import org.example.common.dto.SmsResult;
import org.example.notification.channel.IChannel;
import org.example.notification.configuration.RabbitProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MessageHandler {

    private RabbitProcessor rabbitProcessor;
    private Map<NotificationChannel, IChannel> channels;

    @Autowired
    public MessageHandler(RabbitProcessor rabbitProcessor, List<IChannel> list) {
        this.rabbitProcessor = rabbitProcessor;
        channels = list.stream().collect(Collectors.toMap(elem -> elem.getChannel(), elem -> elem));
    }

    @StreamListener(RabbitProcessor.INPUT)
    public void sendNotification(SendingMessageRequest message) {
        log.info("A message received: {}", message.toString());
        boolean sent = channels.get(message.getChannel()).send(message);
        SmsResult smsResult = SmsResult.builder()
                .messageId(message.getMessageId())
                .isSuccess(sent)
                .messageType(message.getMessageType())
                .channel(message.getChannel()).build();
        rabbitProcessor.output().send(MessageBuilder.withPayload(smsResult).build());
    }

}
