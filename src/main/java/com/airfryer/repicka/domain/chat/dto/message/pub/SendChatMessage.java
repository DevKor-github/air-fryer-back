package com.airfryer.repicka.domain.chat.dto.message.pub;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SendChatMessage
{
    private Long chatRoomId;    // 채팅방 ID
    private String content;     // 내용
}
