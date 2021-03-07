package org.example.voucher.service;

import org.example.common.dto.OtpRequest;
import org.example.common.dto.OtpResponse;
import org.example.common.dto.OtpValidation;
import org.example.common.dto.OtpValidationResponse;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "otp-service", path = "otp-service")
@RibbonClient(value = "otp-service")
public interface OtpServiceProxy {

    @PostMapping(value = "request-otp", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    OtpResponse requestOtp(OtpRequest request);

    @PostMapping(value = "validate-otp", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    OtpValidationResponse validateOtp(OtpValidation otpValidation);
}
