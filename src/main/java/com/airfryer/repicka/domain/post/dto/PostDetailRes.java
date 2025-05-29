package com.airfryer.repicka.domain.post.dto;

import com.airfryer.repicka.domain.item.dto.BaseItemDto;
import com.airfryer.repicka.domain.item.entity.*;
import com.airfryer.repicka.domain.post.entity.Post;
import com.airfryer.repicka.domain.post.entity.PostType;
import com.airfryer.repicka.domain.user.dto.BaseUserDto;
import com.airfryer.repicka.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

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
    private int deposit = 0; // 보증급

    @Builder.Default
    private String[] images = new String[10];

    // user, item, post, imageUrl로 PostDetailRes 반환하는 정적 팩토리 메서드
    public static PostDetailRes from(User user, Item item, Post post, String[] images) {
        return PostDetailRes.builder()
                .id(post.getId())
                .writer(BaseUserDto.from(user))
                .itemInfo(BaseItemDto.from(item))
                .postType(post.getPostType())
                .price(post.getPrice())
                .deposit(post.getDeposit())
                .images(images)
                .build();
    }
}
