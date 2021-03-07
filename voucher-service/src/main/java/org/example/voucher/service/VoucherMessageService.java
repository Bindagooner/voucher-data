package org.example.voucher.service;

import lombok.extern.slf4j.Slf4j;
import org.example.common.dto.MessageType;
import org.example.common.dto.SendingMessageRequest;
import org.example.common.dto.SmsResult;
import org.example.voucher.configuration.RabbitProcessor;
import org.example.voucher.dto.OrderStatus;
import org.example.voucher.entity.Voucher;
import org.example.voucher.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class VoucherMessageService {

    private RabbitProcessor processor;
    private OrderRepository orderRepository;

    @Autowired
    public VoucherMessageService(RabbitProcessor processor, OrderRepository orderRepository) {
        this.processor = processor;
        this.orderRepository = orderRepository;
    }

    public void sendSMSMessage(SendingMessageRequest message) {
        log.info("Send message to channel: {}", message.toString());
        processor.output().send(MessageBuilder.withPayload(message).build());
    }

    @StreamListener(RabbitProcessor.INPUT)
    public void listenSMSResult(SmsResult result) {
        log.info("A Message received: {}", result.toString());
        if (!result.getMessageType().equals(MessageType.SMS_VOUCHER)) return;
        Optional<Voucher> byOrderId = orderRepository.findByOrderId(result.getMessageId());
        if (byOrderId.isPresent()) {
            Voucher voucher = byOrderId.get();
            voucher.setOrderStatus(result.getIsSuccess() ? OrderStatus.COMPLETED : OrderStatus.SENDING_FAILED);
            orderRepository.save(voucher);
        }
    }
}
