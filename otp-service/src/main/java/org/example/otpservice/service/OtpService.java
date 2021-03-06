package org.example.otpservice.service;

import lombok.extern.slf4j.Slf4j;
import org.example.common.dto.*;
import org.example.otpservice.RabbitProcessor;
import org.example.otpservice.entity.OtpEntity;
import org.example.otpservice.repository.OtpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class OtpService {

    private RabbitProcessor rabbitProcessor;
    private OtpRepository otpRepository;

    @Autowired
    public OtpService(RabbitProcessor rabbitProcessor, OtpRepository otpRepository) {
        this.rabbitProcessor = rabbitProcessor;
        this.otpRepository = otpRepository;
    }

    public String requestOtp(OtpRequest otpRequest) {
        OtpEntity entity = generate(otpRequest);
        otpRepository.save(entity);
        sendOtp(entity);
        return entity.getReferenceNumber();
    }

    private OtpEntity generate(OtpRequest otpRequest) {
        // TODO
        OtpEntity entity = new OtpEntity();
        entity.setPhoneNumber(otpRequest.getPhoneNumber());
        entity.setRequester(otpRequest.getRequester());
        entity.setValidated(false);
        entity.setReferenceNumber("randomString" + System.currentTimeMillis());
        entity.setCode("123456");
        entity.setExpiredTime(System.currentTimeMillis() + 5 * 60 * 1000); // + 5mins
        return entity;
    }

    protected void sendOtp(OtpEntity entity) {
        SendingMessageRequest smsMessage = SendingMessageRequest.builder().messageId("otp-" + entity.getReferenceNumber())
                .content("OTP: " + entity.getCode()).phoneNumber(entity.getPhoneNumber())
                .messageType(MessageType.SMS_OTP)
                .channel(NotificationChannel.SMS)
                .build();
        log.info("sending event smsMessage");
        rabbitProcessor.output().send(MessageBuilder.withPayload(smsMessage).build());
    }

    public boolean validate(OtpValidation otpValidation) {
        Optional<OtpEntity> entity = otpRepository.findByReferenceNumber(otpValidation.getRefNo());

        if (entity.isPresent()) {
            OtpEntity otp = entity.get();
            if (!otp.getCode().equals(otpValidation.getCode())) return false;
            if (otp.getExpiredTime() <= System.currentTimeMillis()) return false;
            otp.setValidated(true);
            otpRepository.save(otp);
            return true;
        }

        return false;
    }
}
