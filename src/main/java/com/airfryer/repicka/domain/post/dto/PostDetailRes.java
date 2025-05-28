package com.airfryer.repicka.domain.post.dto;

import com.airfryer.repicka.domain.item.entity.*;
import com.airfryer.repicka.domain.post.entity.Post;
import com.airfryer.repicka.domain.post.entity.PostType;
import com.airfryer.repicka.domain.user.dto.BaseUserDto;
import com.airfryer.repicka.domain.user.entity.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostDetailRes {
    private Long id; // 게시글 식별자
    private BaseUserDto writer; // 게시글 올린 사용자 정보
    // question: 아래부터 state까지 상품 정보인데 BaseItemDto나 다른 이름으로 따로 빼는 게 나을까요?
    @Builder.Default
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
    @Builder.Default
    private int deposit = 0;
    @Builder.Default
    private String[] images = new String[10];

    // user, item, post, imageUrl로 PostDetailRes 반환하는 정적 팩토리 메서드
    public static PostDetailRes from(User user, Item item, Post post, String[] images) {
        return PostDetailRes.builder()
                .id(post.getId())
                .writer(BaseUserDto.from(user))
                .productTypes(item.getProductTypes())
                .size(item.getSize())
                .title(item.getTitle())
                .description(item.getDescription())
                .color(item.getColor())
                .quality(item.getQuality())
                .location(item.getLocation())
                .tradeMethod(item.getTradeMethod())
                .canDeal(item.getCanDeal())
                .state(item.getState())
                .postType(post.getPostType())
                .price(post.getPrice())
                .deposit(post.getDeposit())
                .images(images)
                .build();
    }
}
