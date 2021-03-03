package org.example.voucher.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class SmsVoucherMessage {
    private String orderId;
    private String phoneNumber;
    private String voucherCode;
}
