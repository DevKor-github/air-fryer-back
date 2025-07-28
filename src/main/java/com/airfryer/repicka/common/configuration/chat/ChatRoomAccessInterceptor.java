package com.airfryer.repicka.common.configuration.chat;

import com.airfryer.repicka.common.exception.CustomException;
import com.airfryer.repicka.common.exception.CustomExceptionCode;
import com.airfryer.repicka.common.security.oauth2.CustomOAuth2User;
import com.airfryer.repicka.domain.chat.entity.ChatRoom;
import com.airfryer.repicka.domain.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ChatRoomAccessInterceptor implements ChannelInterceptor
{
    private final ChatRoomRepository chatRoomRepository;

    // 메시지를 전송하기 전, 실행할 메서드
    // 채팅방 구독의 경우, 채팅방 참가자인지 확인합니다
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel)
    {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // 구독일 경우
        if(StompCommand.SUBSCRIBE.equals(Objects.requireNonNull(accessor).getCommand()))
        {
            // 구독 경로
            String destination = accessor.getDestination();

            // 채팅방 구독인 경우
            if(destination != null && destination.startsWith("/sub/chatroom/"))
            {
                // 채팅방 ID 추출
                Long chatRoomId = extractChatRoomId(destination);

                // 사용자 ID 추출
                Authentication authentication = (Authentication) accessor.getUser();
                CustomOAuth2User customOAuth2User = (CustomOAuth2User) Objects.requireNonNull(authentication).getPrincipal();
                Long userId = customOAuth2User.getUser().getId();

                // 채팅방 참가자가 아니라면, 예외 처리
                if(!isParticipant(chatRoomId, userId)) {
                    throw new CustomException(CustomExceptionCode.NOT_CHATROOM_PARTICIPANT, null);
                }
            }
        }

        return message;
    }

    // 채팅방 구독 경로에서 채팅방 ID 추출
    private Long extractChatRoomId(String dest) {
        return Long.parseLong(dest.replace("/sub/chatroom/", ""));
    }

    // 채팅방 참가자인지 확인
    private boolean isParticipant(Long chatRoomId, Long userId)
    {
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.CHATROOM_NOT_FOUND, chatRoomId));

        // 채팅방 참가자인지 확인
        return (chatRoom.getRequester().getId().equals(userId) || chatRoom.getOwner().getId().equals(userId));
    }
}
