package org.example.voucher.service;

import lombok.extern.slf4j.Slf4j;
import org.example.voucher.configuration.RabbitProcessor;
import org.example.voucher.dto.SmsVoucherMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class VoucherMessageService {

    private RabbitProcessor processor;

    @Autowired
    public VoucherMessageService(RabbitProcessor processor) {
        this.processor = processor;
    }

    public void sendSMSMessage(SmsVoucherMessage message) {
        log.info("Send message to channel: {}", message.toString());
        processor.output().send(MessageBuilder.withPayload(message).build());
    }

}
