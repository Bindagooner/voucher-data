package org.example.voucher.service;

import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.example.voucher.dto.*;
import org.example.voucher.entity.Order;
import org.example.voucher.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

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

    /**
     * Assume that payment has been done before go to this function. Order status is PAYMENT_SUCCESS
     * @param requestDto
     * @return
     */
    @TimeLimiter(name = "3rdService", fallbackMethod = "handleFallback")
    public CompletionStage<ResponseDto> acquireVoucher(VoucherRequestDto requestDto) {
        Order order = new Order();
        order.setOrderId(requestDto.getOrderId());
        order.setPhoneNumber(requestDto.getPhoneNo());
        order.setOrderStatus(OrderStatus.PAYMENT_SUCCESS);
        orderRepository.saveAndFlush(order);

        Supplier<ResponseDto> supplier = () -> {
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
                    voucherMessageService.sendSMSMessage(SmsMessageRequest.builder()
                            .content("Voucher code: " + voucherCode).messageId(requestDto.getOrderId())
                            .phoneNumber(requestDto.getPhoneNo())
                            .build());
                } else {
                    order1.setOrderStatus(OrderStatus.COMPLETED);
                }
                orderRepository.save(order1);
            }

            return VoucherDto.builder().message(voucherCode).build();
        };
        return CompletableFuture.supplyAsync(supplier);
    }

    private MultiValueMap<String, String> buildHeader() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "Basic aaaaa");
        return headers;
    }

    private CompletionStage<ResponseDto> handleFallback(VoucherRequestDto requestDto, Throwable throwable) throws Throwable {
        log.info("fallback is being called...");
        log.error("Error: ", throwable);
        if (throwable instanceof TimeoutException) {
            orderRepository.save(Utilities.buildOrder(requestDto.getOrderId(), null,
                    requestDto.getPhoneNo(), OrderStatus.VOUCHER_REQUESTING));
            return CompletableFuture.supplyAsync(() ->
                    ResponseDto.builder().message("The request is being processed within 30 seconds.")
                            .statusCode(202).build());
        } else {
            orderRepository.save(Utilities.buildOrder(requestDto.getOrderId(), null,
                    requestDto.getPhoneNo(), OrderStatus.VOUCHER_REQUEST_FAILED));
            throw throwable;
        }

    }
}
