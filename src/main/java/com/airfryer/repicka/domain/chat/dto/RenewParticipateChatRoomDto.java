package com.airfryer.repicka.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RenewParticipateChatRoomDto
{
    private Long chatRoomId;    // 채팅방 ID
}
