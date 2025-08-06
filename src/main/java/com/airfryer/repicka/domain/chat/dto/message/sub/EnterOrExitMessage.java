package com.airfryer.repicka.domain.chat.dto.message.sub;

import com.airfryer.repicka.domain.chat.entity.ChatRoom;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class EnterOrExitMessage extends SubMessage
{
    private Long chatRoomId;            // 채팅방 ID

    private Long requesterId;           // 요청자 ID
    private Boolean isRequesterOnline;  // 요청자 온라인 여부
    private Long ownerId;               // 제품 소유자 ID
    private Boolean isOwnerOnline;      // 제품 소유자 온라인 여부

    static EnterOrExitMessage from(ChatRoom chatRoom, boolean isRequesterOnline, boolean isOwnerOnline)
    {
        return EnterOrExitMessage.builder()
                .chatRoomId(chatRoom.getId())
                .requesterId(chatRoom.getRequester().getId())
                .isRequesterOnline(isRequesterOnline)
                .ownerId(chatRoom.getOwner().getId())
                .isOwnerOnline(isOwnerOnline)
                .build();
    }
}
