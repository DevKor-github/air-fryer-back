package com.airfryer.repicka.domain.chat.dto.message.sub;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EnterOrExitMessage extends SubMessage
{
    private Long chatRoomId;            // 채팅방 ID

    private Long requesterId;           // 요청자 ID
    private Boolean isRequesterOnline;  // 요청자 온라인 여부
    private Long ownerId;               // 제품 소유자 ID
    private Boolean isOwnerOnline;      // 제품 소유자 온라인 여부
}
