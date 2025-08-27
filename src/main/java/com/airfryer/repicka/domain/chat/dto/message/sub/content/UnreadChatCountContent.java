package com.airfryer.repicka.domain.chat.dto.message.sub.content;

import com.airfryer.repicka.domain.chat.dto.message.sub.SubMessageContent;
import com.airfryer.repicka.domain.user.entity.user.User;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class UnreadChatCountContent extends SubMessageContent
{
    private int unreadChatCount;    // 읽지 않은 채팅 개수

    public static UnreadChatCountContent from(User user)
    {
        return UnreadChatCountContent.builder()
                .unreadChatCount(user.getUnreadChatCount())
                .build();
    }
}
