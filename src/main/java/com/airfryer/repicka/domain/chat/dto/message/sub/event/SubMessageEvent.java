package com.airfryer.repicka.domain.chat.dto.message.sub.event;

import com.airfryer.repicka.domain.chat.dto.message.sub.SubMessage;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubMessageEvent
{
    private Long userId;
    private String destination;
    private SubMessage message;
}
