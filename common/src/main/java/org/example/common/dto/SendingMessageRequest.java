package org.example.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
@Builder
public class SendingMessageRequest {
    private NotificationChannel channel;
    private MessageType messageType;
    private String messageId;
    private String phoneNumber;
    private String content;
}
