package com.airfryer.repicka.common.configuration.chat;

import com.airfryer.repicka.common.exception.CustomException;
import com.airfryer.repicka.common.exception.CustomExceptionCode;
import com.airfryer.repicka.common.security.oauth2.CustomOAuth2User;
import com.airfryer.repicka.domain.chat.dto.message.sub.SubMessageDto;
import com.airfryer.repicka.domain.chat.dto.message.sub.event.SubMessageEvent;
import com.airfryer.repicka.domain.chat.entity.ChatRoom;
import com.airfryer.repicka.domain.chat.repository.ChatRoomRepository;
import com.airfryer.repicka.domain.chat.service.MapSubscribeWithRoomManager;
import com.airfryer.repicka.domain.chat.service.OnlineStatusManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
public class WebSocketAccessInterceptor implements ChannelInterceptor
{
    private final ChatRoomRepository chatRoomRepository;
    private final OnlineStatusManager onlineStatusManager;
    private final MapSubscribeWithRoomManager mapSubscribeWithRoomManager;

    private final ApplicationEventPublisher applicationEventPublisher;

    // STOMP 메시지가 서버로 들어올 때 실행하는 메서드
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel)
    {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // 동작 커맨드
        StompCommand command = Objects.requireNonNull(accessor).getCommand();

        if(StompCommand.SUBSCRIBE.equals(command))
        {
            // 구독 경로
            String destination = accessor.getDestination();

            if(destination != null && destination.startsWith("/sub/chatroom/"))
            {
                // 채팅방 ID 및 사용자 ID 조회
                Long chatRoomId = Long.parseLong(destination.replace("/sub/chatroom/", ""));
                Long userId = getUserId(accessor);

                // 채팅방 조회
                ChatRoom chatRoom = findChatRoom(chatRoomId, userId);

                // (구독 ID, 채팅방 ID) 매핑 정보 저장
                String subId = accessor.getSubscriptionId();
                mapSubscribeWithRoomManager.mapSubscribeWithRoom(accessor.getSessionId(), subId, chatRoomId);

                // 온라인 상태 변경 및 구독자들에게 입장 메시지 전송
                onlineStatusManager.markUserOnline(chatRoomId, userId);
                sendEnterOrExitMessage(chatRoom, true);
            }
        }
        else if(StompCommand.UNSUBSCRIBE.equals(command))
        {
            // 구독 ID 조회
            String subId = accessor.getSubscriptionId();

            // 채팅방 ID 조회
            Long chatRoomId = mapSubscribeWithRoomManager.getChatRoomIdBySubId(accessor.getSessionId(), subId);

            if(chatRoomId != null)
            {
                // 사용자 ID 조회
                Long userId = getUserId(accessor);

                // 채팅방 조회
                ChatRoom chatRoom = findChatRoom(chatRoomId, userId);

                // 온라인 상태 변경 및 구독자들에게 퇴장 메시지 전송
                onlineStatusManager.markUserOffline(chatRoomId, userId);
                sendEnterOrExitMessage(chatRoom, false);

                // 매핑 정보 제거
                mapSubscribeWithRoomManager.removeMapping(accessor.getSessionId(), subId);
            }
        }

        return message;
    }

    // 사용자 ID 조회
    private Long getUserId(StompHeaderAccessor accessor)
    {
        Authentication auth = (Authentication) accessor.getUser();
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) Objects.requireNonNull(auth).getPrincipal();
        return customOAuth2User.getUser().getId();
    }

    // 채팅방 조회
    private ChatRoom findChatRoom(Long chatRoomId, Long userId)
    {
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.CHATROOM_NOT_FOUND, chatRoomId));

        // 채팅방 참가자인지 확인
        if(!chatRoom.getRequester().getId().equals(userId) && !chatRoom.getOwner().getId().equals(userId)) {
            throw new CustomException(CustomExceptionCode.NOT_CHATROOM_PARTICIPANT, null);
        }

        return chatRoom;
    }

    // 채팅방 입장/퇴장 메시지 전송
    private void sendEnterOrExitMessage(ChatRoom chatRoom, boolean isEnter)
    {
        // 온라인 여부 조회
        boolean isRequesterOnline = onlineStatusManager.isUserOnline(chatRoom.getId(), chatRoom.getRequester().getId());
        boolean isOwnerOnline = onlineStatusManager.isUserOnline(chatRoom.getId(), chatRoom.getOwner().getId());

        // 입장/퇴장 메시지 생성
        SubMessageDto message = isEnter ?
                SubMessageDto.createEnterMessage(chatRoom, isRequesterOnline, isOwnerOnline) :
                SubMessageDto.createExitMessage(chatRoom, isRequesterOnline, isOwnerOnline);

        // 입장/퇴장 이벤트 발생
        applicationEventPublisher.publishEvent(SubMessageEvent.builder()
                .userId(null)
                .destination("/sub/chatroom/" + chatRoom.getId())
                .message(message)
                .build());
    }
}
