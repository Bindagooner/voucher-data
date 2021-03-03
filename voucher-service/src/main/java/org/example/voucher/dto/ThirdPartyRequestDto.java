package org.example.voucher.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ThirdPartyRequestDto {
    private String requestId;
    private String voucherType;
}
