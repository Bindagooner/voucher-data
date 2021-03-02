package org.example.mock3rd.controller;

import org.example.mock3rd.dto.ResponseDto;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
public class MockController {

    @PostMapping(value = "/get-voucher", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDto> getVoucher(@RequestBody ResponseDto responseDto,
                                                  @RequestHeader(name = "Authorization") String authorization) {
        if (authorization == null || authorization.isEmpty()){}
//            throw new Forbi
    }
}
