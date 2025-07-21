package com.airfryer.repicka.domain.chat.dto;

import com.airfryer.repicka.domain.chat.entity.Chat;
import lombok.Builder;
import lombok.Getter;
import org.bson.types.ObjectId;

import java.util.List;

@Getter
@Builder
public class ChatPageDto
{
    private List<ChatMessageDto> messages;  // 채팅 리스트
    private ObjectId cursorId;              // 채팅: 커서 ID
    private Boolean hasNext;                // 채팅: 다음 페이지가 존재하는가?

    public static ChatPageDto of(List<Chat> chatList, ObjectId cursorId, boolean hasNext)
    {
        return ChatPageDto.builder()
                .messages(chatList.stream().map(ChatMessageDto::from).toList())
                .cursorId(cursorId)
                .hasNext(hasNext)
                .build();
    }
}
