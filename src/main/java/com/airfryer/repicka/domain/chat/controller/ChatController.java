package com.airfryer.repicka.domain.chat.controller;

import com.airfryer.repicka.common.response.SuccessResponseDto;
import com.airfryer.repicka.common.security.oauth2.CustomOAuth2User;
import com.airfryer.repicka.domain.chat.dto.ChatRoomListDto;
import com.airfryer.repicka.domain.chat.dto.EnterChatRoomRes;
import com.airfryer.repicka.domain.chat.dto.GetMyChatRoomPageReq;
import com.airfryer.repicka.domain.chat.dto.SendChatDto;
import com.airfryer.repicka.domain.chat.service.ChatService;
import com.airfryer.repicka.domain.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ChatController
{
    private final ChatService chatService;

    // TODO: 제품 페이지에서 채팅방 입장

    // 나의 채팅 페이지에서 채팅방 입장
    @GetMapping("/chatroom/{chatRoomId}/enter")
    public ResponseEntity<SuccessResponseDto> enterChatRoom(@AuthenticationPrincipal CustomOAuth2User oAuth2User,
                                                            @PathVariable Long chatRoomId,
                                                            @RequestParam int pageSize)
    {
        User user = oAuth2User.getUser();
        EnterChatRoomRes data = chatService.enterChatRoom(user, chatRoomId, pageSize);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("채팅방에 성공적으로 입장하였습니다.")
                        .data(data)
                        .build());
    }

    // 채팅 전송
    @MessageMapping("/chat")
    public void send(Principal principal, SendChatDto dto)
    {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) ((Authentication) principal).getPrincipal();
        User user = oAuth2User.getUser();
        chatService.sendMessage(user, dto);
    }

    // 내 채팅방 페이지 조회
    @GetMapping("/chatroom")
    public ResponseEntity<SuccessResponseDto> getMyChatRoomPage(@AuthenticationPrincipal CustomOAuth2User oAuth2User,
                                                                @Valid GetMyChatRoomPageReq dto)
    {
        User user = oAuth2User.getUser();
        ChatRoomListDto data = chatService.getMyChatRoomPage(user, dto);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("내 채팅방 리스트를 성공적으로 조회하였습니다.")
                        .data(data)
                        .build());
    }
}
