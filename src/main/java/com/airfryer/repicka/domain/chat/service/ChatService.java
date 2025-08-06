package com.airfryer.repicka.domain.chat.service;

import com.airfryer.repicka.common.exception.CustomException;
import com.airfryer.repicka.common.exception.CustomExceptionCode;
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
import com.airfryer.repicka.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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

    /// 서비스

    // 나의 채팅 페이지에서 채팅방 입장
    @Transactional
    public EnterChatRoomRes enterChatRoom(User user, Long chatRoomId, int pageSize)
    {
        /// 채팅방 조회

        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.CHATROOM_NOT_FOUND, chatRoomId));

        /// 예외 처리

        // 채팅방 관계자인지 확인
        if(!chatRoom.getRequester().equals(user) && !chatRoom.getOwner().equals(user)) {
            throw new CustomException(CustomExceptionCode.NOT_CHATROOM_PARTICIPANT, null);
        }

        /// 채팅방 참여 정보 갱신

        // 채팅방 참여 정보 조회
        ParticipateChatRoom participateChatRoom = participateChatRoomRepository.findByChatRoomIdAndParticipantId(chatRoomId, user.getId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_CHATROOM_NOT_FOUND, null));

        // 채팅방 참여 정보 갱신
        participateChatRoom.renew();

        /// 제품 썸네일 URL 조회

        // 썸네일 데이터 조회
        ItemImage thumbnail = itemImageRepository.findFirstByItemId(chatRoom.getItem().getId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.ITEM_IMAGE_NOT_FOUND, chatRoom.getItem().getId()));

        /// 채팅 페이지 조회

        // Pageable 객체 생성
        Pageable pageable = PageRequest.of(0, pageSize + 1);

        // 채팅 페이지 조회
        List<Chat> chatPage = chatRepository.findByChatRoomIdOrderByIdDesc(chatRoomId, pageable);

        /// 채팅 페이지 정보 계산

        // 채팅: 다음 페이지가 존재하는가?
        boolean hasNext = chatPage.size() > pageSize;

        // 커서 데이터
        String chatCursorId = hasNext ? chatPage.getLast().getId().toHexString() : null;

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

        // 채팅방 페이지
        List<ChatRoom> chatRoomList;

        // 채팅방 페이지 조회
        if(dto.getCursorLastChatAt() == null || dto.getCursorId() == null) {
            chatRoomList = chatRoomRepository.findFirstPageByUserId(user.getId(), pageable);
        } else {
            chatRoomList = chatRoomRepository.findPageByUserId(user.getId(), dto.getCursorLastChatAt(), dto.getCursorId(), pageable);
        }

        /// 데이터 반환

        return createChatRoomListDto(user, chatRoomList, dto.getPageSize());
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
        if(!item.getOwner().equals(user)) {
            throw new CustomException(CustomExceptionCode.NOT_ITEM_OWNER, null);
        }

        /// 채팅방 페이지 조회

        // Pageable 객체 생성
        Pageable pageable = PageRequest.of(0, dto.getPageSize() + 1);

        // 채팅방 페이지
        List<ChatRoom> chatRoomList;

        // 채팅방 페이지 조회
        if(dto.getCursorLastChatAt() == null || dto.getCursorId() == null) {
            chatRoomList = chatRoomRepository.findFirstPageByItemId(itemId, pageable);
        } else {
            chatRoomList = chatRoomRepository.findPageByItemId(itemId, dto.getCursorLastChatAt(), dto.getCursorId(), pageable);
        }

        /// 데이터 반환

        return createChatRoomListDto(user, chatRoomList, dto.getPageSize());
    }

    // 채팅 불러오기
    @Transactional(readOnly = true)
    public ChatPageDto loadChat(User user, Long chatRoomId, int pageSize, String cursorId)
    {
        /// 채팅방 조회

        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.CHATROOM_NOT_FOUND, chatRoomId));

        /// 예외 처리

        // 채팅방 관계자인지 확인
        if(!chatRoom.getRequester().equals(user) && !chatRoom.getOwner().equals(user)) {
            throw new CustomException(CustomExceptionCode.NOT_CHATROOM_PARTICIPANT, null);
        }

        /// 채팅 페이지 조회

        // Pageable 객체 생성
        Pageable pageable = PageRequest.of(0, pageSize + 1);

        // 채팅 페이지 조회
        List<Chat> chatPage = chatRepository.findByChatRoomIdAndIdLessThanEqualOrderByIdDesc(chatRoomId, new ObjectId(cursorId), pageable);

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

    /// 공통 로직

    // ChatRoomListDto 생성
    private ChatRoomListDto createChatRoomListDto(User user, List<ChatRoom> chatRoomList, int pageSize)
    {
        /// 커서 데이터 계산

        // 채팅방: 다음 페이지가 존재하는가?
        boolean hasNext = chatRoomList.size() > pageSize;

        // 채팅방: 커서 데이터
        LocalDateTime cursorLastChatAt = hasNext ? chatRoomList.getLast().getLastChatAt() : null;
        Long cursorId = hasNext ? chatRoomList.getLast().getId() : null;

        // 다음 페이지가 존재한다면, 마지막 아이템 제거
        if(hasNext) {
            chatRoomList.removeLast();
        }

        /// ChatRoomDto 리스트 생성

        List<ChatRoomDto> chatRoomDtoList = chatRoomList.stream().map(chatRoom -> {

            // 가장 최근 채팅
            Optional<Chat> chatOptional = chatRepository.findFirstByChatRoomIdOrderByIdDesc(chatRoom.getId());

            // 채팅방 참여 정보
            ParticipateChatRoom participateChatRoom = participateChatRoomRepository.findByChatRoomIdAndParticipantId(chatRoom.getId(), user.getId())
                    .orElseThrow(() -> new CustomException(CustomExceptionCode.PARTICIPATE_CHATROOM_NOT_FOUND, null));

            if(chatOptional.isPresent())
            {
                return ChatRoomDto.from(
                        chatRoom,
                        user,
                        chatOptional.get(),
                        participateChatRoom.getUnreadChatCount()
                );
            }
            else
            {
                return ChatRoomDto.from(
                        chatRoom,
                        user
                );
            }

        }).toList();

        /// 데이터 반환

        return ChatRoomListDto.builder()
                .chatRooms(chatRoomDtoList)
                .hasNext(hasNext)
                .cursorLastChatAt(cursorLastChatAt)
                .cursorId(cursorId)
                .build();
    }
}
