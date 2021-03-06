package org.example.common.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class SmsResult {
    private NotificationChannel channel;
    private MessageType messageType;
    private String messageId;
    private Boolean isSuccess;
}
