package org.example.voucher.service;

import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.example.common.dto.MessageType;
import org.example.common.dto.NotificationChannel;
import org.example.common.dto.SendingMessageRequest;
import org.example.voucher.dto.*;
import org.example.voucher.entity.Order;
import org.example.voucher.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@Slf4j
public class VoucherService {

    private OrderRepository orderRepository;
    private VoucherMessageService voucherMessageService;
    private static final String EXTERNAL_URL = "http://localhost:7788/third-party/get-voucher";

    @Autowired
    public VoucherService(OrderRepository orderRepository, VoucherMessageService voucherMessageService) {
        this.orderRepository = orderRepository;
        this.voucherMessageService = voucherMessageService;
    }

    public List<String> getPurchasedVoucher(String phoneNumber) {
        List<Order> orders = orderRepository.findByPhoneNumber(phoneNumber);
        return orders.stream().map(Order::getVoucherCode).filter(StringUtils::isNotEmpty).collect(Collectors.toList());
    }

    /**
     * Assume that payment has been done before go to this function. Order status is PAYMENT_SUCCESS
     * @param requestDto
     * @return
     */
    @TimeLimiter(name = "3rdService", fallbackMethod = "handleFallback")
    public CompletionStage<ResponseEntity<ResponseDto>> acquireVoucher(VoucherRequestDto requestDto) {
        Order order = new Order();
        order.setOrderId(requestDto.getOrderId());
        order.setPhoneNumber(requestDto.getPhoneNo());
        order.setOrderStatus(OrderStatus.PAYMENT_SUCCESS);
        orderRepository.saveAndFlush(order);

        Supplier<ResponseEntity<ResponseDto>> supplier = () -> {
            log.info("Requesting voucher from 3rd party - orderId: {}, phoneNumber: {}",
                    requestDto.getOrderId(), requestDto.getPhoneNo());

            RestTemplate restTemplate = new RestTemplate();
            MultiValueMap<String, String> headers = buildHeader();

            HttpEntity<ThirdPartyRequestDto> request = new HttpEntity<>(
                    new ThirdPartyRequestDto(requestDto.getOrderId(), requestDto.getPhoneNo()), headers);
            ThirdPartyResponseDto resp = restTemplate.postForObject(EXTERNAL_URL, request, ThirdPartyResponseDto.class);

            String voucherCode = resp.getVoucherCode();
            log.info("Third party responded - {}", voucherCode);
            Optional<Order> byOrderId = orderRepository.findByOrderId(requestDto.getOrderId());
            if (byOrderId.isPresent()) {
                Order order1 = byOrderId.get();
                order1.setVoucherCode(voucherCode);
                if (order1.getOrderStatus().equals(OrderStatus.VOUCHER_REQUESTING)) {
                    voucherMessageService.sendSMSMessage(SendingMessageRequest.builder()
                            .content("Voucher code: " + voucherCode).messageId(requestDto.getOrderId())
                            .phoneNumber(requestDto.getPhoneNo()).messageType(MessageType.SMS_VOUCHER)
                            .channel(NotificationChannel.SMS)
                            .build());
                } else {
                    order1.setOrderStatus(OrderStatus.COMPLETED);
                }
                orderRepository.save(order1);
            }

            return ResponseEntity.ok(VoucherDto.builder().message(voucherCode).build());
        };
        return CompletableFuture.supplyAsync(supplier);
    }

    private MultiValueMap<String, String> buildHeader() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "Basic aaaaa");
        return headers;
    }

    private CompletionStage<ResponseEntity<ResponseDto>> handleFallback(VoucherRequestDto requestDto, Throwable throwable) {
        log.info("fallback is being called...");
        if (throwable instanceof TimeoutException) {
            orderRepository.save(Utilities.buildOrder(requestDto.getOrderId(), null,
                    requestDto.getPhoneNo(), OrderStatus.VOUCHER_REQUESTING));
            return CompletableFuture.supplyAsync(() ->
                    ResponseEntity.accepted().body(
                            ResponseDto.builder().message("The request is being processed within 30 seconds.")
                                    .statusCode(202).build()));
        } else {
            orderRepository.save(Utilities.buildOrder(requestDto.getOrderId(), null,
                    requestDto.getPhoneNo(), OrderStatus.VOUCHER_REQUEST_FAILED));
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Something went wrong. Please retry later.", throwable);
        }

    }
}
