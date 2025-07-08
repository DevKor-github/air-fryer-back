package com.airfryer.repicka.domain.post_like.dto;

import com.airfryer.repicka.domain.post.entity.Post;
import com.airfryer.repicka.domain.post.entity.PostType;
import com.airfryer.repicka.domain.item.entity.ProductType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostLikeRes {
    private Long id;

    private PostType postType; // 게시글 타입

    private String title; // 게시글 제목

    @Builder.Default
    private ProductType[] productTypes = new ProductType[2]; // 제품 타입

    private String thumbnail; // 대표 사진

    private int price; // 대여료 / 판매값
    
    private int deposit; // 보증금

    public static PostLikeRes from(Post post, String thumbnailUrl) {
        return PostLikeRes.builder()
            .id(post.getId())
            .postType(post.getPostType())
            .title(post.getItem().getTitle())
            .productTypes(post.getItem().getProductTypes())
            .thumbnail(thumbnailUrl)
            .price(post.getPrice())
            .deposit(post.getDeposit())
            .build();
    }
}
