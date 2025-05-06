package com.airfryer.repicka.common.exception;

import com.airfryer.repicka.common.response.ExceptionResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler
{
    // 커스텀 예외 처리 핸들러
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ExceptionResponseDto> customExceptionHandler(CustomException e)
    {
        return ResponseEntity.status(e.getCode().getHttpStatus())
                .body(ExceptionResponseDto.builder()
                        .code(e.getCode().name())
                        .message(e.getCode().getMessage())
                        .data(e.getData())
                        .build());
    }
}
