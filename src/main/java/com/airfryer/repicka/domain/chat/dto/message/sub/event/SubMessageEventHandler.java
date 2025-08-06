package com.airfryer.repicka.domain.chat.dto.message.sub.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubMessageEventHandler
{
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handle(SubMessageEvent event)
    {
        if(event.getUserId() == null) {
            messagingTemplate.convertAndSend(event.getDestination(), event.getMessage());
        } else {
            messagingTemplate.convertAndSendToUser(event.getUserId().toString(), event.getDestination(), event.getMessage());
        }
    }
}
