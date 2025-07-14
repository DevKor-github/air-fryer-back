package com.airfryer.repicka.domain.chat.service;

import com.airfryer.repicka.common.exception.CustomException;
import com.airfryer.repicka.common.exception.CustomExceptionCode;
import com.airfryer.repicka.domain.appointment.entity.Appointment;
import com.airfryer.repicka.domain.appointment.repository.AppointmentRepository;
import com.airfryer.repicka.domain.chat.dto.EnterChatRoomRes;
import com.airfryer.repicka.domain.chat.entity.Chat;
import com.airfryer.repicka.domain.chat.entity.ChatRoom;
import com.airfryer.repicka.domain.chat.repository.ChatRepository;
import com.airfryer.repicka.domain.chat.repository.ChatRoomRepository;
import com.airfryer.repicka.domain.item_image.ItemImageService;
import com.airfryer.repicka.domain.item_image.entity.ItemImage;
import com.airfryer.repicka.domain.item_image.repository.ItemImageRepository;
import com.airfryer.repicka.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService
{
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRepository chatRepository;
    private final AppointmentRepository appointmentRepository;
    private final ItemImageRepository itemImageRepository;

    private final ItemImageService itemImageService;

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

        // 썸네일 URL 조회
        String thumbnailUrl = itemImageService.getFullImageUrl(thumbnail);

        /// 채팅 페이지 조회

        // Pageable 객체 생성
        Pageable pageable = PageRequest.of(0, pageSize + 1);

        // 채팅 페이지 조회
        List<Chat> chatPage = chatRepository
                .findFirstPageByChatRoomId(chatRoomId, pageable)
                .collectList()
                .block();

        if(chatPage == null) {
            chatPage = new ArrayList<>();
        }

        /// 채팅 페이지 정보 계산

        // 채팅: 다음 페이지가 존재하는가?
        boolean hasNext = chatPage.size() > pageSize;

        // 커서 데이터
        LocalDateTime chatCursorCreatedAt = hasNext ? chatPage.getLast().getCreatedAt() : null;
        ObjectId chatCursorId = hasNext ? chatPage.getLast().getId() : null;

        // 다음 페이지가 존재한다면, 마지막 아이템 제거
        if(hasNext) {
            chatPage = chatPage.subList(0, pageSize);
        }

        /// 약속 리스트 조회

        // 약속 리스트 조회
        List<Appointment> appointmentList = appointmentRepository.findByItemIdAndOwnerIdAndRequesterId(
                chatRoom.getItem().getId(),
                chatRoom.getOwner().getId(),
                chatRoom.getRequester().getId()
        );

        // 약속 리스트 정렬
        // AppointmentState 기준 : PENDING > CONFIRMED > IN_PROGRESS > SUCCESS > 그 외
        // 동일한 AppointmentState 내에서는 rentalDate 오름차순
        appointmentList.sort(
                Comparator.comparing(
                        (Appointment a) -> {
                            return switch (a.getState()) {
                                case PENDING -> 1;
                                case CONFIRMED -> 2;
                                case IN_PROGRESS -> 3;
                                case SUCCESS -> 4;
                                default -> 5;
                            };
                        })
                        .thenComparing(Appointment::getRentalDate, Comparator.naturalOrder())
        );

        return EnterChatRoomRes.of(
                chatRoom,
                user,
                thumbnailUrl,
                chatPage,
                appointmentList,
                chatCursorCreatedAt,
                chatCursorId,
                hasNext
        );
    }
}
