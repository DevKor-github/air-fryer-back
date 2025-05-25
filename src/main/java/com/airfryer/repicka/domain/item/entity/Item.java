package com.airfryer.repicka.domain.item.entity;

import com.airfryer.repicka.common.entity.BaseEntity;
import io.hypersistence.utils.hibernate.type.array.EnumArrayType;
import io.hypersistence.utils.hibernate.type.array.StringArrayType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "item"
)
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

    // 제품 타입
    @NotNull
    @Type(value = StringArrayType.class)
    @Column(
            name = "product_type",
            columnDefinition = "text[]"
    )
    private ProductType[] productType;

    // 사이즈
    @NotNull
    @Enumerated(EnumType.STRING)
    private ItemSize size;

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

    // 품질
    @Enumerated(EnumType.STRING)
    private ItemQuality quality;

    // 장소
    @Column(length = 255)
    private String location;

    // 거래 방식
    @Enumerated(EnumType.STRING)
    private TradeMethod tradeMethod;

    // 가격 제시 가능 여부
    @NotNull
    private Boolean canDeal;

    // 현재 제품 상태
    @NotNull
    @Enumerated(EnumType.STRING)
    private CurrentItemState state;

    // 끌올 날짜
    @NotNull
    private LocalDateTime repostDate;
}
