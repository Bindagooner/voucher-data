package org.example.otpservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.common.dto.OtpRequest;
import org.example.common.dto.OtpResponse;
import org.example.common.dto.OtpValidation;
import org.example.common.dto.OtpValidationResponse;
import org.example.otpservice.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@Slf4j
public class OtpController {

    private final OtpService otpService;

    @Autowired
    public OtpController(OtpService service) {
        this.otpService = service;
    }

    @PostMapping(value = "request-otp", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> requestOTP(@RequestBody @Valid OtpRequest request) {
        log.info("request otp: {}", request);
        String refNo = otpService.requestOtp(request);
        return ResponseEntity.ok(new OtpResponse(refNo));
    }

    @PostMapping(value = "validate-otp", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> validateOtp(@RequestBody @Valid OtpValidation otpValidation) {
        log.info("validate otp: {}", otpValidation.toString());
        boolean isValid = otpService.validate(otpValidation);
        return ResponseEntity.ok(new OtpValidationResponse(otpValidation.getRefNo(), isValid));
    }
}
