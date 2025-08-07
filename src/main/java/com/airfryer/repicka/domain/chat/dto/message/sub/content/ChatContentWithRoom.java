package com.airfryer.repicka.domain.chat.dto.message.sub.content;

import com.airfryer.repicka.domain.chat.entity.Chat;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class ChatContentWithRoom extends ChatContent
{
    private Long chatRoomId;

    public static ChatContentWithRoom from(Chat chat)
    {
        return ChatContentWithRoom.builder()
                .chatId(chat.getId().toHexString())
                .userId(chat.getUserId())
                .content(chat.getContent())
                .isPick(chat.getIsPick())
                .createdAt(chat.getId().getDate())
                .chatRoomId(chat.getChatRoomId())
                .build();
    }
}
