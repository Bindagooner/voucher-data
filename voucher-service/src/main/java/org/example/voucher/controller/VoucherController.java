package org.example.voucher.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.common.dto.OtpRequest;
import org.example.common.dto.OtpResponse;
import org.example.common.dto.OtpValidationResponse;
import org.example.voucher.dto.ListVoucherRequestDto;
import org.example.voucher.dto.ResponseDto;
import org.example.voucher.dto.VoucherRequestDto;
import org.example.voucher.service.OtpService;
import org.example.voucher.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.concurrent.CompletionStage;

@RestController
@Slf4j
public class VoucherController {

    private VoucherService voucherService;
    private OtpService otpService;

    @Autowired
    VoucherController(VoucherService voucherService, OtpService otpService) {
        this.voucherService = voucherService;
        this.otpService = otpService;
    }

    /**
     *
     * @param requestDto
     * @return
     */
    @PostMapping(value = "/get-voucher", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletionStage<ResponseEntity<ResponseDto>> getVoucher(@RequestBody VoucherRequestDto requestDto) {
        log.info("Get voucher request received: {}", requestDto.toString());
        return voucherService.acquireVoucher(requestDto);
    }

    @PostMapping(value = "list-voucher", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listVoucher(@RequestBody @Valid ListVoucherRequestDto requestDto) {
        log.info("List voucher request received");
        if (requestDto.getOtp() != null) {
            // validate OTP
            OtpValidationResponse otpValidationResponse = otpService.validateOtp(requestDto.getOtp());
            if (otpValidationResponse.isValid()) {
                List<String> vouchers = voucherService.getPurchasedVoucher(requestDto.getPhoneNo());
                return ResponseEntity.ok(vouchers);
            }

        }
        // request OTP
        OtpResponse otpResponse = otpService.requestOtp(OtpRequest.builder().requester("voucher-service")
                .phoneNumber(requestDto.getPhoneNo()).build());
        return ResponseEntity.accepted().body(otpResponse);
    }
}
