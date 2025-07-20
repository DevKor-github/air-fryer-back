package com.airfryer.repicka.domain.item.dto;

import com.airfryer.repicka.domain.item.entity.*;
import com.airfryer.repicka.domain.item.entity.TransactionType;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchItemReq
{
    // 페이지 번호 (0부터 시작)
    private int page = 0;

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
