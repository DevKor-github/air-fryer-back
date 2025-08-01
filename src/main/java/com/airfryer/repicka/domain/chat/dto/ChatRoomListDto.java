package com.airfryer.repicka.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ChatRoomListDto
{
    private List<ChatRoomDto> chatRooms;    // 채팅방 정보

    private boolean hasNext;                // 채팅방: 다음 페이지가 존재하는가?
    private LocalDateTime cursorLastChatAt; // 채팅방: 마지막 채팅 시점
    private Long cursorId;                  // 채팅방: 커서 ID
}
