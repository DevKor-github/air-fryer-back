package com.airfryer.repicka.domain.chat.entity;

import com.airfryer.repicka.common.entity.BaseEntity;
import com.airfryer.repicka.domain.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "participate_chat_room")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ParticipateChatRoom extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 채팅방
    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "chat_room")
    private ChatRoom chatRoom;

    // 사용자
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant")
    private User participant;

    // 마지막 채팅 읽기 시점
    @NotNull
    @Builder.Default
    private LocalDateTime lastReadAt = LocalDateTime.now();

    // 읽지 않은 채팅 개수
    @NotNull
    @Builder.Default
    private int unreadChatCount = 0;

    // 채팅방 나감 여부
    @NotNull
    @Builder.Default
    private Boolean hasLeftRoom = false;

    // 마지막 재입장 시점
    @NotNull
    @Builder.Default
    private LocalDateTime lastReEnterAt = LocalDateTime.now();

    /// 채팅방 참여 정보 갱신

    public void renew()
    {
        this.lastReadAt = LocalDateTime.now();
        this.unreadChatCount = 0;
    }

    /// 읽지 않은 채팅 개수 증가

    public void increaseUnreadChatCount() {
        this.unreadChatCount++;
    }

    /// 채팅방 나가기

    public void exit()
    {
        this.lastReadAt = LocalDateTime.now();
        this.hasLeftRoom = true;
    }

    /// 채팅방 재입장

    public void reEnter()
    {
        this.lastReadAt = LocalDateTime.now();
        this.hasLeftRoom = false;
        this.lastReEnterAt = LocalDateTime.now();
    }
}
