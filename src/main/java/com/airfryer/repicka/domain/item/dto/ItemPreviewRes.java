package com.airfryer.repicka.domain.item.dto;

import com.airfryer.repicka.domain.item.entity.Item;
import com.airfryer.repicka.domain.item.entity.ProductType;
import com.airfryer.repicka.domain.item.entity.PostType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemPreviewRes
{
    private Long itemId;    // 제품 ID

    @Builder.Default
    private ProductType[] productTypes = new ProductType[2]; // 제품 타입

    @Builder.Default
    private PostType[] postTypes = new PostType[2]; // 게시글 타입

    private String thumbnail;       // 대표 사진
    private String title;           // 게시글 제목
    private int rentalFee;          // 대여료
    private int salePrice;          // 판매값
    private int deposit;            // 보증금
    private int likeCount;          // 좋아요 개수
    private int chatRoomCount;      // 채팅방 개수
    private boolean isAvailable;    // 대여 및 구매 가능 여부

    public static ItemPreviewRes from(Item item, String thumbnailUrl, boolean isAvailable)
    {
        return ItemPreviewRes.builder()
                .itemId(item.getId())
                .productTypes(item.getProductTypes())
                .postTypes(item.getPostTypes())
                .thumbnail(thumbnailUrl)
                .title(item.getTitle())
                .rentalFee(item.getRentalFee())
                .salePrice(item.getSalePrice())
                .deposit(item.getDeposit())
                .likeCount(item.getLikeCount())
                .chatRoomCount(item.getChatRoomCount())
                .isAvailable(isAvailable)
                .build();
    }
}
