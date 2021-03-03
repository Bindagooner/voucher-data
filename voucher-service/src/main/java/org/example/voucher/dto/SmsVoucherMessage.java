package org.example.voucher.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
@Builder
public class SmsVoucherMessage {
    private String orderId;
    private String phoneNumber;
    private String voucherCode;
}
