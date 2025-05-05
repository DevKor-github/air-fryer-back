package com.airfryer.repicka.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum CustomExceptionCode
{
    // 권한 관련 예외
    NOT_LOGIN(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    LOW_AUTHORITY(HttpStatus.UNAUTHORIZED, "권한이 부족합니다."),

    EXAMPLE_EXCEPTION(HttpStatus.NOT_FOUND, "예외 예시입니다.");

    private final HttpStatus httpStatus;    // HTTP 상태 코드
    private final String message;           // 메시지
}
