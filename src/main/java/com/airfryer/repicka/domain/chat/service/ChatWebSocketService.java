package com.airfryer.repicka.domain.chat.service;

import com.airfryer.repicka.common.exception.CustomException;
import com.airfryer.repicka.common.exception.CustomExceptionCode;
import com.airfryer.repicka.common.firebase.dto.FCMNotificationReq;
import com.airfryer.repicka.common.firebase.service.FCMService;
import com.airfryer.repicka.domain.chat.dto.message.pub.SendChatMessage;
import com.airfryer.repicka.domain.chat.dto.message.sub.SubMessage;
import com.airfryer.repicka.domain.chat.dto.message.sub.event.SubMessageEvent;
import com.airfryer.repicka.domain.chat.entity.Chat;
import com.airfryer.repicka.domain.chat.entity.ChatRoom;
import com.airfryer.repicka.domain.chat.entity.ParticipateChatRoom;
import com.airfryer.repicka.domain.chat.repository.ChatRepository;
import com.airfryer.repicka.domain.chat.repository.ParticipateChatRoomRepository;
import com.airfryer.repicka.domain.user.entity.user.User;
import com.airfryer.repicka.domain.user.repository.UserBlockRepository;
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
    private final ChatRepository chatRepository;
    private final ParticipateChatRoomRepository participateChatRoomRepository;
    private final UserBlockRepository userBlockRepository;

    private final ApplicationEventPublisher applicationEventPublisher;
    private final OnlineStatusManager onlineStatusManager;

    private final FCMService fcmService;

    // 채팅 전송
    @Transactional
    public void sendMessageChat(User user, SendChatMessage dto)
    {
        /// 데이터 조회

        // 채팅방 참여 정보 조회
        ParticipateChatRoom participateChatRoom = participateChatRoomRepository.findByChatRoomIdAndParticipantId(dto.getChatRoomId(), user.getId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_CHATROOM_NOT_FOUND, null));

        // 채팅방 조회
        ChatRoom chatRoom = participateChatRoom.getChatRoom();

        /// 예외 처리

        // 이미 채팅방을 나갔는지 확인
        if(participateChatRoom.getHasLeftRoom()) {
            throw new CustomException(CustomExceptionCode.ALREADY_LEFT_CHATROOM, null);
        }

        // 유저 차단 데이터 존재 여부 체크
        if(userBlockRepository.existsByUserIds(chatRoom.getRequester().getId(), chatRoom.getOwner().getId())) {
            throw new CustomException(CustomExceptionCode.USER_BLOCK_EXIST, null);
        }

        // 채팅방 관계자인지 확인
        if(!Objects.equals(user.getId(), chatRoom.getRequester().getId()) && !Objects.equals(user.getId(), chatRoom.getOwner().getId())) {
            throw new CustomException(CustomExceptionCode.NOT_CHATROOM_PARTICIPANT, null);
        }

        // 비어있는 메시지인지 확인
        if(dto.getContent().isEmpty()) {
            throw new CustomException(CustomExceptionCode.INVALID_CHAT_MESSAGE, null);
        }

        /// 상대방의 채팅방 재입장

        // 채팅 상대방 정보 조회
        User opponent = Objects.equals(chatRoom.getRequester().getId(), user.getId()) ? chatRoom.getOwner() : chatRoom.getRequester();

        // 상대방의 채팅방 참여 정보 조회
        ParticipateChatRoom opponentParticipateChatRoom = participateChatRoomRepository.findByChatRoomIdAndParticipantId(chatRoom.getId(), opponent.getId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_CHATROOM_NOT_FOUND, null));

        // 요청자가 이미 채팅방을 나간 경우
        if(opponentParticipateChatRoom.getHasLeftRoom())
        {
            // 채팅방 재입장 처리
            opponentParticipateChatRoom.reEnter();

            // 채팅방 재입장 채팅 생성
            Chat reEnterChat = Chat.builder()
                    .chatRoomId(chatRoom.getId())
                    .userId(opponent.getId())
                    .nickname(opponent.getNickname())
                    .content(opponent.getNickname() + " 님께서 채팅방에 재입장하였습니다.")
                    .isNotification(true)
                    .isPick(false)
                    .pickInfo(null)
                    .build();

            // 채팅방 재입장 채팅 전송
            sendMessageChat(opponent, chatRoom, reEnterChat);
        }

        /// 채팅 생성

        Chat chat = Chat.builder()
                .chatRoomId(dto.getChatRoomId())
                .userId(user.getId())
                .nickname(user.getNickname())
                .content(dto.getContent())
                .isNotification(false)
                .isPick(false)
                .pickInfo(null)
                .build();

        /// 채팅 전송

        sendMessageChat(user, chatRoom, chat);
    }

    // 채팅 전송
    @Transactional
    public void sendMessageChat(User user, ChatRoom chatRoom, Chat chat)
    {
        /// 채팅 저장

        chatRepository.save(chat);

        /// 채팅방 마지막 채팅 시점 갱신

        chatRoom.renewLastChatAt();

        /// 채팅 상대방의 읽지 않은 채팅 개수 증가

        // 채팅 상대방 정보 조회
        User opponent = Objects.equals(chatRoom.getRequester().getId(), user.getId()) ? chatRoom.getOwner() : chatRoom.getRequester();

        // 상대방의 채팅방 참여 정보 조회
        ParticipateChatRoom opponentParticipateChatRoom = participateChatRoomRepository.findByChatRoomIdAndParticipantId(chatRoom.getId(), opponent.getId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_CHATROOM_NOT_FOUND, null));

        // 알림 메시지가 아니고, 상대방이 오프라인이라면 읽지 않은 채팅 개수 증가
        if(
                !chat.getIsNotification() &&
                !onlineStatusManager.isUserOnline(chatRoom.getId(), opponent.getId())
        ) {
            opponentParticipateChatRoom.increaseUnreadChatCount();
            opponent.increaseUnreadChatCount();
        }

        /// 구독자에게 소켓 메시지 및 푸시 알림 전송

        // 메시지 생성
        SubMessage message = SubMessage.createChatMessage(chat);
        SubMessage userMessage = SubMessage.createChatMessageByUser(chatRoom, opponent, chat, opponentParticipateChatRoom.getUnreadChatCount());
        SubMessage unreadChatCountMessage = SubMessage.createUnreadChatCountMessage(opponent);

        try {

            /// 채팅 전송 이벤트 발생

            // 채팅방 구독에 채팅 웹소켓 메시지 발행
            applicationEventPublisher.publishEvent(SubMessageEvent.builder()
                    .userId(null)
                    .destination("/sub/chatroom/" + chatRoom.getId())
                    .message(message)
                    .build());

            // 사용자별 구독에 채팅 웹소켓 메시지 발행
            applicationEventPublisher.publishEvent(SubMessageEvent.builder()
                    .userId(opponent.getId())
                    .destination("/sub")
                    .message(userMessage)
                    .build());

            applicationEventPublisher.publishEvent(SubMessageEvent.builder()
                    .userId(user.getId())
                    .destination("/sub")
                    .message(userMessage)
                    .build());

            // 상대방에게 사용자 읽지 않은 채팅 개수 웹소켓 메시지 발행
            applicationEventPublisher.publishEvent(SubMessageEvent.builder()
                    .userId(opponent.getId())
                    .destination("/sub")
                    .message(unreadChatCountMessage)
                    .build());

            /// 푸시 알림 전송

            FCMNotificationReq notificationReq = FCMNotificationReq.of(
                    chat.getId().toHexString(),
                    user.getNickname() + " (" + chatRoom.getItem().getTitle() + ")",
                    chat.getContent()
            );
            fcmService.sendNotification(opponent.getFcmToken(), notificationReq);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new CustomException(CustomExceptionCode.INTERNAL_CHAT_ERROR, e.getMessage());
        }
    }
}
