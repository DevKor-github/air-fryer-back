package com.airfryer.repicka.domain.item.dto.res;

import com.airfryer.repicka.domain.item.dto.ItemPreviewDto;
import com.airfryer.repicka.domain.item.entity.*;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class OwnedItemListRes extends ItemPreviewDto
{
    private Boolean isSold;     // 판매 여부

    public static OwnedItemListRes from(Item item, String thumbnailUrl, boolean isSold)
    {
        return OwnedItemListRes.builder()
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
                .repostDate(item.getRepostDate())
                .isSold(isSold)
                .build();
    }
}
