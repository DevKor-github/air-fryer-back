package com.airfryer.repicka.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SendChatDto
{
    private Long chatRoomId;    // 채팅 ID
    private String content;     // 내용
}
