package com.airfryer.repicka.domain.chat.dto.message.sub;

import com.airfryer.repicka.domain.chat.entity.Chat;
import com.airfryer.repicka.domain.chat.entity.ChatRoom;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class SubMessageDto
{
    private SubMessageType type;    // 구독 메시지 타입
    private SubMessage message;     // 구독 메시지

    public static SubMessageDto createEnterMessage(ChatRoom chatRoom,
                                                   boolean isRequesterOnline,
                                                   boolean isOwnerOnline,
                                                   LocalDateTime requesterLastEnterAt,
                                                   LocalDateTime ownerLastEnterAt)
    {
        return SubMessageDto.builder()
                .type(SubMessageType.ENTER)
                .message(EnterOrExitMessage.from(chatRoom, isRequesterOnline, isOwnerOnline, requesterLastEnterAt,  ownerLastEnterAt))
                .build();
    }

    public static SubMessageDto createExitMessage(ChatRoom chatRoom,
                                                  boolean isRequesterOnline,
                                                  boolean isOwnerOnline,
                                                  LocalDateTime requesterLastEnterAt,
                                                  LocalDateTime ownerLastEnterAt)
    {
        return SubMessageDto.builder()
                .type(SubMessageType.EXIT)
                .message(EnterOrExitMessage.from(chatRoom, isRequesterOnline, isOwnerOnline, requesterLastEnterAt,  ownerLastEnterAt))
                .build();
    }

    public static SubMessageDto createChatMessage(Chat chat)
    {
        return SubMessageDto.builder()
                .type(SubMessageType.EXIT)
                .message(ChatMessage.from(chat))
                .build();
    }

    public static SubMessageDto createChatMessageWithRoom(Chat chat)
    {
        return SubMessageDto.builder()
                .type(SubMessageType.EXIT)
                .message(ChatMessageWithRoom.from(chat))
                .build();
    }
}
