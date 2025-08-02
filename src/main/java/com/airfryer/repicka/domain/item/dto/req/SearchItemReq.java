package com.airfryer.repicka.domain.item.dto.req;

import com.airfryer.repicka.domain.item.dto.ItemOrder;
import com.airfryer.repicka.domain.item.entity.*;
import com.airfryer.repicka.domain.item.entity.TransactionType;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchItemReq
{
    // 페이지 크기
    @Positive(message = "페이지 크기는 0보다 커야 합니다.")
    @Max(value = 50, message = "페이지 크기는 50 이하여야 합니다.")
    @NotNull(message = "페이지 크기는 필수 입력 값입니다.")
    private int pageSize;
    
    // 커서 기반 페이지네이션을 위한 필드 조건
    @AssertTrue(message = "커서 기반 페이지네이션을 위한 필드 조건을 충족하지 못했습니다.")
    private boolean isValidCursor() {
        if (cursorId == null) return true;
        return cursorLike != null && cursorDate != null;
    }
    
    private Long cursorId;              // 마지막 아이템의 ID
    private Integer cursorLike;    // 마지막 아이템의 좋아요 개수 (LIKE 정렬용)
    private LocalDateTime cursorDate;   // 마지막 아이템의 repost_date (RECENT 정렬용)

    // 검색 키워드
    private String keyword;

    // 제품 타입
    private ProductType[] productTypes;

    // 거래 타입
    private TransactionType[] transactionTypes;

    // 제품 사이즈
    private ItemSize[] sizes;

    // 제품 색상
    private ItemColor[] colors;

    // 거래 방식
    private TradeMethod[] tradeMethods;

    // 거래 시작 날짜
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    // 거래 종료 날짜
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;

    // 시작 금액
    private int startPrice = 0;

    // 종료 금액
    private int endPrice = 999999;

    // 제품 정렬 순서
    private ItemOrder itemOrder = ItemOrder.RECENT;
}
