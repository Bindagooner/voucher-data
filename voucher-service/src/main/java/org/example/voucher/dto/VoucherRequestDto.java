package org.example.voucher.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class VoucherRequestDto {

    private String orderId;
    private String phoneNo;
}
