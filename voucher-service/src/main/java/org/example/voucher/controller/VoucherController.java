package org.example.voucher.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.voucher.dto.ResponseDto;
import org.example.voucher.dto.VoucherRequestDto;
import org.example.voucher.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletionStage;

@RestController
@Slf4j
public class VoucherController {

    private VoucherService voucherService;

    @Autowired
    VoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @PostMapping(value = "/get-voucher", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletionStage<ResponseDto> getVoucher(@RequestBody VoucherRequestDto requestDto) {
        log.info("Get voucher request received: {}", requestDto.toString());
        return voucherService.acquireVoucher(requestDto);
    }
}
