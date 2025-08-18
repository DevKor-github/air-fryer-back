package com.airfryer.repicka.domain.chat.service;

import com.airfryer.repicka.common.exception.CustomException;
import com.airfryer.repicka.common.exception.CustomExceptionCode;
import com.airfryer.repicka.common.firebase.dto.FCMNotificationReq;
import com.airfryer.repicka.common.firebase.service.FCMService;
import com.airfryer.repicka.common.firebase.type.NotificationType;
import com.airfryer.repicka.domain.chat.dto.message.pub.SendChatMessage;
import com.airfryer.repicka.domain.chat.dto.message.sub.SubMessage;
import com.airfryer.repicka.domain.chat.dto.message.sub.event.SubMessageEvent;
import com.airfryer.repicka.domain.chat.entity.Chat;
import com.airfryer.repicka.domain.chat.entity.ChatRoom;
import com.airfryer.repicka.domain.chat.entity.ParticipateChatRoom;
import com.airfryer.repicka.domain.chat.repository.ChatRepository;
import com.airfryer.repicka.domain.chat.repository.ChatRoomRepository;
import com.airfryer.repicka.domain.chat.repository.ParticipateChatRoomRepository;
import com.airfryer.repicka.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketService
{
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRepository chatRepository;
    private final ParticipateChatRoomRepository participateChatRoomRepository;

    private final ApplicationEventPublisher applicationEventPublisher;
    private final OnlineStatusManager onlineStatusManager;

    private final FCMService fcmService;

    // 채팅 전송
    @Transactional
    public void sendChatMessage(User user, SendChatMessage dto)
    {
        /// 채팅방 조회

        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(dto.getChatRoomId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.CHATROOM_NOT_FOUND, dto.getChatRoomId()));

        /// 예외 처리

        // 이미 종료된 채팅방인지 확인
        if(chatRoom.getIsFinished()) {
            throw new CustomException(CustomExceptionCode.ALREADY_FINISHED_CHATROOM, null);
        }

        // 채팅방 관계자인지 확인
        if(!Objects.equals(user.getId(), chatRoom.getRequester().getId()) && !Objects.equals(user.getId(), chatRoom.getOwner().getId())) {
            throw new CustomException(CustomExceptionCode.NOT_CHATROOM_PARTICIPANT, null);
        }

        // 비어있는 메시지인지 확인
        if(dto.getContent().isEmpty()) {
            throw new CustomException(CustomExceptionCode.INVALID_CHAT_MESSAGE, null);
        }

        /// 채팅 저장

        // 채팅 저장
        Chat chat = Chat.builder()
                .chatRoomId(dto.getChatRoomId())
                .userId(user.getId())
                .nickname(user.getNickname())
                .content(dto.getContent())
                .isPick(false)
                .pickInfo(null)
                .build();

        chatRepository.save(chat);

        sendChatMessage(user, chatRoom, chat);
    }

    // 채팅 전송
    @Transactional
    public void sendChatMessage(User user, ChatRoom chatRoom, Chat chat)
    {
        /// 채팅방 마지막 채팅 시점 갱신

        chatRoom.renewLastChatAt();

        /// 채팅 상대방의 읽지 않은 채팅 개수 증가

        // 채팅 상대방 정보 조회
        User opponent = Objects.equals(chatRoom.getRequester().getId(), user.getId()) ? chatRoom.getOwner() : chatRoom.getRequester();

        // 상대방의 채팅방 참여 정보 조회
        ParticipateChatRoom opponentParticipateChatRoom = participateChatRoomRepository.findByChatRoomIdAndParticipantId(chatRoom.getId(), opponent.getId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_CHATROOM_NOT_FOUND, null));

        // 상대방이 오프라인이라면 읽지 않은 채팅 개수 증가
        if(!onlineStatusManager.isUserOnline(chatRoom.getId(), opponent.getId())) {
            opponentParticipateChatRoom.increaseUnreadChatCount();
        }

        /// 구독자에게 소켓 메시지 및 푸시 알림 전송

        // 메시지 생성
        SubMessage message = SubMessage.createChatMessage(chat);
        SubMessage userMessage = SubMessage.createChatMessageByUser(chatRoom, user, chat, opponentParticipateChatRoom.getUnreadChatCount());

        try {

            /// 채팅 전송 이벤트 발생

            applicationEventPublisher.publishEvent(SubMessageEvent.builder()
                    .userId(null)
                    .destination("/sub/chatroom/" + chatRoom.getId())
                    .message(message)
                    .build());

            applicationEventPublisher.publishEvent(SubMessageEvent.builder()
                    .userId(opponent.getId())
                    .destination("/sub")
                    .message(userMessage)
                    .build());

            /// 푸시 알림 전송

            // 푸시 알림 전송
            FCMNotificationReq notificationReq = FCMNotificationReq.of(NotificationType.CHAT_MESSAGE, chat.getId().toHexString(), user.getNickname());
            fcmService.sendNotification(opponent.getFcmToken(), notificationReq);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new CustomException(CustomExceptionCode.INTERNAL_CHAT_ERROR, e.getMessage());
        }
    }
}
