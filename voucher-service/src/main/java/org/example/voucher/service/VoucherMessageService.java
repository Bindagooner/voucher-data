package org.example.voucher.service;

import lombok.extern.slf4j.Slf4j;
import org.example.common.dto.dto.SmsMessageRequest;
import org.example.common.dto.dto.SmsResult;
import org.example.voucher.configuration.RabbitProcessor;
import org.example.voucher.dto.OrderStatus;
import org.example.voucher.entity.Order;
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

    public void sendSMSMessage(SmsMessageRequest message) {
        log.info("Send message to channel: {}", message.toString());
        processor.output().send(MessageBuilder.withPayload(message).build());
    }

    @StreamListener(RabbitProcessor.INPUT)
    public void listenSMSResult(SmsResult result) {
        log.info("A Message received: {}", result.toString());
        if (!result.getMessageType().equals("VOUCHER")) return;
        Optional<Order> byOrderId = orderRepository.findByOrderId(result.getMessageId());
        if (byOrderId.isPresent()) {
            Order order = byOrderId.get();
            order.setOrderStatus(result.getIsSuccess() ? OrderStatus.COMPLETED : OrderStatus.SENDING_FAILED);
            orderRepository.save(order);
        }
    }
}
