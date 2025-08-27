package com.airfryer.repicka.domain.chat.dto.message.sub;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SubMessageType
{
    CHAT("CHAT", "채팅 메시지"),
    ENTER("ENTER", "채팅방 입장"),
    EXIT("EXIT", "채팅방 퇴장"),
    UNREAD_CHAT_COUNT("UNREAD_CHAT_COUNT", "읽지 않은 채팅 개수");

    private final String code;
    private final String label;
}
