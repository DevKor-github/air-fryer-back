package com.airfryer.repicka.domain.item.entity;

import com.airfryer.repicka.common.entity.BaseEntity;
import com.airfryer.repicka.domain.item.dto.CreateItemReq;

import com.airfryer.repicka.domain.user.entity.User;
import io.hypersistence.utils.hibernate.type.array.EnumArrayType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;

@Entity
@Table(name = "item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Item extends BaseEntity
{
    // 제품 식별자
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 소유자
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner")
    private User owner;

    // 제품 타입
    @NotEmpty
    @Type(
            value = EnumArrayType.class,
            parameters = @org.hibernate.annotations.Parameter(
                    name = EnumArrayType.SQL_ARRAY_TYPE,
                    value = "text"
            )
    )
    @Column(
            name = "product_type",
            columnDefinition = "text[]"
    )
    @Builder.Default
    private ProductType[] productTypes = new ProductType[2];

    // 게시글 타입
    @NotEmpty
    @Type(
            value = EnumArrayType.class,
            parameters = @org.hibernate.annotations.Parameter(
                    name = EnumArrayType.SQL_ARRAY_TYPE,
                    value = "text"
            )
    )
    @Column(
            name = "post_type",
            columnDefinition = "text[]"
    )
    @Builder.Default
    private PostType[] postTypes = new PostType[2];

    // 제목
    @NotNull
    @Column(length = 255)
    private String title;

    // 설명
    @Column(columnDefinition = "TEXT")
    private String description;

    // 색상
    @Enumerated(EnumType.STRING)
    private ItemColor color;

    // 사이즈
    @NotNull
    @Enumerated(EnumType.STRING)
    private ItemSize size;

    // 품질
    @Enumerated(EnumType.STRING)
    private ItemQuality quality;

    // 대여료
    @NotNull
    @Builder.Default
    private int rentalFee = 0;

    // 판매값
    @NotNull
    @Builder.Default
    private int salePrice = 0;

    // 보증금
    @NotNull
    @Builder.Default
    private int deposit = 0;

    // 장소
    @Column(length = 255)
    private String location;

    // 거래 방식
    @NotEmpty
    @Type(
            value = EnumArrayType.class,
            parameters = @org.hibernate.annotations.Parameter(
                    name = EnumArrayType.SQL_ARRAY_TYPE,
                    value = "text"
            )
    )
    @Column(
            name = "trade_method",
            columnDefinition = "text[]"
    )
    @Builder.Default
    private TradeMethod[] tradeMethods = new TradeMethod[2];

    // 가격 제시 가능 여부
    @NotNull
    private Boolean canDeal;

    // 좋아요 개수
    @NotNull
    @Builder.Default
    private int likeCount = 0;

    // 채팅방 개수
    @NotNull
    @Builder.Default
    private int chatRoomCount = 0;

    // 판매 날짜
    private LocalDateTime saleDate;

    // 끌올 날짜
    @NotNull
    private LocalDateTime repostDate;

    /// 제품 판매 확정

    public void confirmSale(LocalDateTime saleDate) {
        this.saleDate = saleDate;
    }

    /// 제품 판매 취소

    public void cancelSale() {
        this.saleDate = null;
    }

    /// 제품 수정

    public void updateItem(CreateItemReq itemDetail) {
        this.productTypes = itemDetail.getProductTypes();
        this.size = itemDetail.getSize();
        this.title = itemDetail.getTitle();
        this.description = itemDetail.getDescription();
        this.color = itemDetail.getColor();
        this.quality = itemDetail.getQuality();
        this.location = itemDetail.getLocation();
        this.tradeMethod = itemDetail.getTradeMethod();
        this.canDeal = itemDetail.getCanDeal();
    }

    /// 제품 끌올

    public void repostItem() {
        this.repostDate = LocalDateTime.now();
    }

    /// 게시글 가격 및 보증금 수정

    public void updatePriceAndDeposit(int price, int deposit) {
        this.price = price;
        this.deposit = deposit;
    }

    /// 좋아요 개수 증가/감소

    public void addLikeCount() { this.likeCount++; }
    public void removeLikeCount() { this.likeCount--; }

    /// 채팅방 개수 증가/감소
    public void addChatRoomCount() { this.chatRoomCount++; }
    public void removeChatRoomCount() { this.chatRoomCount--; }
}
