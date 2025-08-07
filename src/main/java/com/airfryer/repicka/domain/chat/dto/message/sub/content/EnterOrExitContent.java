package com.airfryer.repicka.domain.chat.dto.message.sub.content;

import com.airfryer.repicka.domain.chat.dto.message.sub.SubMessageContent;
import com.airfryer.repicka.domain.chat.entity.ChatRoom;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@SuperBuilder
public class EnterOrExitContent extends SubMessageContent
{
    private Long chatRoomId;            // 채팅방 ID

    private Long requesterId;                   // 요청자 ID
    private Long ownerId;                       // 제품 소유자 ID
    private Boolean isRequesterOnline;          // 요청자 온라인 여부
    private Boolean isOwnerOnline;              // 제품 소유자 온라인 여부
    private LocalDateTime requesterLastEnterAt; // 요청자 마지막 입장 시점
    private LocalDateTime ownerLastEnterAt;     // 제품 소유자 마지막 입장 시점

    public static EnterOrExitContent from(ChatRoom chatRoom,
                                          boolean isRequesterOnline,
                                          boolean isOwnerOnline,
                                          LocalDateTime requesterLastEnterAt,
                                          LocalDateTime ownerLastEnterAt)
    {
        return EnterOrExitContent.builder()
                .chatRoomId(chatRoom.getId())
                .requesterId(chatRoom.getRequester().getId())
                .ownerId(chatRoom.getOwner().getId())
                .isRequesterOnline(isRequesterOnline)
                .isOwnerOnline(isOwnerOnline)
                .requesterLastEnterAt(requesterLastEnterAt)
                .ownerLastEnterAt(ownerLastEnterAt)
                .build();
    }
}
