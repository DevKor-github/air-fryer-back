package com.airfryer.repicka.common.redis.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TaskType {
    EXPIRE("EXPIRE", "예약 만료 처리"),
    RENTAL_REMIND("RENTAL_REMIND", "대여(구매) 리마인드 예약 알림 발송"),
    RETURN_REMIND("RETURN_REMIND", "반납 리마인드 예약 알림 발송");
    
    private final String code;
    private final String description;
}