package com.airfryer.repicka.domain.chat.controller;

import com.airfryer.repicka.common.security.oauth2.CustomOAuth2User;
import com.airfryer.repicka.domain.chat.dto.RenewParticipateChatRoomDto;
import com.airfryer.repicka.domain.chat.dto.SendChatDto;
import com.airfryer.repicka.domain.chat.service.ChatWebSocketService;
import com.airfryer.repicka.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1")
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

    // 채팅방 참여 정보 갱신
    @MessageMapping("/participate-chatroom/renew")
    public void renewParticipateChatRoom(Principal principal, RenewParticipateChatRoomDto dto)
    {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) ((Authentication) principal).getPrincipal();
        User user = oAuth2User.getUser();
        chatWebSocketService.renewParticipateChatRoom(user, dto);
    }
}
