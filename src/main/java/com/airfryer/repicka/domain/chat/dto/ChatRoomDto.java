package com.airfryer.repicka.domain.chat.dto;

import com.airfryer.repicka.domain.chat.entity.Chat;
import com.airfryer.repicka.domain.chat.entity.ChatRoom;
import com.airfryer.repicka.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Builder
public class ChatRoomDto
{
    private Long chatRoomId;                // 채팅방 ID
    private Long myUserId;                  // 나의 사용자 ID
    private Long opponentUserId;            // 상대방의 사용자 ID
    private String opponentNickname;        // 상대방의 닉네임
    private String opponentProfileImageUrl; // 상대방의 프로필 이미지 URL
    private Boolean isOpponentKorean;       // 상대방의 고려대 인증 여부
    private Boolean isFinished;             // 채팅방 종료 여부
    private String mostRecentChatContent;   // 가장 최근 채팅 내용
    private String mostRecentChatNickname;  // 가장 최근 채팅을 보낸 사용자 닉네임
    private Boolean mostRecentChatIsPick;   // 가장 최근 채팅 PICK 메시지 여부
    private LocalDateTime lastChatAt;       // 마지막 채팅 시점
    private int unreadChatCount;            // 읽지 않은 채팅 개수

    // 가장 최근 채팅이 필요하지 않을 때
    public static ChatRoomDto from(ChatRoom chatRoom, User me)
    {
        User opponent = Objects.equals(chatRoom.getRequester().getId(), me.getId()) ? chatRoom.getOwner() : chatRoom.getRequester();

        return ChatRoomDto.builder()
                .chatRoomId(chatRoom.getId())
                .myUserId(me.getId())
                .opponentUserId(opponent.getId())
                .opponentNickname(opponent.getNickname())
                .opponentProfileImageUrl(opponent.getProfileImageUrl())
                .isOpponentKorean(opponent.getIsKoreaUnivVerified())
                .isFinished(chatRoom.getIsFinished())
                .mostRecentChatContent(null)
                .mostRecentChatNickname(null)
                .mostRecentChatIsPick(null)
                .lastChatAt(chatRoom.getLastChatAt())
                .unreadChatCount(0)
                .build();
    }

    // 가장 최근 채팅이 필요할 때
    public static ChatRoomDto from(ChatRoom chatRoom, User me, Chat mostRecentChat, int unreadChatCount)
    {
        User opponent = Objects.equals(chatRoom.getRequester().getId(), me.getId()) ? chatRoom.getOwner() : chatRoom.getRequester();

        return ChatRoomDto.builder()
                .chatRoomId(chatRoom.getId())
                .myUserId(me.getId())
                .opponentUserId(opponent.getId())
                .opponentNickname(opponent.getNickname())
                .opponentProfileImageUrl(opponent.getProfileImageUrl())
                .isOpponentKorean(opponent.getIsKoreaUnivVerified())
                .isFinished(chatRoom.getIsFinished())
                .mostRecentChatContent(mostRecentChat.getContent())
                .mostRecentChatNickname(mostRecentChat.getNickname())
                .mostRecentChatIsPick(mostRecentChat.getIsPick())
                .lastChatAt(chatRoom.getLastChatAt())
                .unreadChatCount(unreadChatCount)
                .build();
    }
}
