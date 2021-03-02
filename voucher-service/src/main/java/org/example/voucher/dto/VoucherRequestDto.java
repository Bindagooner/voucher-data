package org.example.voucher.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VoucherRequestDto {

    private String orderId;
    private String phoneNo;
}
