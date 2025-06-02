package com.airfryer.repicka.domain.post.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PostOrder {
    RECENT("RECENT", "최신순"),
    LIKE("LIKE", "좋아요순"),
    PRICE("CHEAP", "가격순");

    private final String code;
    private final String label;
}
