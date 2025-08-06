package com.airfryer.repicka.domain.chat.dto.message.sub;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubMessageDto
{
    private SubMessageType type;    // 구독 메시지 타입
    private SubMessage message;     // 구독 메시지
}
