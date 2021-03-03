package org.example.voucher.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ThirdPartyResponseDto {

    private String voucherCode;
    private String requestId;
}
