package org.example.notification.channel;

import org.example.common.dto.NotificationChannel;
import org.example.common.dto.SendingMessageRequest;

import java.util.Map;

public interface IChannel {

    NotificationChannel getChannel();
    boolean send(SendingMessageRequest metadata);
}
