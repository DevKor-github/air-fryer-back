package com.airfryer.repicka.common.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SuccessResponseDto
{
    private String message;     // 메시지
    private Object data;        // 데이터
}
