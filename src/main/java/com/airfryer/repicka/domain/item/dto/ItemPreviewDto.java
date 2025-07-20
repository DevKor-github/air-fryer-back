package com.airfryer.repicka.domain.item.dto;

import com.airfryer.repicka.domain.item.entity.Item;
import com.airfryer.repicka.domain.item.entity.ProductType;
import com.airfryer.repicka.domain.item.entity.TransactionType;
import com.airfryer.repicka.domain.item.entity.ItemColor;
import com.airfryer.repicka.domain.item.entity.ItemSize;
import com.airfryer.repicka.domain.item.entity.ItemQuality;
import com.airfryer.repicka.domain.item.entity.TradeMethod;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemPreviewDto
{
    private Long itemId;    // 제품 ID

    @Builder.Default
    private ProductType[] productTypes = new ProductType[2]; // 제품 타입

    @Builder.Default
    private TransactionType[] transactionTypes = new TransactionType[2]; // 거래 타입

    private String thumbnail;       // 대표 사진
    private String title;           // 게시글 제목
    private int rentalFee;          // 대여료
    private int salePrice;          // 판매값
    private int deposit;            // 보증금
    private ItemSize size;          // 사이즈
    private ItemColor color;         // 색상
    private ItemQuality quality;     // 품질
    private TradeMethod[] tradeMethods; // 거래 방법
    private int likeCount;          // 좋아요 개수
    private int chatRoomCount;      // 채팅방 개수
    private boolean isAvailable;    // 대여 및 구매 가능 여부

    public static ItemPreviewDto from(Item item, String thumbnailUrl, boolean isAvailable)
    {
        return ItemPreviewDto.builder()
                .itemId(item.getId())
                .productTypes(item.getProductTypes())
                .transactionTypes(item.getTransactionTypes())
                .thumbnail(thumbnailUrl)
                .title(item.getTitle())
                .rentalFee(item.getRentalFee())
                .salePrice(item.getSalePrice())
                .deposit(item.getDeposit())
                .size(item.getSize())
                .color(item.getColor())
                .quality(item.getQuality())
                .tradeMethods(item.getTradeMethods())
                .likeCount(item.getLikeCount())
                .chatRoomCount(item.getChatRoomCount())
                .isAvailable(isAvailable)
                .build();
    }
}
