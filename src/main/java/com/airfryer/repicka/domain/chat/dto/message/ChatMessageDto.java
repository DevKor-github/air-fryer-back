package com.airfryer.repicka.domain.chat.dto.message;

import com.airfryer.repicka.domain.chat.entity.Chat;
import com.airfryer.repicka.domain.user.entity.User;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Getter
@SuperBuilder
public class ChatMessageDto
{
    private String chatId;      // 채팅 ID
    private Long userId;        // 사용자 ID
    private String content;     // 내용
    private Boolean isPick;     // PICK 여부
    private Boolean isRead;     // 읽음 여부
    private Date createdAt;     // 채팅 생성 날짜

    public static ChatMessageDto from(Chat chat, User me, LocalDateTime opponentLastEnterAt)
    {
        // 채팅 생성 시점
        LocalDateTime createdAt = LocalDateTime.ofInstant(chat.getId().getDate().toInstant(), ZoneId.systemDefault());

        // 상대가 보낸 채팅이거나, 상대방이 마지막으로 채팅방에 입장한 시점이 해당 채팅 생성 시점보다 나중이면 읽은 것으로 간주
        boolean isRead = !me.getId().equals(chat.getUserId()) || !opponentLastEnterAt.isBefore(createdAt);

        return ChatMessageDto.builder()
                .chatId(chat.getId().toHexString())
                .userId(chat.getUserId())
                .content(chat.getContent())
                .isPick(chat.getIsPick())
                .isRead(isRead)
                .createdAt(chat.getId().getDate())
                .build();
    }

    public static ChatMessageDto from(Chat chat)
    {
        return ChatMessageDto.builder()
                .chatId(chat.getId().toHexString())
                .userId(chat.getUserId())
                .content(chat.getContent())
                .isPick(chat.getIsPick())
                .isRead(null)
                .createdAt(chat.getId().getDate())
                .build();
    }
}
