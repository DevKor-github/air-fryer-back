package com.airfryer.repicka.common.configuration.websocket;

import com.airfryer.repicka.common.security.oauth2.CustomOAuth2User;
import com.airfryer.repicka.domain.chat.dto.message.sub.SubMessageDto;
import com.airfryer.repicka.domain.chat.dto.message.sub.event.SubMessageEvent;
import com.airfryer.repicka.domain.chat.entity.ChatRoom;
import com.airfryer.repicka.domain.chat.entity.ParticipateChatRoom;
import com.airfryer.repicka.domain.chat.repository.ChatRoomRepository;
import com.airfryer.repicka.domain.chat.repository.ParticipateChatRoomRepository;
import com.airfryer.repicka.domain.chat.service.MappingSubWithRoomManager;
import com.airfryer.repicka.domain.chat.service.OnlineStatusManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.List;
import java.util.Objects;

@Configuration
@RequiredArgsConstructor
public class WebSocketEventHandler
{
    private final ChatRoomRepository chatRoomRepository;
    private final ParticipateChatRoomRepository participateChatRoomRepository;

    private final OnlineStatusManager onlineStatusManager;
    private final MappingSubWithRoomManager mappingSubWithRoomManager;

    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleSubMessage(SubMessageEvent event)
    {
        if(event.getUserId() == null) {
            messagingTemplate.convertAndSend(event.getDestination(), event.getMessage());
        } else {
            messagingTemplate.convertAndSendToUser(event.getUserId().toString(), event.getDestination(), event.getMessage());
        }
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event)
    {
        String sessionId = event.getSessionId();
        List<Long> chatRoomIdList = mappingSubWithRoomManager.get(sessionId);

        for(Long chatRoomId : chatRoomIdList)
        {
            StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

            // 사용자 ID
            Authentication auth = (Authentication) accessor.getUser();
            CustomOAuth2User customOAuth2User = (CustomOAuth2User) Objects.requireNonNull(auth).getPrincipal();
            Long userId = customOAuth2User.getUser().getId();

            ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                    .orElseThrow(() -> new RuntimeException("ChatRoom not found"));

            // 온라인 상태 변경 및 퇴장 메시지 전송
            onlineStatusManager.markUserOffline(chatRoomId, userId);

            // 참여 정보 가져오기
            ParticipateChatRoom requester = participateChatRoomRepository.findByChatRoomIdAndParticipantId(chatRoomId, chatRoom.getRequester().getId()).orElseThrow();
            ParticipateChatRoom owner = participateChatRoomRepository.findByChatRoomIdAndParticipantId(chatRoomId, chatRoom.getOwner().getId()).orElseThrow();

            SubMessageDto exitMessage = SubMessageDto.createExitMessage(
                    chatRoom,
                    onlineStatusManager.isUserOnline(chatRoomId, chatRoom.getRequester().getId()),
                    onlineStatusManager.isUserOnline(chatRoomId, chatRoom.getOwner().getId()),
                    requester.getLastEnterAt(),
                    owner.getLastEnterAt()
            );

            handleSubMessage(SubMessageEvent.builder()
                    .userId(null)
                    .destination("/sub/chatroom/" + chatRoomId)
                    .message(exitMessage)
                    .build());
        }

        // 매핑 정보 제거
        mappingSubWithRoomManager.delete(sessionId);
    }
}
