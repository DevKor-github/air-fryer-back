package com.airfryer.repicka.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum CustomExceptionCode
{
    // 로그인 관련 예외
    EXAMPLE_EXCEPTION(HttpStatus.NOT_FOUND, "예외 예시입니다.");

    private final HttpStatus httpStatus;    // HTTP 상태 코드
    private final String message;           // 메시지
}
