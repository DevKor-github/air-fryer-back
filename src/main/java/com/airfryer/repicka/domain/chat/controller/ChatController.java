package com.airfryer.repicka.domain.chat.controller;

import com.airfryer.repicka.common.response.SuccessResponseDto;
import com.airfryer.repicka.common.security.oauth2.CustomOAuth2User;
import com.airfryer.repicka.domain.chat.dto.*;
import com.airfryer.repicka.domain.chat.service.ChatService;
import com.airfryer.repicka.domain.user.entity.user.User;
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

    // 채팅방 생성
    @PostMapping("/chatroom")
    public ResponseEntity<SuccessResponseDto> createChatRoom(@AuthenticationPrincipal CustomOAuth2User oAuth2User,
                                                             @RequestBody @Valid CreateChatRoomReq createChatRoomReq)
    {
        User user = oAuth2User.getUser();
        EnterChatRoomRes data = chatService.createChatRoom(user, createChatRoomReq.getItemId());

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("채팅방을 성공적으로 생성하였습니다.")
                        .data(data)
                        .build());
    }

    // 채팅방 ID로 채팅방에 입장할 때 필요한 데이터를 조회
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
                                                       @RequestParam(required = false) String cursorId)
    {
        User user = oAuth2User.getUser();
        ChatPageDto data = chatService.loadChat(user, chatRoomId, pageSize, cursorId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("채팅을 성공적으로 불러왔습니다.")
                        .data(data)
                        .build());
    }

    // 채팅방 나가기
    @PatchMapping("/chatroom/{chatRoomId}/exit")
    public ResponseEntity<SuccessResponseDto> exitChatRoom(@AuthenticationPrincipal CustomOAuth2User oAuth2User,
                                                           @PathVariable Long chatRoomId)
    {
        User user = oAuth2User.getUser();
        chatService.exitChatRoom(user, chatRoomId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("채팅방을 성공적으로 나갔습니다.")
                        .data(null)
                        .build());
    }

    // 특정 채팅방의 대여중 상태인 약속 존재 여부 확인
    @GetMapping("/chatroom/{chatRoomId}/in-progress-appointment/exist")
    public ResponseEntity<SuccessResponseDto> isInProgressAppointmentExist(@AuthenticationPrincipal CustomOAuth2User oAuth2User,
                                                                           @PathVariable Long chatRoomId)
    {
        User user = oAuth2User.getUser();
        boolean data = chatService.isInProgressAppointmentExist(user, chatRoomId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("특정 채팅방의 대여중 상태인 약속 존재 여부를 성공적으로 확인하였습니다.")
                        .data(data)
                        .build());
    }
}
