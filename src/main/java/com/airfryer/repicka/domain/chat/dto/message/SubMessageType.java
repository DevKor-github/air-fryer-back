package com.airfryer.repicka.domain.chat.dto.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SubMessageType
{
    CHAT("CHAT", "채팅"),
    ENTER("ENTER", "채팅방 입장"),
    EXIT("EXIT", "채팅방 퇴장");

    private final String code;
    private final String label;
}
