package com.airfryer.repicka.domain.item.dto;

import com.airfryer.repicka.domain.item.entity.*;
import com.airfryer.repicka.domain.item_image.entity.ItemImage;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class BaseItemDto
{
    @Builder.Default private ProductType[] productTypes = new ProductType[2];   // 제품 타입
    @Builder.Default private PostType[] postTypes = new PostType[2];            // 게시글 타입
    private String title;                                                       // 게시글 제목
    private String description;                                                 // 게시글 내용
    private ItemColor color;                                                    // 색상
    private ItemSize size;                                                      // 사이즈
    private ItemQuality quality;                                                // 품질
    private int rentalFee;                                                      // 대여료
    private int salePrice;                                                      // 판매값
    @Builder.Default private int deposit = 0;                                   // 보증금
    private String location;                                                    // 거래 장소
    @Builder.Default private TradeMethod[] tradeMethods = new TradeMethod[2];   // 거래 방법
    private Boolean canDeal;                                                    // 가격 제시 가능 여부
    private int likeCount;                                                      // 좋아요 개수
    private int chatRoomCount;                                                  // 채팅방 개수
    private boolean isMine;                                                     // 내 게시글 여부
    private boolean isLiked;                                                    // 좋아요 여부
    private LocalDateTime saleDate;                                             // 제품 판매 날짜
    private LocalDateTime repostDate;                                           // 끌올 날짜
    @Builder.Default private List<String> images = new ArrayList<>();           // 이미지 리스트

    public static BaseItemDto of(Item item, List<String> imageUrls, boolean isMine, boolean isLiked)
    {
        return BaseItemDto.builder()
                .productTypes(item.getProductTypes())
                .postTypes(item.getPostTypes())
                .title(item.getTitle())
                .description(item.getDescription())
                .color(item.getColor())
                .size(item.getSize())
                .quality(item.getQuality())
                .rentalFee(item.getRentalFee())
                .salePrice(item.getSalePrice())
                .deposit(item.getDeposit())
                .location(item.getLocation())
                .tradeMethods(item.getTradeMethods())
                .canDeal(item.getCanDeal())
                .likeCount(item.getLikeCount())
                .chatRoomCount(item.getChatRoomCount())
                .isMine(isMine)
                .isLiked(isLiked)
                .saleDate(item.getSaleDate())
                .repostDate(item.getRepostDate())
                .images(imageUrls)
                .build();
    }
}
