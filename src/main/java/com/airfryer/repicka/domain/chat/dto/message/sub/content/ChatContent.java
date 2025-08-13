package com.airfryer.repicka.domain.chat.dto.message.sub.content;

import com.airfryer.repicka.domain.chat.dto.message.sub.SubMessageContent;
import com.airfryer.repicka.domain.chat.entity.Chat;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@Getter
@SuperBuilder
public class ChatContent extends SubMessageContent
{
    private String chatId;              // 채팅 ID
    private Long userId;                // 사용자 ID
    private String content;             // 내용
    private Boolean isPick;             // PICK 여부
    private Chat.PickInfo pickInfo;     // PICK 정보
    private Date createdAt;             // 채팅 생성 날짜

    public static ChatContent from(Chat chat)
    {
        return ChatContent.builder()
                .chatId(chat.getId().toHexString())
                .userId(chat.getUserId())
                .content(chat.getContent())
                .isPick(chat.getIsPick())
                .pickInfo(chat.getPickInfo())
                .createdAt(chat.getId().getDate())
                .build();
    }
}
