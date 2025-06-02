package com.airfryer.repicka.domain.post.dto;

import com.airfryer.repicka.domain.item.entity.ProductType;
import com.airfryer.repicka.domain.item_image.entity.ItemImage;
import com.airfryer.repicka.domain.post.entity.Post;
import com.airfryer.repicka.domain.post.entity.PostType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostPreviewRes {
    private Long id;

    private PostType postType; // 게시글 타입

    private String title; // 게시글 제목

    @Builder.Default
    private ProductType[] productTypes = new ProductType[2]; // 제품 타입

    private String thumbnail; // 대표 사진

    private int price; // 대여료 / 판매값

    private int likeCount; // 좋아요 개수

    private int chatRoomCount; // 채팅방 개수

    private boolean isAvailable;

    public static PostPreviewRes from(Post post, ItemImage itemImage, boolean isAvailable) {
        return PostPreviewRes.builder()
                .id(post.getId())
                .postType(post.getPostType())
                .title(post.getItem().getTitle())
                .productTypes(post.getItem().getProductTypes())
                .thumbnail(itemImage.getImageUrl())
                .price(post.getPrice())
                .likeCount(post.getLikeCount())
                .chatRoomCount(post.getChatRoomCount())
                .isAvailable(isAvailable)
                .build();
    }
}
