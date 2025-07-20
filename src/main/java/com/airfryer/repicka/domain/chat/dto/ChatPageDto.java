package com.airfryer.repicka.domain.chat.dto;

import com.airfryer.repicka.domain.chat.entity.Chat;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.bson.types.ObjectId;

import java.util.List;

@Getter
@Builder
public class ChatPageDto
{
    private List<Content> contents;     // 채팅 정보 리스트
    private ObjectId cursorId;          // 채팅: 커서 ID
    private Boolean hasNext;            // 채팅: 다음 페이지가 존재하는가?

    public static ChatPageDto of(List<Chat> chatList, ObjectId cursorId, boolean hasNext)
    {
        return ChatPageDto.builder()
                .contents(chatList.stream().map(Content::from).toList())
                .cursorId(cursorId)
                .hasNext(hasNext)
                .build();
    }

    // 채팅 정보
    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    private static class Content
    {
        private ObjectId chatId;    // 채팅 ID
        private Long userId;        // 사용자 ID
        private String content;     // 내용

        private static Content from(Chat chat)
        {
            return Content.builder()
                    .chatId(chat.getId())
                    .userId(chat.getUserId())
                    .content(chat.getContent())
                    .build();
        }
    }
}
