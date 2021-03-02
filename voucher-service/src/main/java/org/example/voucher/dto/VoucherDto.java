package org.example.voucher.dto;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class VoucherDto extends ResponseDto {

    private String voucherCode;

    public VoucherDto(String voucherCode) {
        super(HttpStatus.OK.value(), "SUCCESS");
        this.voucherCode = voucherCode;
    }
}
