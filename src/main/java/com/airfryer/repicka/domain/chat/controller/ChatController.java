package com.airfryer.repicka.domain.chat.controller;

import com.airfryer.repicka.common.response.SuccessResponseDto;
import com.airfryer.repicka.common.security.oauth2.CustomOAuth2User;
import com.airfryer.repicka.domain.appointment.dto.OfferRentalAppointmentReq;
import com.airfryer.repicka.domain.chat.dto.EnterChatRoomRes;
import com.airfryer.repicka.domain.chat.service.ChatService;
import com.airfryer.repicka.domain.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ChatController
{
    private final ChatService chatService;

    private final SimpMessageSendingOperations template;

    // TODO: 나의 채팅 리스트 조회

    // TODO: 제품 페이지에서 채팅방 입장

    // 나의 채팅 페이지에서 채팅방 입장
    @PostMapping("/chatroom/{chatRoomId}/enter")
    public ResponseEntity<SuccessResponseDto> enterChatRoom(@AuthenticationPrincipal CustomOAuth2User oAuth2User,
                                                            @PathVariable Long chatRoomId)
    {
        User user = oAuth2User.getUser();
        EnterChatRoomRes data = chatService.enterChatRoom(user, chatRoomId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("채팅방에 성공적으로 입장하였습니다.")
                        .data(data)
                        .build());
    }

    // TODO: 채팅 전송

    // TODO: 채팅방 나가기
}
