package com.airfryer.repicka.domain.chat.controller;

import com.airfryer.repicka.common.response.SuccessResponseDto;
import com.airfryer.repicka.common.security.oauth2.CustomOAuth2User;
import com.airfryer.repicka.domain.chat.dto.*;
import com.airfryer.repicka.domain.chat.service.ChatService;
import com.airfryer.repicka.domain.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    // 내 채팅방 페이지 조회
    // 내 제품의 채팅방 페이지 조회
    @GetMapping("/chatroom")
    public ResponseEntity<SuccessResponseDto> getMyChatRoomPage(@AuthenticationPrincipal CustomOAuth2User oAuth2User,
                                                                @RequestParam(required = false) Long itemId,
                                                                @Valid GetMyChatRoomPageReq dto)
    {
        User user = oAuth2User.getUser();

        ChatRoomListDto data = itemId == null ?
                chatService.getMyChatRoomPage(user, dto) :
                chatService.getMyChatRoomPageByItem(user, itemId, dto);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message(itemId == null ?
                                "내 채팅방 페이지를 성공적으로 조회하였습니다." :
                                "내 제품의 채팅방 페이지를 성공적으로 조회하였습니다.")
                        .data(data)
                        .build());
    }

    // 채팅 불러오기
    @GetMapping("/chatroom/{chatRoomId}/load-chat")
    public ResponseEntity<SuccessResponseDto> loadChat(@AuthenticationPrincipal CustomOAuth2User oAuth2User,
                                                       @PathVariable Long chatRoomId,
                                                       @RequestParam int pageSize,
                                                       @RequestParam String cursorId)
    {
        User user = oAuth2User.getUser();
        ChatPageDto data = chatService.loadChat(user, chatRoomId, pageSize, cursorId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("채팅을 성공적으로 불러왔습니다.")
                        .data(data)
                        .build());
    }
}
