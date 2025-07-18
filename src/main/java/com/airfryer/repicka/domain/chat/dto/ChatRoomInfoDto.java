package com.airfryer.repicka.domain.chat.dto;

import com.airfryer.repicka.domain.chat.entity.ChatRoom;
import com.airfryer.repicka.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.util.Objects;

@Getter
@Builder
public class ChatRoomInfoDto
{
    private Long chatRoomId;            // 채팅방 ID
    private Long myUserId;              // 나의 사용자 ID
    private Long opponentUserId;        // 상대방의 사용자 ID
    private String opponentNickname;    // 상대방의 닉네임
    private Boolean isOpponentKorean;   // 상대방의 고려대 인증 여부
    private Boolean isFinished;         // 채팅방 종료 여부

    public static ChatRoomInfoDto from(ChatRoom chatRoom, User me)
    {
        User opponent = Objects.equals(chatRoom.getRequester().getId(), me.getId()) ? chatRoom.getOwner() : chatRoom.getRequester();

        return ChatRoomInfoDto.builder()
                .chatRoomId(chatRoom.getId())
                .myUserId(me.getId())
                .opponentUserId(opponent.getId())
                .opponentNickname(opponent.getNickname())
                .isOpponentKorean(opponent.getIsKoreaUnivVerified())
                .isFinished(chatRoom.getIsFinished())
                .build();
    }
}
