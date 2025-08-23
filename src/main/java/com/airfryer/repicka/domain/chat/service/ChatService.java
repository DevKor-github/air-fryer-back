package com.airfryer.repicka.domain.chat.service;

import com.airfryer.repicka.common.exception.CustomException;
import com.airfryer.repicka.common.exception.CustomExceptionCode;
import com.airfryer.repicka.common.firebase.dto.FCMNotificationReq;
import com.airfryer.repicka.common.firebase.service.FCMService;
import com.airfryer.repicka.domain.notification.entity.NotificationType;
import com.airfryer.repicka.common.redis.RedisService;
import com.airfryer.repicka.domain.appointment.entity.Appointment;
import com.airfryer.repicka.domain.appointment.entity.AppointmentState;
import com.airfryer.repicka.domain.appointment.repository.AppointmentRepository;
import com.airfryer.repicka.domain.chat.dto.ChatPageDto;
import com.airfryer.repicka.domain.chat.dto.EnterChatRoomRes;
import com.airfryer.repicka.domain.chat.dto.*;
import com.airfryer.repicka.domain.chat.entity.Chat;
import com.airfryer.repicka.domain.chat.entity.ChatRoom;
import com.airfryer.repicka.domain.chat.entity.ParticipateChatRoom;
import com.airfryer.repicka.domain.chat.repository.ChatRepository;
import com.airfryer.repicka.domain.chat.repository.ChatRoomRepository;
import com.airfryer.repicka.domain.chat.repository.ParticipateChatRoomRepository;
import com.airfryer.repicka.domain.item.entity.Item;
import com.airfryer.repicka.domain.item.repository.ItemRepository;
import com.airfryer.repicka.domain.item_image.entity.ItemImage;
import com.airfryer.repicka.domain.item_image.repository.ItemImageRepository;
import com.airfryer.repicka.domain.user.entity.user.User;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService
{
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRepository chatRepository;
    private final ParticipateChatRoomRepository participateChatRoomRepository;
    private final ItemRepository itemRepository;
    private final ItemImageRepository itemImageRepository;
    private final AppointmentRepository appointmentRepository;

    private final OnlineStatusManager onlineStatusManager;

    private final ChatWebSocketService chatWebSocketService;

    private final RedisService delayedQueueService;
    private final FCMService fcmService;

    /// 서비스

    // 채팅방 생성
    @Transactional
    public EnterChatRoomRes createChatRoom(User requester, Long itemId)
    {
        /// 제품 조회

        // 제품 조회
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.ITEM_NOT_FOUND, itemId));

        // 이미 삭제된 제품인 경우, 예외 처리
        if(item.getIsDeleted()) {
            throw new CustomException(CustomExceptionCode.ALREADY_DELETED_ITEM, null);
        }

        /// 채팅방 조회 (존재하지 않으면 생성)

        ChatRoom chatRoom = createChatRoom(item, requester);

        /// 채팅방 재입장

        // 채팅방 참여 정보 조회
        ParticipateChatRoom participateChatRoom = participateChatRoomRepository.findByChatRoomIdAndParticipantId(chatRoom.getId(), requester.getId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_CHATROOM_NOT_FOUND, null));

        // 이미 채팅방을 나간 경우
        if(participateChatRoom.getHasLeftRoom())
        {
            // 채팅방 재입장 처리
            participateChatRoom.reEnter();

            // 채팅방 재입장 채팅 생성
            Chat reEnterChat = Chat.builder()
                    .chatRoomId(chatRoom.getId())
                    .userId(requester.getId())
                    .nickname(requester.getNickname())
                    .content(requester.getNickname() + " 님께서 채팅방에 재입장하였습니다.")
                    .isNotification(true)
                    .isPick(false)
                    .pickInfo(null)
                    .build();

            // 채팅방 재입장 채팅 전송
            chatWebSocketService.sendMessageChat(requester, chatRoom, reEnterChat);
        }

        /// 채팅방 입장 데이터 반환

        return enterChatRoom(requester, chatRoom, 1);
    }

    // 채팅방 ID로 채팅방에 입장할 때 필요한 데이터를 조회
    @Transactional
    public EnterChatRoomRes enterChatRoom(User user, Long chatRoomId, int pageSize)
    {
        /// 채팅방 조회

        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.CHATROOM_NOT_FOUND, chatRoomId));

        return enterChatRoom(user, chatRoom, pageSize);
    }

    @Transactional
    public EnterChatRoomRes enterChatRoom(User user, ChatRoom chatRoom, int pageSize)
    {
        /// 채팅방 참여 데이터 조회

        ParticipateChatRoom participateChatRoom = participateChatRoomRepository.findByChatRoomIdAndParticipantId(chatRoom.getId(), user.getId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_CHATROOM_NOT_FOUND, null));

        /// 예외 처리

        // 이미 채팅방을 나갔는지 확인
        if(participateChatRoom.getHasLeftRoom()) {
            throw new CustomException(CustomExceptionCode.ALREADY_LEFT_CHATROOM, null);
        }

        /// 제품 썸네일 URL 조회

        // 썸네일 데이터 조회
        ItemImage thumbnail = itemImageRepository.findFirstByItemId(chatRoom.getItem().getId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.ITEM_IMAGE_NOT_FOUND, chatRoom.getItem().getId()));

        /// 채팅 페이지 조회

        // Pageable 객체 생성
        Pageable pageable = PageRequest.of(0, pageSize + 1);

        // 채팅 페이지 조회
        List<Chat> chatPage = chatRepository.findFirstChatList(chatRoom.getId(), participateChatRoom.getLastReEnterAt(), pageable);

        /// 채팅 페이지 정보 계산

        // 채팅: 다음 페이지가 존재하는가?
        boolean hasNext = chatPage.size() > pageSize;

        // 커서 데이터
        String chatCursorId = hasNext ? chatPage.getLast().getId().toHexString() : null;

        // 다음 페이지가 존재한다면, 마지막 아이템 제거
        if(hasNext) {
            chatPage.removeLast();
        }

        /// 상대방의 온라인 정보 조회

        // 상대방 정보
        User opponent = Objects.equals(chatRoom.getRequester().getId(), user.getId()) ? chatRoom.getOwner() : chatRoom.getRequester();

        // 상대방의 온라인 여부 조회
        boolean isOpponentOnline = onlineStatusManager.isUserOnline(chatRoom.getId(), opponent.getId());

        // 상대방의 채팅방 참여 정보 조회
        ParticipateChatRoom opponentParticipateChatRoom = participateChatRoomRepository.findByChatRoomIdAndParticipantId(chatRoom.getId(), opponent.getId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_CHATROOM_NOT_FOUND, null));

        /// 완료되지 않은 약속 조회

        List<Appointment> currentAppointmentOptional = appointmentRepository.findByItemIdAndOwnerIdAndRequesterIdAndStateIn(
                chatRoom.getItem().getId(),
                chatRoom.getOwner().getId(),
                chatRoom.getRequester().getId(),
                List.of(AppointmentState.PENDING, AppointmentState.CONFIRMED, AppointmentState.IN_PROGRESS)
        );

        /// 데이터 반환

        return EnterChatRoomRes.of(
                chatRoom,
                user,
                thumbnail.getFileKey(),
                chatPage,
                isOpponentOnline,
                opponentParticipateChatRoom,
                !currentAppointmentOptional.isEmpty(),
                !currentAppointmentOptional.isEmpty() ? currentAppointmentOptional.getFirst() : null,
                chatCursorId,
                hasNext
        );
    }

    // 내 채팅방 페이지 조회
    @Transactional(readOnly = true)
    public ChatRoomListDto getMyChatRoomPage(User user, GetMyChatRoomPageReq dto)
    {
        /// 채팅방 페이지 조회

        // Pageable 객체 생성
        Pageable pageable = PageRequest.of(0, dto.getPageSize() + 1);

        // 채팅방 참여 정보 페이지
        List<ParticipateChatRoom> participateChatRoomList;

        // 채팅방 페이지 조회
        if(dto.getCursorLastChatAt() == null || dto.getCursorId() == null) {
            participateChatRoomList = participateChatRoomRepository.findFirstPageByUserId(user.getId(), pageable);
        } else {
            participateChatRoomList = participateChatRoomRepository.findPageByUserId(user.getId(), dto.getCursorLastChatAt(), dto.getCursorId(), pageable);
        }

        /// 데이터 반환

        return createChatRoomListDto(
                user,
                participateChatRoomList.stream().map(ParticipateChatRoom::getChatRoom).toList(),
                dto.getPageSize()
        );
    }

    // 내 제품의 채팅방 페이지 조회
    @Transactional(readOnly = true)
    public ChatRoomListDto getMyChatRoomPageByItem(User user, Long itemId, GetMyChatRoomPageReq dto)
    {
        /// 제품 조회

        // 제품 조회
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.ITEM_NOT_FOUND, itemId));

        // 제품 소유자가 아닌 경우, 예외 처리
        if(!Objects.equals(user.getId(), item.getOwner().getId())) {
            throw new CustomException(CustomExceptionCode.NOT_ITEM_OWNER, null);
        }

        /// 채팅방 페이지 조회

        // Pageable 객체 생성
        Pageable pageable = PageRequest.of(0, dto.getPageSize() + 1);

        // 채팅방 참여 정보 페이지
        List<ParticipateChatRoom> participateChatRoomList;

        // 채팅방 페이지 조회
        if(dto.getCursorLastChatAt() == null || dto.getCursorId() == null) {
            participateChatRoomList = participateChatRoomRepository.findFirstPageByUserIdAndItemId(user.getId(), itemId, pageable);
        } else {
            participateChatRoomList = participateChatRoomRepository.findPageByUserIdAndItemId(user.getId(), itemId, dto.getCursorLastChatAt(), dto.getCursorId(), pageable);
        }

        /// 데이터 반환

        return createChatRoomListDto(
                user,
                participateChatRoomList.stream().map(ParticipateChatRoom::getChatRoom).toList(),
                dto.getPageSize()
        );
    }

    // 채팅 불러오기
    @Transactional(readOnly = true)
    public ChatPageDto loadChat(User user, Long chatRoomId, int pageSize, String cursorId)
    {
        /// 채팅방 조회

        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.CHATROOM_NOT_FOUND, chatRoomId));

        /// 채팅방 참여 데이터 조회

        ParticipateChatRoom participateChatRoom = participateChatRoomRepository.findByChatRoomIdAndParticipantId(chatRoom.getId(), user.getId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_CHATROOM_NOT_FOUND, null));

        /// 예외 처리

        // 이미 채팅방을 나갔는지 확인
        if(participateChatRoom.getHasLeftRoom()) {
            throw new CustomException(CustomExceptionCode.ALREADY_LEFT_CHATROOM, null);
        }

        /// 채팅 페이지 조회

        // Pageable 객체 생성
        Pageable pageable = PageRequest.of(0, pageSize + 1);

        // 채팅 페이지 조회
        List<Chat> chatPage = cursorId == null ?
                chatRepository.findFirstChatList(chatRoomId, participateChatRoom.getLastReEnterAt(), pageable):
                chatRepository.findChatList(chatRoomId, new ObjectId(cursorId), participateChatRoom.getLastReEnterAt(), pageable);

        /// 채팅 페이지 정보 계산

        // 채팅: 다음 페이지가 존재하는가?
        boolean hasNext = chatPage.size() > pageSize;

        // 커서 데이터
        String nextCursorId = hasNext ? chatPage.getLast().getId().toHexString() : null;

        // 다음 페이지가 존재한다면, 마지막 아이템 제거
        if(hasNext) {
            chatPage.removeLast();
        }

        /// 데이터 반환

        return ChatPageDto.of(
                chatPage,
                nextCursorId,
                hasNext
        );
    }

    // 채팅방 나가기
    @Transactional
    public void exitChatRoom(User user, Long chatRoomId)
    {
        /// 채팅방 참여 데이터 조회

        // 채팅방 참여 데이터 조회
        ParticipateChatRoom participateChatRoom = participateChatRoomRepository.findByChatRoomIdAndParticipantId(chatRoomId, user.getId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_CHATROOM_NOT_FOUND, null));

        // 이미 채팅방을 나갔는지 확인
        if(participateChatRoom.getHasLeftRoom()) {
            throw new CustomException(CustomExceptionCode.ALREADY_LEFT_CHATROOM, null);
        }

        /// 채팅방 조회

        ChatRoom chatRoom = participateChatRoom.getChatRoom();

        /// 채팅 상대방 조회

        User opponent = Objects.equals(chatRoom.getRequester().getId(), user.getId()) ? chatRoom.getOwner() : chatRoom.getRequester();

        /// 완료되지 않은 약속 취소

        // 완료되지 않은 약속 조회
        List<Appointment> currentAppointmentOptional = appointmentRepository.findByItemIdAndOwnerIdAndRequesterIdAndStateIn(
                chatRoom.getItem().getId(),
                chatRoom.getOwner().getId(),
                chatRoom.getRequester().getId(),
                List.of(AppointmentState.PENDING, AppointmentState.CONFIRMED, AppointmentState.IN_PROGRESS)
        );

        // 완료되지 않은 약속이 존재하는 경우
        if(!currentAppointmentOptional.isEmpty())
        {
            // 완료되지 않은 약속
            Appointment currentAppointment = currentAppointmentOptional.getFirst();

            // 완료되지 않은 약속이 대여 중 상태인 경우, 예외 처리
            if(currentAppointment.getState().equals(AppointmentState.IN_PROGRESS)) {
                throw new CustomException(CustomExceptionCode.IN_PROGRESS_APPOINTMENT_EXIST, null);
            }

            /// 약속 취소 처리

            // 제품의 판매 예정 날짜 초기화
            currentAppointment.getItem().cancelSale();

            // 약속 취소
            currentAppointment.cancel();

            // 약속 알림 발송 예약 취소
            delayedQueueService.cancelDelayedTask("appointment", currentAppointment.getId());

            /// 약속 취소 채팅 및 알림 전송

            // 채팅 생성
            Chat cancelChat = Chat.builder()
                    .chatRoomId(chatRoom.getId())
                    .userId(user.getId())
                    .nickname(user.getNickname())
                    .content(user.getNickname() + " 님께서 약속을 취소하였습니다.")
                    .isNotification(true)
                    .isPick(false)
                    .pickInfo(null)
                    .build();

            // 채팅 전송
            chatWebSocketService.sendMessageChat(user, chatRoom, cancelChat);

            // 푸시 알림 전송
            FCMNotificationReq cancelNotificationReq = FCMNotificationReq.of(NotificationType.APPOINTMENT_CANCEL, currentAppointment.getId().toString(), user.getNickname());
            fcmService.sendNotification(opponent.getFcmToken(), cancelNotificationReq);
        }

        /// 채팅방 나가기 처리

        // 채팅방 나가기
        participateChatRoom.exit();

        /// 채팅방 나가기 채팅 및 알림 전송

        // 채팅 생성
        Chat leaveChat = Chat.builder()
                .chatRoomId(chatRoom.getId())
                .userId(user.getId())
                .nickname(user.getNickname())
                .content(user.getNickname() + " 님께서 채팅방을 나갔습니다.")
                .isNotification(true)
                .isPick(false)
                .pickInfo(null)
                .build();

        // 채팅 전송
        chatWebSocketService.sendMessageChat(user, chatRoom, leaveChat);
    }

    /// 공통 로직

    // ChatRoomListDto 생성
    private ChatRoomListDto createChatRoomListDto(User user, List<ChatRoom> chatRoomList, int pageSize)
    {
        /// 커서 데이터 계산

        boolean hasNext = chatRoomList.size() > pageSize;

        // 채팅방: 커서 데이터
        LocalDateTime cursorLastChatAt = hasNext ? chatRoomList.get(pageSize).getLastChatAt() : null;
        Long cursorId = hasNext ? chatRoomList.get(pageSize).getId() : null;

        // 다음 페이지가 존재한다면, 마지막 아이템 제거
        List<ChatRoom> actualChatRoomList = hasNext ? chatRoomList.subList(0, pageSize) : chatRoomList;

        /// ChatRoomDto 리스트 생성

        List<ChatRoomDto> chatRoomDtoList = actualChatRoomList.stream().map(chatRoom -> {

            // 가장 최근 채팅
            Optional<Chat> chatOptional = chatRepository.findFirstByChatRoomIdOrderByIdDesc(chatRoom.getId());

            // 채팅방 참여 정보
            ParticipateChatRoom participateChatRoom = participateChatRoomRepository.findByChatRoomIdAndParticipantId(chatRoom.getId(), user.getId())
                    .orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_CHATROOM_NOT_FOUND, null));

            return ChatRoomDto.from(
                    chatRoom,
                    user,
                    chatOptional.orElse(null),
                    participateChatRoom.getUnreadChatCount()
            );

        }).toList();

        /// 데이터 반환

        return ChatRoomListDto.builder()
                .chatRooms(chatRoomDtoList)
                .hasNext(hasNext)
                .cursorLastChatAt(cursorLastChatAt)
                .cursorId(cursorId)
                .build();
    }

    // 채팅방 조회 (존재하지 않으면 생성)
    @Transactional
    public ChatRoom createChatRoom(Item item, User requester)
    {
        /// 예외 처리

        // 요청자와 제품 소유자가 다른 사용자인지 체크
        if(Objects.equals(requester.getId(), item.getOwner().getId())) {
            throw new CustomException(CustomExceptionCode.SAME_OWNER_AND_REQUESTER, null);
        }

        // 채팅방 조회
        Optional<ChatRoom> chatRoomOptional = chatRoomRepository.findByItemIdAndOwnerIdAndRequesterId(item.getId(), item.getOwner().getId(), requester.getId());

        // 채팅방이 이미 존재한다면 기존 채팅방 반환
        // 채팅방이 존재하지 않는다면 새로 생성하여 반환
        if(chatRoomOptional.isPresent())
        {
            return chatRoomOptional.get();
        }
        else
        {
            /// 채팅방 생성

            ChatRoom chatRoom = ChatRoom.builder()
                    .item(item)
                    .requester(requester)
                    .owner(item.getOwner())
                    .build();

            chatRoomRepository.save(chatRoom);

            /// 채팅방 참여 정보 생성

            // 요청자 참여 정보 생성
            participateChatRoomRepository.save(ParticipateChatRoom.builder()
                    .chatRoom(chatRoom)
                    .participant(requester)
                    .build());

            // 제품 소유자 참여 정보 생성
            participateChatRoomRepository.save(ParticipateChatRoom.builder()
                    .chatRoom(chatRoom)
                    .participant(item.getOwner())
                    .build());

            /// 제품의 채팅방 개수 증가

            item.addChatRoomCount();

            return chatRoom;
        }
    }
}
