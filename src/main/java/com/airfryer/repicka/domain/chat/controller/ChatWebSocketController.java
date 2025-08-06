package com.airfryer.repicka.domain.chat.controller;

import com.airfryer.repicka.common.security.oauth2.CustomOAuth2User;
import com.airfryer.repicka.domain.chat.dto.SendChatDto;
import com.airfryer.repicka.domain.chat.service.ChatWebSocketService;
import com.airfryer.repicka.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController
{
    private final ChatWebSocketService chatWebSocketService;

    // 채팅 전송
    @MessageMapping("/chat")
    public void send(Principal principal, SendChatDto dto)
    {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) ((Authentication) principal).getPrincipal();
        User user = oAuth2User.getUser();
        chatWebSocketService.sendMessage(user, dto);
    }
}
