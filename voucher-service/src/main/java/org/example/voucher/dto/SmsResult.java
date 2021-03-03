package org.example.voucher.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class SmsResult {

    private String orderId;
    private Boolean isSuccess;
}
