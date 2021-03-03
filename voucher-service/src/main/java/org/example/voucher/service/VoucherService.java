package org.example.voucher.service;

import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.example.voucher.dto.*;
import org.example.voucher.entity.Order;
import org.example.voucher.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

@Service
@Slf4j
public class VoucherService {

    private OrderRepository orderRepository;
    private OkHttpClient client;
    private static final String EXTERNAL_URL = "http://localhost:7788/get-voucher";

    @Autowired
    public VoucherService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
        client = new OkHttpClient.Builder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build();
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
        order = orderRepository.saveAndFlush(order);

        Supplier<ResponseDto> supplier = () -> {
            log.info("Requesting voucher from 3rd party - orderId: {}, phoneNumber: {}",
                    requestDto.getOrderId(), requestDto.getPhoneNo());

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<ThirdPartyRequestDto> request = new HttpEntity<>(
                    new ThirdPartyRequestDto(requestDto.getOrderId(), requestDto.getPhoneNo()));
            ThirdPartyResponseDto resp = restTemplate.postForObject(EXTERNAL_URL, request, ThirdPartyResponseDto.class);

            log.info("Third party responded");
            Optional<Order> byOrderId = orderRepository.findByOrderId(requestDto.getOrderId());
            if (byOrderId.isPresent())

            return VoucherDto.builder().message(resp.getVoucherCode()).build();
        };
        return CompletableFuture.supplyAsync(supplier);
    }

    private CompletionStage<ResponseDto> handleFallback(VoucherRequestDto requestDto, Throwable throwable) {
        log.info("fallback is being called...");
        if (throwable instanceof TimeoutException) {

            orderRepository.save(Utilities.buildOrder(requestDto.getOrderId(), null,
                    requestDto.getPhoneNo(), OrderStatus.VOUCHER_REQUESTING));
            return CompletableFuture.supplyAsync(() ->
                    ResponseDto.builder().message("The request is being processed within 30 seconds.")
                            .statusCode(202).build());
        } else {
            orderRepository.save(Utilities.buildOrder(requestDto.getOrderId(), null,
                    requestDto.getPhoneNo(), OrderStatus.VOUCHER_REQUEST_FAILED));
            return CompletableFuture.supplyAsync(() ->
                    ResponseDto.builder().message("Something went wrong")
                            .statusCode(500).build());
        }

    }
}
