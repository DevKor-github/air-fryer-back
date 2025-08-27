package com.airfryer.repicka.domain.chat.dto.message.sub;

import com.airfryer.repicka.domain.chat.dto.message.sub.content.ChatContent;
import com.airfryer.repicka.domain.chat.dto.message.sub.content.ChatContentByUser;
import com.airfryer.repicka.domain.chat.dto.message.sub.content.EnterOrExitContent;
import com.airfryer.repicka.domain.chat.dto.message.sub.content.UnreadChatCountContent;
import com.airfryer.repicka.domain.chat.entity.Chat;
import com.airfryer.repicka.domain.chat.entity.ChatRoom;
import com.airfryer.repicka.domain.user.entity.user.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class SubMessage
{
    private SubMessageType type;        // 구독 메시지 타입
    private SubMessageContent message;  // 구독 메시지 내용

    public static SubMessage createEnterMessage(ChatRoom chatRoom,
                                                boolean isRequesterOnline,
                                                boolean isOwnerOnline,
                                                LocalDateTime requesterLastEnterAt,
                                                LocalDateTime ownerLastEnterAt)
    {
        return SubMessage.builder()
                .type(SubMessageType.ENTER)
                .message(EnterOrExitContent.from(chatRoom, isRequesterOnline, isOwnerOnline, requesterLastEnterAt,  ownerLastEnterAt))
                .build();
    }

    public static SubMessage createExitMessage(ChatRoom chatRoom,
                                               boolean isRequesterOnline,
                                               boolean isOwnerOnline,
                                               LocalDateTime requesterLastEnterAt,
                                               LocalDateTime ownerLastEnterAt)
    {
        return SubMessage.builder()
                .type(SubMessageType.EXIT)
                .message(EnterOrExitContent.from(chatRoom, isRequesterOnline, isOwnerOnline, requesterLastEnterAt,  ownerLastEnterAt))
                .build();
    }

    public static SubMessage createChatMessage(Chat chat)
    {
        return SubMessage.builder()
                .type(SubMessageType.CHAT)
                .message(ChatContent.from(chat))
                .build();
    }

    public static SubMessage createChatMessageByUser(ChatRoom chatRoom, User me, Chat mostRecentChat, int unreadChatCount)
    {
        return SubMessage.builder()
                .type(SubMessageType.CHAT)
                .message(ChatContentByUser.from(chatRoom, me, mostRecentChat, unreadChatCount))
                .build();
    }

    public static SubMessage createUnreadChatCountMessage(User user)
    {
        return SubMessage.builder()
                .type(SubMessageType.UNREAD_CHAT_COUNT)
                .message(UnreadChatCountContent.from(user))
                .build();
    }
}
