package com.airfryer.repicka.domain.chat.dto;

import com.airfryer.repicka.domain.chat.entity.Chat;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class ChatMessageWithRoomDto extends ChatMessageDto
{
    private Long chatRoomId;

    public static ChatMessageWithRoomDto from(Chat chat)
    {
        return ChatMessageWithRoomDto.builder()
                .chatId(chat.getId().toHexString())
                .userId(chat.getUserId())
                .content(chat.getContent())
                .isPick(chat.getIsPick())
                .isRead(null)
                .createdAt(chat.getId().getDate())
                .chatRoomId(chat.getChatRoomId())
                .build();
    }
}
