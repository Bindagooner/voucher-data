package org.example.notification.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class SmsResult {

    private String messageId;
    private Boolean isSuccess;
}
