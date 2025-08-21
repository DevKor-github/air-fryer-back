package com.airfryer.repicka.domain.user.entity.user_report;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ReportLocation
{
    POST("POST", "게시글"),
    CHATROOM("CHATROOM", "채팅방");

    private final String code;
    private final String label;
}
