package org.example.common.dto.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class OtpValidation {
    private String code;
    private String refNo;
    private String phoneNo;
}
