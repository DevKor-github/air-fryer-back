package com.airfryer.repicka.domain.chat.dto.message.sub;

import com.airfryer.repicka.domain.chat.dto.message.sub.content.MessageContent;
import com.airfryer.repicka.domain.chat.dto.message.sub.content.MessageContentWithRoom;
import com.airfryer.repicka.domain.chat.dto.message.sub.content.EnterOrExitContent;
import com.airfryer.repicka.domain.chat.entity.Chat;
import com.airfryer.repicka.domain.chat.entity.ChatRoom;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class SubChat
{
    private SubChatType type;        // 구독 메시지 타입
    private SubChatContent message;  // 구독 메시지 내용

    public static SubChat createEnterMessage(ChatRoom chatRoom,
                                             boolean isRequesterOnline,
                                             boolean isOwnerOnline,
                                             LocalDateTime requesterLastEnterAt,
                                             LocalDateTime ownerLastEnterAt)
    {
        return SubChat.builder()
                .type(SubChatType.ENTER)
                .message(EnterOrExitContent.from(chatRoom, isRequesterOnline, isOwnerOnline, requesterLastEnterAt,  ownerLastEnterAt))
                .build();
    }

    public static SubChat createExitMessage(ChatRoom chatRoom,
                                            boolean isRequesterOnline,
                                            boolean isOwnerOnline,
                                            LocalDateTime requesterLastEnterAt,
                                            LocalDateTime ownerLastEnterAt)
    {
        return SubChat.builder()
                .type(SubChatType.EXIT)
                .message(EnterOrExitContent.from(chatRoom, isRequesterOnline, isOwnerOnline, requesterLastEnterAt,  ownerLastEnterAt))
                .build();
    }

    public static SubChat createChatMessage(Chat chat)
    {
        return SubChat.builder()
                .type(SubChatType.CHAT)
                .message(MessageContent.from(chat))
                .build();
    }

    public static SubChat createChatMessageWithRoom(Chat chat)
    {
        return SubChat.builder()
                .type(SubChatType.CHAT)
                .message(MessageContentWithRoom.from(chat))
                .build();
    }
}
