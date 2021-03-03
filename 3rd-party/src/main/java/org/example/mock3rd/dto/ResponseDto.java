package org.example.mock3rd.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseDto {

    private String voucherCode;
    private String requestId;
}
