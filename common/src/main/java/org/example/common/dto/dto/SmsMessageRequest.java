package org.example.common.dto.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
@Builder
public class SmsMessageRequest {
    private String messageType;
    private String messageId;
    private String phoneNumber;
    private String content;
}