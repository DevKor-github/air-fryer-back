package com.airfryer.repicka.domain.post.entity;

import com.airfryer.repicka.common.entity.BaseEntity;
import com.airfryer.repicka.domain.item.entity.Item;
import com.airfryer.repicka.domain.post.dto.CreatePostReq;
import com.airfryer.repicka.domain.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(
        name = "post"
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Post extends BaseEntity
{
    // 게시글 식별자
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 작성자
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer")
    private User writer;

    // 제품
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item")
    private Item item;

    // 게시글 타입
    @NotNull
    @Enumerated(EnumType.STRING)
    private PostType postType;

    // 대여료 / 판매값
    @NotNull
    private int price;

    // 보증금
    @NotNull
    @Builder.Default
    private int deposit = 0;

    // 좋아요 개수
    @NotNull
    @Builder.Default
    private int likeCount = 0;

    // 채팅방 개수
    @NotNull
    @Builder.Default
    private int chatRoomCount = 0;

    // 게시글 가격 및 보증금 수정
    public void updatePriceAndDeposit(int price, int deposit) {
        this.price = price;
        this.deposit = deposit;
    }

    // 좋아요 개수 증가
    public void addLikeCount() {
        this.likeCount++;
    }

    // 좋아요 개수 감소
    public void removeLikeCount() {
        this.likeCount--;
    }

    // 채팅방 개수 증가
    public void addChatRoomCount() {
        this.chatRoomCount++;
    }

    // 채팅방 개수 감소
    public void removeChatRoomCount() {
        this.chatRoomCount--;
    }
    
}
