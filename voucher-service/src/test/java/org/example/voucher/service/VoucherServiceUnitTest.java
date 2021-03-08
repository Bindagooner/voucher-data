package org.example.voucher.service;

import org.example.voucher.dto.*;
import org.example.voucher.repository.VoucherRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.stubbing.answers.AnswersWithDelay;
import org.mockito.internal.stubbing.answers.Returns;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(value = "classpath:application.properties")
public class VoucherServiceUnitTest {

    @Autowired
    private VoucherService voucherService;

    @Autowired
    private VoucherRepository voucherRepository;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    public void testAcquireVoucher() throws ExecutionException, InterruptedException {
        VoucherRequestDto requestDto = new VoucherRequestDto("orderId" + System.currentTimeMillis(), "077777777");
        HttpEntity<ThirdPartyRequestDto> request = new HttpEntity<>(
                new ThirdPartyRequestDto(requestDto.getOrderId(), requestDto.getPhoneNo()), voucherService.buildHeader());
        when(restTemplate.postForObject(VoucherService.EXTERNAL_URL, request, ThirdPartyResponseDto.class))
                .thenReturn(new ThirdPartyResponseDto("voucher-code-test", requestDto.getOrderId()));
        voucherService.setRestTemplate(restTemplate);

        CompletionStage<ResponseEntity<ResponseDto>> response = voucherService.acquireVoucher(requestDto);
        ResponseEntity<ResponseDto> responseEntity = response.toCompletableFuture().get();
        assertEquals(HttpStatus.OK , responseEntity.getStatusCode());
        assertEquals("voucher-code-test", responseEntity.getBody().getMessage());
    }

    @Test
    public void testAcquireVoucherTimeout() throws ExecutionException, InterruptedException {
        String orderId = "orderId" + System.currentTimeMillis();
        VoucherRequestDto requestDto = new VoucherRequestDto(orderId, "077777777");
        HttpEntity<ThirdPartyRequestDto> request = new HttpEntity<>(
                new ThirdPartyRequestDto(requestDto.getOrderId(), requestDto.getPhoneNo()), voucherService.buildHeader());

        doAnswer(new AnswersWithDelay(7 * 1000, new Returns(
                new ThirdPartyResponseDto("voucher-code-test", requestDto.getOrderId()))))
                .when(restTemplate).postForObject(VoucherService.EXTERNAL_URL, request, ThirdPartyResponseDto.class);

        voucherService.setRestTemplate(restTemplate);

        CompletionStage<ResponseEntity<ResponseDto>> response = voucherService.acquireVoucher(requestDto);
        ResponseEntity<ResponseDto> responseEntity = response.toCompletableFuture().get();
        assertEquals(HttpStatus.ACCEPTED , responseEntity.getStatusCode());
        assertEquals("The request is being processed within 30 seconds.", responseEntity.getBody().getMessage());
        assertEquals(OrderStatus.VOUCHER_REQUESTING, voucherRepository.findByOrderId(orderId).get().getOrderStatus());
    }
}
