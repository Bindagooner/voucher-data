package org.example.voucher.service;

import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.example.common.dto.MessageType;
import org.example.common.dto.NotificationChannel;
import org.example.common.dto.SendingMessageRequest;
import org.example.voucher.dto.*;
import org.example.voucher.entity.Voucher;
import org.example.voucher.repository.VoucherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
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

    private VoucherRepository voucherRepository;
    private VoucherMessageService voucherMessageService;
    private RestTemplate restTemplate;
    private MultiValueMap<String, String> headers;
    protected static final String EXTERNAL_URL = "http://localhost:7788/third-party/get-voucher";

    @Autowired
    public VoucherService(VoucherRepository voucherRepository, VoucherMessageService voucherMessageService) {
        this.voucherRepository = voucherRepository;
        this.voucherMessageService = voucherMessageService;
        restTemplate = new RestTemplate();
        headers = buildHeader();
    }

    public List<String> getPurchasedVoucher(String phoneNumber) {
        List<Voucher> vouchers = voucherRepository.findByPhoneNumber(phoneNumber);
        return vouchers.stream().map(Voucher::getVoucherCode).filter(StringUtils::isNotEmpty).collect(Collectors.toList());
    }

    /**
     * Assume that payment has been done before go to this function. Order status is PAYMENT_SUCCESS
     * @param requestDto
     * @return
     */
    @TimeLimiter(name = "3rdService", fallbackMethod = "handleFallback")
    public CompletionStage<ResponseEntity<ResponseDto>> acquireVoucher(VoucherRequestDto requestDto) {
        Voucher voucher = new Voucher();
        voucher.setOrderId(requestDto.getOrderId());
        voucher.setPhoneNumber(requestDto.getPhoneNo());
        voucher.setOrderStatus(OrderStatus.PAYMENT_SUCCESS);
        voucherRepository.saveAndFlush(voucher);

        Supplier<ResponseEntity<ResponseDto>> supplier = () -> {
            log.info("Requesting voucher from 3rd party - orderId: {}, phoneNumber: {}",
                    requestDto.getOrderId(), requestDto.getPhoneNo());

            HttpEntity<ThirdPartyRequestDto> request = new HttpEntity<>(
                    new ThirdPartyRequestDto(requestDto.getOrderId(), requestDto.getPhoneNo()), headers);
            ThirdPartyResponseDto resp = restTemplate.postForObject(EXTERNAL_URL, request, ThirdPartyResponseDto.class);

            String voucherCode = resp.getVoucherCode();
            log.info("Third party responded - {}", voucherCode);
            Optional<Voucher> byOrderId = voucherRepository.findByOrderId(requestDto.getOrderId());
            if (byOrderId.isPresent()) {
                Voucher voucher1 = byOrderId.get();
                voucher1.setVoucherCode(voucherCode);
                if (voucher1.getOrderStatus().equals(OrderStatus.VOUCHER_REQUESTING)) {
                    voucherMessageService.sendSMSMessage(SendingMessageRequest.builder()
                            .content("Voucher code: " + voucherCode).messageId(requestDto.getOrderId())
                            .phoneNumber(requestDto.getPhoneNo()).messageType(MessageType.SMS_VOUCHER)
                            .channel(NotificationChannel.SMS)
                            .build());
                } else {
                    voucher1.setOrderStatus(OrderStatus.COMPLETED);
                }
                voucherRepository.save(voucher1);
            }

            return ResponseEntity.ok(VoucherDto.builder().message(voucherCode).build());
        };
        return CompletableFuture.supplyAsync(supplier);
    }

    private CompletionStage<ResponseEntity<ResponseDto>> handleFallback(VoucherRequestDto requestDto, Throwable throwable) {
        log.info("fallback is being called...");
        log.error("Exception: ", throwable);
        if (throwable instanceof TimeoutException) {
            voucherRepository.save(Utilities.buildOrder(requestDto.getOrderId(), null,
                    requestDto.getPhoneNo(), OrderStatus.VOUCHER_REQUESTING));
            return CompletableFuture.supplyAsync(() ->
                    ResponseEntity.accepted().body(
                            ResponseDto.builder().message("The request is being processed within 30 seconds.")
                                    .statusCode(202).build()));
        } else {
            voucherRepository.save(Utilities.buildOrder(requestDto.getOrderId(), null,
                    requestDto.getPhoneNo(), OrderStatus.VOUCHER_REQUEST_FAILED));
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Something went wrong. Please retry later.", throwable);
        }

    }

    protected MultiValueMap<String, String> buildHeader() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        headers.add("Content-Type", "application/json");
        headers.add("Authorization",
                "Basic " + Base64Utils.encodeToString("user:password".getBytes(StandardCharsets.UTF_8)));
        return headers;
    }

    protected void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
}
