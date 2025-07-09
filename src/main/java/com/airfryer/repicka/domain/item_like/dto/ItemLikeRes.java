package com.airfryer.repicka.domain.item_like.dto;

import com.airfryer.repicka.domain.item.entity.Item;
import com.airfryer.repicka.domain.item.entity.TransactionType;
import com.airfryer.repicka.domain.item.entity.ProductType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemLikeRes
{
    // 제품 ID
    private Long itemId;

    // 제품 타입
    @Builder.Default
    private ProductType[] productTypes = new ProductType[2];

    // 거래 타입
    @Builder.Default
    private TransactionType[] transactionTypes = new TransactionType[2];

    private String title;       // 제목
    private String thumbnail;   // 대표 사진
    private int rentalFee;      // 대여료
    private int salePrice;      // 판매값
    private int deposit;        // 보증금

    public static ItemLikeRes from(Item item, String thumbnailUrl)
    {
        return ItemLikeRes.builder()
                .itemId(item.getId())
                .productTypes(item.getProductTypes())
                .transactionTypes(item.getTransactionTypes())
                .title(item.getTitle())
                .thumbnail(thumbnailUrl)
                .rentalFee(item.getRentalFee())
                .salePrice(item.getSalePrice())
                .deposit(item.getDeposit())
                .build();
    }
}
