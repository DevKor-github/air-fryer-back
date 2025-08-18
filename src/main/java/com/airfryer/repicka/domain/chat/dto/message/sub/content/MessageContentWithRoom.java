package com.airfryer.repicka.domain.chat.dto.message.sub.content;

import com.airfryer.repicka.domain.chat.entity.Chat;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class MessageContentWithRoom extends MessageContent
{
    private Long chatRoomId;

    public static MessageContentWithRoom from(Chat chat)
    {
        return MessageContentWithRoom.builder()
                .chatId(chat.getId().toHexString())
                .userId(chat.getUserId())
                .content(chat.getContent())
                .isNotification(chat.getIsNotification())
                .isPick(chat.getIsPick())
                .createdAt(chat.getId().getDate())
                .chatRoomId(chat.getChatRoomId())
                .build();
    }
}
