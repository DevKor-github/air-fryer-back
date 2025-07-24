package com.airfryer.repicka.domain.item.entity;

import com.airfryer.repicka.common.entity.BaseEntity;
import com.airfryer.repicka.domain.item.dto.CreateItemReq;

import com.airfryer.repicka.domain.user.entity.User;
import io.hypersistence.utils.hibernate.type.array.EnumArrayType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Range;
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
            name = "product_types",
            columnDefinition = "text[]"
    )
    @Builder.Default
    private ProductType[] productTypes = new ProductType[2];

    // 거래 타입
    @NotEmpty
    @Type(
            value = EnumArrayType.class,
            parameters = @org.hibernate.annotations.Parameter(
                    name = EnumArrayType.SQL_ARRAY_TYPE,
                    value = "text"
            )
    )
    @Column(
            name = "transaction_types",
            columnDefinition = "text[]"
    )
    @Builder.Default
    private TransactionType[] transactionTypes = new TransactionType[2];

    // 제목
    @NotNull
    @Column(length = 64)
    private String title;

    // 설명
    @Column(length = 1024)
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
    @Range(min = 0, max = 999999)
    @Builder.Default
    private int rentalFee = 0;

    // 판매값
    @NotNull
    @Range(min = 0, max = 999999)
    @Builder.Default
    private int salePrice = 0;

    // 보증금
    @NotNull
    @Range(min = 0, max = 999999)
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
            name = "trade_methods",
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

    // 삭제 여부
    @NotNull
    @Builder.Default
    private Boolean isDeleted = false;

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

    public void updateItem(CreateItemReq itemDetail)
    {
        this.productTypes = itemDetail.getProductTypes();
        this.transactionTypes = itemDetail.getTransactionTypes();
        this.title = itemDetail.getTitle();
        this.description = itemDetail.getDescription();
        this.color = itemDetail.getColor();
        this.size = itemDetail.getSize();
        this.quality = itemDetail.getQuality();
        this.rentalFee = itemDetail.getRentalFee();
        this.salePrice = itemDetail.getSalePrice();
        this.deposit = itemDetail.getDeposit();
        this.location = itemDetail.getLocation();
        this.tradeMethods = itemDetail.getTradeMethods();
        this.canDeal = itemDetail.getCanDeal();
    }

    /// 제품 삭제

    public void delete() {
        this.isDeleted = true;
    }

    /// 제품 끌올

    public void repostItem() {
        this.repostDate = LocalDateTime.now();
    }

    /// 좋아요 개수 증가/감소

    public void addLikeCount() { this.likeCount++; }
    public void removeLikeCount() { this.likeCount--; }

    /// 채팅방 개수 증가/감소

    public void addChatRoomCount() { this.chatRoomCount++; }
    public void removeChatRoomCount() { this.chatRoomCount--; }
}
