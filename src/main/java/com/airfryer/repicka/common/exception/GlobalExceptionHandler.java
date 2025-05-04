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

    // 이외의 예외 처리 핸들러
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponseDto> unexpectedExceptionHandler(Exception e)
    {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ExceptionResponseDto.builder()
                        .code("UNEXPECTED_ERROR")
                        .message(e.getMessage())
                        .data(null)
                        .build());
    }
}
