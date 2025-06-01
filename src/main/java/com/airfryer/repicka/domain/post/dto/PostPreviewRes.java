package com.airfryer.repicka.domain.post.dto;

import com.airfryer.repicka.domain.item.entity.ProductType;
import com.airfryer.repicka.domain.item_image.entity.ItemImage;
import com.airfryer.repicka.domain.post.entity.Post;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostPreviewRes {
    private String title; // 게시글 제목

    @Builder.Default
    private ProductType[] productTypes = new ProductType[2];

    private String image; // 대표 사진

    private int price; // 대여료 / 판매값

    private int likeCount; // 좋아요 개수

    private int chatCount; // 채팅방 개수

    public static PostPreviewRes from(Post post, ItemImage itemImage) {
        return PostPreviewRes.builder()
                .title(post.getItem().getTitle())
                .productTypes(post.getItem().getProductTypes())
                .image(itemImage.getImageUrl())
                .price(post.getPrice())
                .likeCount(post.getLikeCount())
                .chatCount(post.getChatCount())
                .build();
    }
}
