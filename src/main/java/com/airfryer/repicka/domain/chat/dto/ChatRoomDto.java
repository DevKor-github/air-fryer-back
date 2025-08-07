package com.airfryer.repicka.domain.chat.dto;

import com.airfryer.repicka.domain.chat.entity.Chat;
import com.airfryer.repicka.domain.chat.entity.ChatRoom;
import com.airfryer.repicka.domain.chat.entity.ParticipateChatRoom;
import com.airfryer.repicka.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Builder
public class ChatRoomDto
{
    private Long chatRoomId;                    // 채팅방 ID
    private Long myUserId;                      // 나의 사용자 ID
    private Long opponentUserId;                // 상대방의 사용자 ID
    private String opponentNickname;            // 상대방의 닉네임
    private String opponentProfileImageUrl;     // 상대방의 프로필 이미지 URL
    private Boolean isOpponentKorean;           // 상대방의 고려대 인증 여부
    private Boolean isOpponentOnline;           // 상대방의 온라인 여부
    private LocalDateTime opponentLastEnterAt;  // 상대방의 마지막 채팅방 입장 시점
    private Boolean isFinished;                 // 채팅방 종료 여부
    private String mostRecentChatContent;       // 가장 최근 채팅 내용
    private String mostRecentChatNickname;      // 가장 최근 채팅을 보낸 사용자 닉네임
    private Boolean mostRecentChatIsPick;       // 가장 최근 채팅 PICK 메시지 여부
    private LocalDateTime lastChatAt;           // 마지막 채팅 시점
    private int unreadChatCount;                // 읽지 않은 채팅 개수

    public static ChatRoomDto from(ChatRoom chatRoom, User me, Chat mostRecentChat, int unreadChatCount, boolean isOpponentOnline, ParticipateChatRoom opponentParticipateChatRoom)
    {
        User opponent = Objects.equals(chatRoom.getRequester().getId(), me.getId()) ? chatRoom.getOwner() : chatRoom.getRequester();

        return ChatRoomDto.builder()
                .chatRoomId(chatRoom.getId())
                .myUserId(me.getId())
                .opponentUserId(opponent.getId())
                .opponentNickname(opponent.getNickname())
                .opponentProfileImageUrl(opponent.getProfileImageUrl())
                .isOpponentKorean(opponent.getIsKoreaUnivVerified())
                .isOpponentOnline(isOpponentOnline)
                .opponentLastEnterAt(opponentParticipateChatRoom.getLastEnterAt())
                .isFinished(chatRoom.getIsFinished())
                .mostRecentChatContent(mostRecentChat != null ? mostRecentChat.getContent() : null)
                .mostRecentChatNickname(mostRecentChat != null ? mostRecentChat.getNickname() : null)
                .mostRecentChatIsPick(mostRecentChat != null ? mostRecentChat.getIsPick() : null)
                .lastChatAt(chatRoom.getLastChatAt())
                .unreadChatCount(unreadChatCount)
                .build();
    }

    // 상대방의 온라인 정보가 필요하지 않을 때
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
                .isOpponentOnline(null)
                .opponentLastEnterAt(null)
                .isFinished(chatRoom.getIsFinished())
                .mostRecentChatContent(mostRecentChat != null ? mostRecentChat.getContent() : null)
                .mostRecentChatNickname(mostRecentChat != null ? mostRecentChat.getNickname() : null)
                .mostRecentChatIsPick(mostRecentChat != null ? mostRecentChat.getIsPick() : null)
                .lastChatAt(chatRoom.getLastChatAt())
                .unreadChatCount(unreadChatCount)
                .build();
    }
}
