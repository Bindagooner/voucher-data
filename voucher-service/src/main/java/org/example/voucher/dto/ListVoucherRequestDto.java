package org.example.voucher.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.example.common.dto.OtpValidation;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ListVoucherRequestDto {
    @NotBlank
    @NotNull
    private String phoneNo;
    private OtpValidation otp;
}
