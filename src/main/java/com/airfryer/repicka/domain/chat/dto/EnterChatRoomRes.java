package com.airfryer.repicka.domain.chat.dto;

import com.airfryer.repicka.domain.chat.entity.Chat;
import com.airfryer.repicka.domain.chat.entity.ChatRoom;
import com.airfryer.repicka.domain.item.dto.ItemPreviewDto;
import com.airfryer.repicka.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class EnterChatRoomRes
{
    private ChatRoomDto chatRoom;           // 채팅방 정보
    private ChatPageDto chat;               // 채팅 정보
    private ItemPreviewDto item;            // 제품 정보

    public static EnterChatRoomRes of(ChatRoom chatRoom,
                                      User me,
                                      String imageUrl,
                                      LocalDateTime opponentLastEnterAt,
                                      List<Chat> chatList,
                                      String chatCursorId,
                                      boolean chatHasNext)
    {
        return EnterChatRoomRes.builder()
                .chatRoom(ChatRoomDto.from(chatRoom, me))
                .item(ItemPreviewDto.from(chatRoom.getItem(), imageUrl))
                .chat(ChatPageDto.of(chatList, me, opponentLastEnterAt, chatCursorId, chatHasNext))
                .build();
    }
}
