package com.airfryer.repicka.domain.chat.dto;

import com.airfryer.repicka.domain.chat.entity.Chat;
import lombok.Builder;
import lombok.Getter;
import org.bson.types.ObjectId;

@Getter
@Builder
public class ChatMessageDto
{
    private ObjectId chatId;    // 채팅 ID
    private Long userId;        // 사용자 ID
    private String content;     // 내용
    private Boolean isPick;     // PICK 여부

    public static ChatMessageDto from(Chat chat)
    {
        return ChatMessageDto.builder()
                .chatId(chat.getId())
                .userId(chat.getUserId())
                .content(chat.getContent())
                .isPick(chat.getIsPick())
                .build();
    }
}
