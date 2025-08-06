package com.airfryer.repicka.domain.chat.dto.message.sub;

import com.airfryer.repicka.domain.chat.entity.Chat;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class ChatMessageWithRoom extends ChatMessage
{
    private Long chatRoomId;

    public static ChatMessageWithRoom from(Chat chat)
    {
        return ChatMessageWithRoom.builder()
                .chatId(chat.getId().toHexString())
                .userId(chat.getUserId())
                .content(chat.getContent())
                .isPick(chat.getIsPick())
                .createdAt(chat.getId().getDate())
                .chatRoomId(chat.getChatRoomId())
                .build();
    }
}
