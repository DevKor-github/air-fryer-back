package com.airfryer.repicka.domain.chat.dto.message.sub.content;

import com.airfryer.repicka.domain.chat.entity.Chat;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class ChatContentByUser extends ChatContent
{
    private Long chatRoomId;

    public static ChatContentByUser from(Chat chat)
    {
        return ChatContentByUser.builder()
                .chatId(chat.getId().toHexString())
                .userId(chat.getUserId())
                .content(chat.getContent())
                .isPick(chat.getIsPick())
                .createdAt(chat.getId().getDate())
                .chatRoomId(chat.getChatRoomId())
                .build();
    }
}
