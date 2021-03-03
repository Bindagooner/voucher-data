package org.example.mock3rd.controller;

import org.example.mock3rd.dto.RequestDto;
import org.example.mock3rd.dto.ResponseDto;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
public class MockController {

    Random random = new Random();


    @PostMapping(value = "/get-voucher", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDto> getVoucher(@RequestBody RequestDto requestDto,
                                                  @RequestHeader(name = "Authorization") String authorization)
            throws InterruptedException {

        int wait = random.nextInt(120 - 3 + 1) + 3;
        Thread.sleep(wait * 1000);

        return ResponseEntity.ok(new ResponseDto("test-voucher-" + wait, requestDto.getRequestId()));
    }
}
