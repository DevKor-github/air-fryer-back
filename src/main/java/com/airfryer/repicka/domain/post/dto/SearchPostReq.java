package com.airfryer.repicka.domain.post.dto;

import com.airfryer.repicka.domain.item.entity.*;
import com.airfryer.repicka.domain.post.entity.PostType;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchPostReq {
    private int page = 0; // 페이지 번호 (0부터 시작)

    private String keyword; // 검색 키워드

    private ProductType productType; // 제품 타입

    private ItemSize size; // 제품 사이즈

    private ItemColor color; // 제품 색상

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime date; // 원하는 거래 날짜

    private PostOrder postOrder = PostOrder.RECENT; // 게시글 순서

    private PostType postType; // 게시글 타입
}
