package com.airfryer.repicka.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CustomException extends RuntimeException
{
    private CustomExceptionCode code;   // 에러 코드
    private Object data;                // 데이터
}
