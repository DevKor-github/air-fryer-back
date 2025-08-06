package com.airfryer.repicka.domain.chat.dto.message.sub;

import com.airfryer.repicka.domain.chat.entity.ChatRoom;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class SubMessageDto
{
    private SubMessageType type;    // 구독 메시지 타입
    private SubMessage message;     // 구독 메시지

    public static SubMessageDto createEnterMessage(ChatRoom chatRoom, boolean isRequesterOnline, boolean isOwnerOnline)
    {
        return SubMessageDto.builder()
                .type(SubMessageType.ENTER)
                .message(EnterOrExitMessage.from(chatRoom, isRequesterOnline, isOwnerOnline))
                .build();
    }

    public static SubMessageDto createExitMessage(ChatRoom chatRoom, boolean isRequesterOnline, boolean isOwnerOnline)
    {
        return SubMessageDto.builder()
                .type(SubMessageType.EXIT)
                .message(EnterOrExitMessage.from(chatRoom, isRequesterOnline, isOwnerOnline))
                .build();
    }
}
