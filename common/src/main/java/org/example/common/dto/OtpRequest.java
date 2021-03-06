package org.example.common.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ToString
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class OtpRequest {
    @NotNull
    @NotBlank
    private String phoneNumber;

    @NotBlank
    @NotNull
    private String requester;
}
