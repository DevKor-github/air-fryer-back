package com.airfryer.repicka.domain.chat.dto;

import com.airfryer.repicka.domain.chat.entity.Chat;
import com.airfryer.repicka.domain.chat.entity.ChatRoom;
import com.airfryer.repicka.domain.item.dto.ItemPreviewDto;
import com.airfryer.repicka.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.bson.types.ObjectId;

import java.util.List;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class EnterChatRoomRes
{
    private ChatRoomInfoDto chatRoom;           // 채팅방 정보
    private List<ChatInfo> chats;               // 채팅 정보 리스트
    private ItemPreviewDto item;                // 제품 정보

    private ObjectId chatCursorId;              // 채팅: 커서 ID
    private Boolean chatHasNext;                // 채팅: 다음 페이지가 존재하는가?

    public static EnterChatRoomRes of(ChatRoom chatRoom,
                                      User me,
                                      String imageUrl,
                                      List<Chat> chatList,
                                      ObjectId chatCursorId,
                                      boolean chatHasNext,
                                      boolean isAvailable)
    {
        return EnterChatRoomRes.builder()
                .chatRoom(ChatRoomInfoDto.from(chatRoom, me))
                .item(ItemPreviewDto.from(chatRoom.getItem(), imageUrl, isAvailable))
                .chats(chatList.stream().map(ChatInfo::from).toList())
                .chatCursorId(chatCursorId)
                .chatHasNext(chatHasNext)
                .build();
    }

    // 채팅 정보
    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    private static class ChatInfo
    {
        private ObjectId chatId;    // 채팅 ID
        private Long userId;        // 사용자 ID
        private String content;     // 내용

        private static ChatInfo from(Chat chat)
        {
            return ChatInfo.builder()
                    .chatId(chat.getId())
                    .userId(chat.getUserId())
                    .content(chat.getContent())
                    .build();
        }
    }
}
