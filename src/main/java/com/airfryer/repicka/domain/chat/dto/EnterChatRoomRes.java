package com.airfryer.repicka.domain.chat.dto;

import com.airfryer.repicka.domain.appointment.dto.CurrentAppointmentRes;
import com.airfryer.repicka.domain.appointment.entity.Appointment;
import com.airfryer.repicka.domain.chat.entity.Chat;
import com.airfryer.repicka.domain.chat.entity.ChatRoom;
import com.airfryer.repicka.domain.chat.entity.ParticipateChatRoom;
import com.airfryer.repicka.domain.item.dto.ItemPreviewDto;
import com.airfryer.repicka.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class EnterChatRoomRes
{
    private ChatRoomDto chatRoom;                       // 채팅방 정보
    private ChatPageDto chat;                           // 채팅 정보
    private ItemPreviewDto item;                        // 제품 정보
    private CurrentAppointmentRes currentAppointment;   // 완료되지 않은 약속 정보

    public static EnterChatRoomRes of(ChatRoom chatRoom,
                                      User me,
                                      String imageUrl,
                                      List<Chat> chatList,
                                      boolean isOpponentOnline,
                                      ParticipateChatRoom opponentParticipateChatRoom,
                                      boolean isCurrentAppointmentPresent,
                                      Appointment currentAppointment,
                                      String chatCursorId,
                                      boolean chatHasNext)
    {
        return EnterChatRoomRes.builder()
                .chatRoom(ChatRoomDto.from(chatRoom, me, isOpponentOnline, opponentParticipateChatRoom))
                .item(ItemPreviewDto.from(chatRoom.getItem(), imageUrl))
                .chat(ChatPageDto.of(chatList, chatCursorId, chatHasNext))
                .currentAppointment(CurrentAppointmentRes.from(isCurrentAppointmentPresent, currentAppointment))
                .build();
    }

    @Getter
    @Builder
    static class ChatRoomDto
    {
        private Long chatRoomId;                    // 채팅방 ID
        private Long myUserId;                      // 나의 사용자 ID
        private Long opponentUserId;                // 상대방의 사용자 ID
        private String opponentNickname;            // 상대방의 닉네임
        private String opponentProfileImageUrl;     // 상대방의 프로필 이미지 URL
        private Boolean isOpponentKorean;           // 상대방의 고려대 인증 여부
        private Boolean isOpponentOnline;           // 상대방의 온라인 여부
        private LocalDateTime opponentLastEnterAt;  // 상대방의 마지막 채팅방 입장 시점
        private LocalDateTime lastChatAt;           // 마지막 채팅 시점

        private static ChatRoomDto from(ChatRoom chatRoom, User me, boolean isOpponentOnline, ParticipateChatRoom opponentParticipateChatRoom)
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
                    .opponentLastEnterAt(opponentParticipateChatRoom.getLastReadAt())
                    .lastChatAt(chatRoom.getLastChatAt())
                    .build();
        }
    }
}
