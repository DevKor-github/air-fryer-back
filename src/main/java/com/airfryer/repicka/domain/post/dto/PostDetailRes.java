package com.airfryer.repicka.domain.post.dto;

import com.airfryer.repicka.domain.item.dto.BaseItemDto;
import com.airfryer.repicka.domain.item.entity.PostType;
import com.airfryer.repicka.domain.user.dto.BaseUserDto;
import com.airfryer.repicka.domain.user.entity.User;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Getter
@Builder
public class PostDetailRes {
    private Long id; // 게시글 식별자

    private BaseUserDto writer; // 게시글 올린 사용자 정보

    // question: item에 title과 description이 존재하는 게 중복 저장은 안돼서 db 딴에선 좋긴 한데 너무 명시적이지 않게 느껴질 수 있을 거 같아요
    private BaseItemDto itemInfo; // 상품 정보

    private PostType postType; // 게시글 타입: 대여 or 판매

    private int price; // 가격

    @Builder.Default
    private int deposit = 0; // 보증금

    @Builder.Default
    private List<String> images = new ArrayList<>(); // 이미지 리스트

    private LocalDateTime repostDate; // 끌올 날짜

    private int likeCount; // 좋아요 개수

    private int chatRoomCount; // 채팅방 개수

    private boolean isMine; // 내 게시글 여부

    private boolean isLiked; // 좋아요 여부

    // post, imageUrls, currentUser로 PostDetailRes 반환하는 정적 팩토리 메서드
    public static PostDetailRes from(Post post, List<String> imageUrls, User currentUser, boolean isLiked) {
        boolean isMine = currentUser != null && currentUser.getId().equals(post.getWriter().getId());

        return PostDetailRes.builder()
                .id(post.getId())
                .writer(BaseUserDto.from(post.getWriter()))
                .itemInfo(BaseItemDto.from(post.getItem()))
                .postType(post.getPostType())
                .price(post.getPrice())
                .deposit(post.getDeposit())
                .images(imageUrls)
                .repostDate(post.getItem().getRepostDate())
                .likeCount(post.getLikeCount())
                .chatRoomCount(post.getChatRoomCount())
                .isMine(isMine)
                .isLiked(isLiked)
                .build();
    }
}
