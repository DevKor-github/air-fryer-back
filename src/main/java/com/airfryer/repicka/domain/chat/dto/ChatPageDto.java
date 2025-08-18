package com.airfryer.repicka.domain.chat.dto;

import com.airfryer.repicka.domain.chat.dto.message.sub.content.MessageContent;
import com.airfryer.repicka.domain.chat.entity.Chat;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChatPageDto
{
    private List<MessageContent> messages;     // 채팅 리스트
    private String cursorId;                // 채팅: 커서 ID
    private Boolean hasNext;                // 채팅: 다음 페이지가 존재하는가?

    public static ChatPageDto of(List<Chat> chatList, String cursorId, boolean hasNext)
    {
        return ChatPageDto.builder()
                .messages(chatList.stream().map(MessageContent::from).toList())
                .cursorId(cursorId)
                .hasNext(hasNext)
                .build();
    }
}
