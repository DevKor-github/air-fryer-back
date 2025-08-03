package com.airfryer.repicka.domain.chat.service;

import com.airfryer.repicka.common.exception.CustomException;
import com.airfryer.repicka.common.exception.CustomExceptionCode;
import com.airfryer.repicka.common.firebase.dto.FCMNotificationReq;
import com.airfryer.repicka.common.firebase.service.FCMService;
import com.airfryer.repicka.common.firebase.type.NotificationType;
import com.airfryer.repicka.domain.chat.dto.ChatMessageDto;
import com.airfryer.repicka.domain.chat.dto.ChatMessageWithRoomDto;
import com.airfryer.repicka.domain.chat.dto.RenewParticipateChatRoomDto;
import com.airfryer.repicka.domain.chat.dto.SendChatDto;
import com.airfryer.repicka.domain.chat.entity.Chat;
import com.airfryer.repicka.domain.chat.entity.ChatRoom;
import com.airfryer.repicka.domain.chat.entity.ParticipateChatRoom;
import com.airfryer.repicka.domain.chat.repository.ChatRepository;
import com.airfryer.repicka.domain.chat.repository.ChatRoomRepository;
import com.airfryer.repicka.domain.chat.repository.ParticipateChatRoomRepository;
import com.airfryer.repicka.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ChatWebSocketService
{
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRepository chatRepository;
    private final ParticipateChatRoomRepository participateChatRoomRepository;

    private final SimpMessagingTemplate messagingTemplate;

    private final FCMService fcmService;

    // 채팅 전송
    @Transactional
    public void sendMessage(User user, SendChatDto dto)
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
        if(!chatRoom.getRequester().equals(user) && !chatRoom.getOwner().equals(user)) {
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
                .content(dto.getContent())
                .isPick(false)
                .build();

        chatRepository.save(chat);

        /// 채팅방 마지막 채팅 시점 갱신

        chatRoom.renewLastChatAt();

        /// 구독자에게 소켓 메시지 및 푸시 알림 전송

        // 메시지 생성
        ChatMessageDto message = ChatMessageDto.from(chat);
        ChatMessageWithRoomDto messageWithRoom = ChatMessageWithRoomDto.from(chat);

        // 채팅 상대방 정보
        User opponent = Objects.equals(chatRoom.getRequester().getId(), user.getId()) ? chatRoom.getOwner() : chatRoom.getRequester();

        try {

            /// 소켓 메시지 전송

            // 채팅방 구독자에게 소켓 메시지 전송
            messagingTemplate.convertAndSend("/sub/chatroom/" + dto.getChatRoomId(), message);

            // 채팅 상대방에게 소켓 메시지 전송
            messagingTemplate.convertAndSendToUser(
                    opponent.getId().toString(),
                    "/sub",
                    messageWithRoom
            );

            /// 푸시 알림 전송

            // 푸시 알림 전송
            FCMNotificationReq notificationReq = FCMNotificationReq.of(NotificationType.CHAT_MESSAGE, chat.getId().toHexString(), user.getNickname());
            fcmService.sendNotification(opponent.getFcmToken(), notificationReq);

        } catch (Exception e) {
            throw new CustomException(CustomExceptionCode.INTERNAL_CHAT_ERROR, e.getMessage());
        }

        /// 채팅 상대방의 읽지 않은 채팅 개수 증가

        // 상대방의 채팅방 참여 정보 조회
        ParticipateChatRoom opponentParticipateChatRoom = participateChatRoomRepository.findByChatRoomIdAndParticipantId(dto.getChatRoomId(), opponent.getId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_CHATROOM_NOT_FOUND, null));

        // 읽지 않은 채팅 개수 증가
        opponentParticipateChatRoom.increaseUnreadChatCount();
    }

    // 채팅방 참여 정보 갱신
    @Transactional
    public void renewParticipateChatRoom(User user, RenewParticipateChatRoomDto dto)
    {
        /// 채팅방 조회

        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(dto.getChatRoomId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.CHATROOM_NOT_FOUND, dto.getChatRoomId()));

        /// 예외 처리

        // 채팅방 관계자인지 확인
        if(!chatRoom.getRequester().equals(user) && !chatRoom.getOwner().equals(user)) {
            throw new CustomException(CustomExceptionCode.NOT_CHATROOM_PARTICIPANT, null);
        }

        /// 채팅방 참여 정보 갱신

        // 채팅방 참여 정보 조회
        ParticipateChatRoom participateChatRoom = participateChatRoomRepository.findByChatRoomIdAndParticipantId(dto.getChatRoomId(), user.getId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_CHATROOM_NOT_FOUND, null));

        // 채팅방 참여 정보 갱신
        participateChatRoom.renew();
    }
}
