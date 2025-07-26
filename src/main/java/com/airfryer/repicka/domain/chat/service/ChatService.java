package com.airfryer.repicka.domain.chat.service;

import com.airfryer.repicka.common.exception.CustomException;
import com.airfryer.repicka.common.exception.CustomExceptionCode;
import com.airfryer.repicka.domain.appointment.service.AppointmentService;
import com.airfryer.repicka.domain.chat.dto.ChatMessageDto;
import com.airfryer.repicka.domain.chat.dto.EnterChatRoomRes;
import com.airfryer.repicka.domain.chat.dto.SendChatDto;
import com.airfryer.repicka.domain.chat.entity.Chat;
import com.airfryer.repicka.domain.chat.entity.ChatRoom;
import com.airfryer.repicka.domain.chat.repository.ChatRepository;
import com.airfryer.repicka.domain.chat.repository.ChatRoomRepository;
import com.airfryer.repicka.domain.item_image.entity.ItemImage;
import com.airfryer.repicka.domain.item_image.repository.ItemImageRepository;
import com.airfryer.repicka.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService
{
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRepository chatRepository;
    private final ItemImageRepository itemImageRepository;

    private final AppointmentService appointmentService;

    private final SimpMessagingTemplate messagingTemplate;

    // 나의 채팅 페이지에서 채팅방 입장
    @Transactional(readOnly = true)
    public EnterChatRoomRes enterChatRoom(User user, Long chatRoomId, int pageSize)
    {
        /// 채팅방 조회

        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.CHATROOM_NOT_FOUND, chatRoomId));

        /// 예외 처리

        // 이미 종료된 채팅방인지 확인
        if(chatRoom.getIsFinished()) {
            throw new CustomException(CustomExceptionCode.ALREADY_FINISHED_CHATROOM, null);
        }

        // 채팅방 관계자인지 확인
        if(!chatRoom.getRequester().equals(user) && !chatRoom.getOwner().equals(user)) {
            throw new CustomException(CustomExceptionCode.NOT_CHATROOM_PARTICIPANT, null);
        }

        /// 제품 썸네일 URL 조회

        // 썸네일 데이터 조회
        ItemImage thumbnail = itemImageRepository.findFirstByItemId(chatRoom.getItem().getId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.ITEM_IMAGE_NOT_FOUND, chatRoom.getItem().getId()));

        /// 제품의 현재 대여 및 구매 가능 여부 조회

        boolean isAvailable = appointmentService.isItemAvailableOnDate(chatRoom.getItem().getId(), LocalDateTime.now());

        /// 채팅 페이지 조회

        // Pageable 객체 생성
        Pageable pageable = PageRequest.of(0, pageSize + 1);

        // 채팅 페이지 조회
        List<Chat> chatPage = chatRepository.findByChatRoomIdOrderByIdDesc(chatRoomId, pageable);

        if(chatPage == null) {
            chatPage = new ArrayList<>();
        }

        /// 채팅 페이지 정보 계산

        // 채팅: 다음 페이지가 존재하는가?
        boolean hasNext = chatPage.size() > pageSize;

        // 커서 데이터
        ObjectId chatCursorId = hasNext ? chatPage.getLast().getId() : null;

        // 다음 페이지가 존재한다면, 마지막 아이템 제거
        if(hasNext) {
            chatPage.removeLast();
        }

        /// 데이터 반환

        return EnterChatRoomRes.of(
                chatRoom,
                user,
                thumbnail.getFileKey(),
                chatPage,
                chatCursorId,
                hasNext,
                isAvailable
        );
    }

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
                .build();

        chatRepository.save(chat);

        /// 구독자에게 메시지 전송

        ChatMessageDto message = ChatMessageDto.builder()
                .chatId(chat.getId())
                .userId(user.getId())
                .content(dto.getContent())
                .build();

        // 구독자에게 메시지 전송
        try {
            messagingTemplate.convertAndSend("/sub/chatroom/" + dto.getChatRoomId(), message);
        } catch (Exception e) {
            throw new CustomException(CustomExceptionCode.INTERNAL_CHAT_ERROR, e.getMessage());
        }
    }
}
