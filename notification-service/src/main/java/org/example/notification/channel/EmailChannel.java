package org.example.notification.channel;

import org.example.common.dto.NotificationChannel;
import org.example.common.dto.SendingMessageRequest;

import java.util.Map;

public class EmailChannel implements IChannel{
    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public boolean send(SendingMessageRequest request) {
        // TODO
        return true ;
    }
}
