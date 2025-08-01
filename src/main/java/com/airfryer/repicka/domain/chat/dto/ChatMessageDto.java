package com.airfryer.repicka.domain.chat.dto;

import com.airfryer.repicka.domain.chat.entity.Chat;
import lombok.Builder;
import lombok.Getter;

import java.util.Date;

@Getter
@Builder
public class ChatMessageDto
{
    private String chatId;      // 채팅 ID
    private Long userId;        // 사용자 ID
    private String content;     // 내용
    private Boolean isPick;     // PICK 여부
    private Date createdAt;     // 채팅 생성 날짜

    public static ChatMessageDto from(Chat chat)
    {
        return ChatMessageDto.builder()
                .chatId(chat.getId().toHexString())
                .userId(chat.getUserId())
                .content(chat.getContent())
                .isPick(chat.getIsPick())
                .createdAt(chat.getId().getDate())
                .build();
    }
}
