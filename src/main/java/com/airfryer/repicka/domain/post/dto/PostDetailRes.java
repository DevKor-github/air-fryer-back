package com.airfryer.repicka.domain.post.dto;

import com.airfryer.repicka.domain.item.entity.*;
import com.airfryer.repicka.domain.post.entity.PostType;
import com.airfryer.repicka.domain.user.dto.BaseUserDto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreatePostRes {
    private BaseUserDto writer; // 게시글 올린 사용자 정보
    // question: 아래부터 state까지 상품 정보인데 BaseItemDto나 다른 이름으로 따로 빼는 게 나을까요?
    private ProductType[] productTypes = new ProductType[2];
    private ItemSize size;
    private String title;
    private String description;
    private ItemColor color;
    private ItemQuality quality;
    private String location;
    private TradeMethod tradeMethod;
    private Boolean canDeal;
    private CurrentItemState state;
    private PostType postType;
    private int price;
    private int deposit = 0;
    private String[] images = new String[10];

}
