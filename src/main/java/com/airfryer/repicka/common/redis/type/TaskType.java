package com.airfryer.repicka.common.redis.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TaskType {
    EXPIRE("EXPIRE", "예약 만료 처리"),
    REMIND("REMIND", "예약 알림 발송");
    
    private final String code;
    private final String description;
}