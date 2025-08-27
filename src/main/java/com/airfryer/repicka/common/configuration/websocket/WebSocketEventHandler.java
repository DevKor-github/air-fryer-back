package com.airfryer.repicka.common.configuration.websocket;

import com.airfryer.repicka.common.exception.CustomException;
import com.airfryer.repicka.common.exception.CustomExceptionCode;
import com.airfryer.repicka.common.security.oauth2.CustomOAuth2User;
import com.airfryer.repicka.domain.chat.dto.message.sub.SubMessage;
import com.airfryer.repicka.domain.chat.dto.message.sub.event.SubMessageEvent;
import com.airfryer.repicka.domain.chat.entity.ChatRoom;
import com.airfryer.repicka.domain.chat.entity.ParticipateChatRoom;
import com.airfryer.repicka.domain.chat.repository.ChatRoomRepository;
import com.airfryer.repicka.domain.chat.repository.ParticipateChatRoomRepository;
import com.airfryer.repicka.domain.chat.service.MappingSubWithRoomManager;
import com.airfryer.repicka.domain.chat.service.OnlineStatusManager;
import com.airfryer.repicka.domain.user.entity.user.User;
import com.airfryer.repicka.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.List;
import java.util.Objects;

@Configuration
@RequiredArgsConstructor
public class WebSocketEventHandler
{
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ParticipateChatRoomRepository participateChatRoomRepository;

    private final OnlineStatusManager onlineStatusManager;
    private final MappingSubWithRoomManager mappingSubWithRoomManager;

    private final SimpMessagingTemplate messagingTemplate;

    // 채팅방 구독 메시지 전송 이벤트
    @EventListener
    public void handleSubMessage(SubMessageEvent event)
    {
        if(event.getUserId() == null) {
            messagingTemplate.convertAndSend(event.getDestination(), event.getMessage());
        } else {
            messagingTemplate.convertAndSendToUser(event.getUserId().toString(), event.getDestination(), event.getMessage());
        }
    }

    // 사용자별 구독 초기 메시지 전송 이벤트
    @EventListener
    public void handleUserSubscribe(SessionSubscribeEvent event)
    {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();

        // 사용자 정보 가져오기
        Authentication auth = (Authentication) accessor.getUser();
        if(auth == null) {
            return;
        }
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) auth.getPrincipal();

        Long userId = customOAuth2User.getUser().getId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.USER_NOT_FOUND, userId));

        // 사용자별 구독일 때만 초기 메시지 전송
        if(destination != null && destination.startsWith("/user/"))
        {
            SubMessage message = SubMessage.createUnreadChatCountMessage(user);
            messagingTemplate.convertAndSendToUser(userId.toString(), "/sub", message);
        }
    }

    // 예기치 못한 소켓 연결 해제 이벤트
    @EventListener
    @Transactional
    public void handleSessionDisconnect(SessionDisconnectEvent event)
    {
        String sessionId = event.getSessionId();
        List<Long> chatRoomIdList = mappingSubWithRoomManager.get(sessionId);

        for(Long chatRoomId : chatRoomIdList)
        {
            StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

            // 사용자 ID 조회
            Authentication auth = (Authentication) accessor.getUser();
            CustomOAuth2User customOAuth2User = (CustomOAuth2User) Objects.requireNonNull(auth).getPrincipal();
            Long userId = customOAuth2User.getUser().getId();

            // 채팅방 조회
            ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                    .orElseThrow(() -> new CustomException(CustomExceptionCode.CHATROOM_NOT_FOUND, chatRoomId));

            // 채팅방 참여 정보 조회
            ParticipateChatRoom participateChatRoom = participateChatRoomRepository.findByChatRoomIdAndParticipantId(chatRoom.getId(), userId)
                    .orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_CHATROOM_NOT_FOUND, null));

            // 온라인 상태 변경 및 퇴장 메시지 전송
            onlineStatusManager.markUserOffline(chatRoomId, userId);

            // 채팅방 참여 정보 갱신
            participateChatRoom.renew();

            // 참여 정보 가져오기
            ParticipateChatRoom requester = participateChatRoomRepository.findByChatRoomIdAndParticipantId(chatRoomId, chatRoom.getRequester().getId()).orElseThrow();
            ParticipateChatRoom owner = participateChatRoomRepository.findByChatRoomIdAndParticipantId(chatRoomId, chatRoom.getOwner().getId()).orElseThrow();

            // 채팅방 퇴장 메시지
            SubMessage exitMessage = SubMessage.createExitMessage(
                    chatRoom,
                    onlineStatusManager.isUserOnline(chatRoomId, chatRoom.getRequester().getId()),
                    onlineStatusManager.isUserOnline(chatRoomId, chatRoom.getOwner().getId()),
                    requester.getLastReadAt(),
                    owner.getLastReadAt()
            );

            // 채팅방 퇴장 이벤트 발생
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
