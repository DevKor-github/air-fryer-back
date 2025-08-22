package com.airfryer.repicka.common.configuration.websocket;

import com.airfryer.repicka.common.exception.CustomException;
import com.airfryer.repicka.common.exception.CustomExceptionCode;
import com.airfryer.repicka.common.security.oauth2.CustomOAuth2User;
import com.airfryer.repicka.domain.chat.dto.message.sub.SubChat;
import com.airfryer.repicka.domain.chat.dto.message.sub.event.SubMessageEvent;
import com.airfryer.repicka.domain.chat.entity.Chat;
import com.airfryer.repicka.domain.chat.entity.ChatRoom;
import com.airfryer.repicka.domain.chat.entity.ParticipateChatRoom;
import com.airfryer.repicka.domain.chat.repository.ChatRoomRepository;
import com.airfryer.repicka.domain.chat.repository.ParticipateChatRoomRepository;
import com.airfryer.repicka.domain.chat.service.ChatWebSocketService;
import com.airfryer.repicka.domain.chat.service.MappingSubWithRoomManager;
import com.airfryer.repicka.domain.chat.service.OnlineStatusManager;
import com.airfryer.repicka.domain.user.entity.user.User;
import com.airfryer.repicka.domain.user.repository.UserRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class WebSocketAccessInterceptor implements ChannelInterceptor
{
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ParticipateChatRoomRepository participateChatRoomRepository;

    private final ChatWebSocketService chatWebSocketService;

    private final OnlineStatusManager onlineStatusManager;
    private final MappingSubWithRoomManager mappingSubWithRoomManager;

    private final ApplicationEventPublisher applicationEventPublisher;

    // STOMP 메시지가 서버로 들어올 때 실행하는 메서드
    @Override
    @Transactional
    public Message<?> preSend(Message<?> message, MessageChannel channel)
    {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // 동작 커맨드
        StompCommand command = Objects.requireNonNull(accessor).getCommand();

        if(StompCommand.SUBSCRIBE.equals(command))
        {
            // 구독 경로
            String destination = accessor.getDestination();

            // 채팅방 구독의 경우
            if(destination != null && destination.startsWith("/sub/chatroom/"))
            {
                /// 데이터 조회

                // 채팅방 ID 및 사용자 ID 조회
                Long chatRoomId = Long.parseLong(destination.replace("/sub/chatroom/", ""));
                Long userId = getUserId(accessor);

                // 채팅방 조회
                ChatRoom chatRoom = findChatRoom(chatRoomId, userId);

                // 채팅방 참여 정보 조회
                ParticipateChatRoom participateChatRoom = participateChatRoomRepository.findByChatRoomIdAndParticipantId(chatRoom.getId(), userId)
                        .orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_CHATROOM_NOT_FOUND, null));

                /// 채팅방 재입장 처리

                // 이미 채팅방을 나간 경우
                if(participateChatRoom.getHasLeftRoom())
                {
                    /// 데이터 조회

                    // 사용자 조회
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new CustomException(CustomExceptionCode.USER_NOT_FOUND, userId));

                    /// 채팅방 재입장

                    participateChatRoom.reEnter();

                    /// 채팅방 재입장 채팅 전송

                    // 채팅 생성
                    Chat reEnterChat = Chat.builder()
                            .chatRoomId(chatRoom.getId())
                            .userId(user.getId())
                            .nickname(user.getNickname())
                            .content(user.getNickname() + " 님께서 채팅방에 재입장하였습니다.")
                            .isNotification(true)
                            .isPick(false)
                            .pickInfo(null)
                            .build();

                    // 채팅 전송
                    chatWebSocketService.sendMessageChat(user, chatRoom, reEnterChat);
                }

                // (세션 ID + 구독 ID, 채팅방 ID) 매핑 정보 저장
                mappingSubWithRoomManager.set(accessor.getSessionId(), accessor.getSubscriptionId(), chatRoomId);

                // 온라인 상태 변경
                onlineStatusManager.markUserOnline(chatRoomId, userId);

                // 채팅방 참여 정보 갱신
                participateChatRoom.renew();

                // 채팅방 입장 이벤트 발생
                sendEnterOrExitMessage(chatRoom, true);
            }
        }
        else if(StompCommand.UNSUBSCRIBE.equals(command))
        {
            // 채팅방 ID 조회
            Long chatRoomId = mappingSubWithRoomManager.get(accessor.getSessionId(), accessor.getSubscriptionId());

            if(chatRoomId != null)
            {
                // 사용자 ID 조회
                Long userId = getUserId(accessor);

                // 채팅방 조회
                ChatRoom chatRoom = findChatRoom(chatRoomId, userId);

                // 채팅방 참여 정보 조회
                ParticipateChatRoom participateChatRoom = participateChatRoomRepository.findByChatRoomIdAndParticipantId(chatRoom.getId(), userId)
                        .orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_CHATROOM_NOT_FOUND, null));

                // 온라인 상태 변경
                onlineStatusManager.markUserOffline(chatRoomId, userId);

                // 매핑 정보 제거
                mappingSubWithRoomManager.delete(accessor.getSessionId(), accessor.getSubscriptionId());

                // 채팅방 참여 정보 갱신
                participateChatRoom.renew();

                // 채팅방 퇴장 이벤트 발생
                sendEnterOrExitMessage(chatRoom, false);
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
        if(!Objects.equals(userId, chatRoom.getRequester().getId()) && !Objects.equals(userId, chatRoom.getOwner().getId())) {
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

        // 채팅방 참가자 참여 정보 조회
        ParticipateChatRoom requesterParticipateChatRoom = participateChatRoomRepository.findByChatRoomIdAndParticipantId(chatRoom.getId(), chatRoom.getRequester().getId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_CHATROOM_NOT_FOUND, null));
        ParticipateChatRoom ownerParticipateChatRoom = participateChatRoomRepository.findByChatRoomIdAndParticipantId(chatRoom.getId(), chatRoom.getOwner().getId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_CHATROOM_NOT_FOUND, null));

        // 입장/퇴장 메시지 생성
        SubChat message = isEnter ?
                SubChat.createEnterChat(chatRoom, isRequesterOnline, isOwnerOnline, requesterParticipateChatRoom.getLastReadAt(), ownerParticipateChatRoom.getLastReadAt()) :
                SubChat.createExitChat(chatRoom, isRequesterOnline, isOwnerOnline, requesterParticipateChatRoom.getLastReadAt(), ownerParticipateChatRoom.getLastReadAt());

        // 입장/퇴장 이벤트 발생
        applicationEventPublisher.publishEvent(SubMessageEvent.builder()
                .userId(null)
                .destination("/sub/chatroom/" + chatRoom.getId())
                .message(message)
                .build());
    }
}
