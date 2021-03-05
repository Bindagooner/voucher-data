package org.example.voucher.service;

import org.example.common.dto.dto.OtpRequest;
import org.example.common.dto.dto.OtpResponse;
import org.example.common.dto.dto.OtpValidation;
import org.example.common.dto.dto.OtpValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient("otp-service")
public interface OtpService {

    @PostMapping(value = "request-otp", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    OtpResponse requestOtp(OtpRequest request);

    @PostMapping(value = "validate-otp", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    OtpValidationResponse validateOtp(OtpValidation otpValidation);
}
