package org.example.voucher.service;

import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.example.voucher.dto.OrderStatus;
import org.example.voucher.dto.ResponseDto;
import org.example.voucher.dto.VoucherDto;
import org.example.voucher.dto.VoucherRequestDto;
import org.example.voucher.entity.Order;
import org.example.voucher.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

@Service
@Slf4j
public class VoucherService {

    private OrderRepository orderRepository;

    @Autowired
    public VoucherService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
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

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            throw new RuntimeException("3rd exception");
//            return VoucherDto.builder().message("voucher_code_aaa").build();
        };
        return CompletableFuture.supplyAsync(supplier);
    }

    private CompletionStage<String> handleFallback(VoucherRequestDto requestDto, Throwable throwable) {
        log.info("fallback is being called...");
        if (throwable instanceof TimeoutException) {

        }
        return CompletableFuture.supplyAsync(() -> "Message error");
    }
}
