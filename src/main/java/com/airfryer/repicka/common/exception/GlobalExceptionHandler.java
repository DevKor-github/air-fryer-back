package com.airfryer.repicka.common.exception;

import com.airfryer.repicka.common.response.ExceptionResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler
{
    // 커스텀 예외 처리 핸들러
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ExceptionResponseDto> customExceptionHandler(CustomException e)
    {
        log.error("CustomException occurred: ", e);

        return ResponseEntity.status(e.getCode().getHttpStatus())
                .body(ExceptionResponseDto.builder()
                        .code(e.getCode().name())
                        .message(e.getCode().getMessage())
                        .data(e.getData())
                        .build());
    }

    // 권한이 없는 요청 예외 처리
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ExceptionResponseDto> handleAuthenticationException(AuthenticationException e) {
        log.error("접근 권한 누락: ", e);

        return ResponseEntity.status(CustomExceptionCode.NOT_LOGIN.getHttpStatus())
                .body(ExceptionResponseDto.builder()
                        .code(CustomExceptionCode.NOT_LOGIN.name())
                        .message(CustomExceptionCode.NOT_LOGIN.getMessage())
                        .data(null)
                        .build());
    }

    // 권한이 부족한 요청 예외 처리
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ExceptionResponseDto> handleAccessDeniedException(AccessDeniedException e) {
        log.error("접근 권한 부족: ", e);

        return ResponseEntity.status(CustomExceptionCode.LOW_AUTHORITY.getHttpStatus())
                .body(ExceptionResponseDto.builder()
                        .code(CustomExceptionCode.LOW_AUTHORITY.name())
                        .message(CustomExceptionCode.LOW_AUTHORITY.getMessage())
                        .data(null)
                        .build());
    }

    // 접근 인가 실패
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ExceptionResponseDto> handleAuthorizationDenied(AuthorizationDeniedException e) {
        log.error("접근 인가 실패: ", e);

        return ResponseEntity.status(CustomExceptionCode.ACCESS_DENIED.getHttpStatus())
                .body(ExceptionResponseDto.builder()
                        .code(CustomExceptionCode.ACCESS_DENIED.name())
                        .message(CustomExceptionCode.ACCESS_DENIED.getMessage())
                        .data(null)
                        .build());
    }

    // 404 에러 처리 - 없는 API 엔드포인트
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ExceptionResponseDto> handleNotFoundException(Exception e)
    {
        log.warn("404 Not Found: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ExceptionResponseDto.builder()
                        .code("NOT_FOUND")
                        .message("요청한 API를 찾을 수 없습니다.")
                        .data(null)
                        .build());
    }

    // 405 에러 처리 - 지원하지 않는 HTTP 메서드
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ExceptionResponseDto> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e)
    {
        log.warn("405 Method Not Allowed: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ExceptionResponseDto.builder()
                        .code("METHOD_NOT_ALLOWED")
                        .message("지원하지 않는 HTTP 메서드입니다: " + e.getMethod())
                        .data(null)
                        .build());
    }

    // @RequestBody @Valid 유효성 검사 예외 처리 핸들러
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponseDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException e)
    {
        log.error("MethodArgumentNotValidException occurred: ", e);

        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("요청 값이 올바르지 않습니다.");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ExceptionResponseDto.builder()
                        .code("INVALID_INPUT")
                        .message(message)
                        .data(null)
                        .build());
    }

    // @RequestBody @Valid 유효성 검사 이외의 요청 dto 역직렬화 예외 처리 핸들러
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ExceptionResponseDto> handleHttpMessageNotReadableException(HttpMessageNotReadableException e)
    {
        log.error("HttpMessageNotReadableException occurred: ", e);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ExceptionResponseDto.builder()
                        .code("INVALID_INPUT")
                        .message(e.getMessage())
                        .data(null)
                        .build());
    }

    // @PathVariable 유효성 검사 예외 처리 핸들러
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ExceptionResponseDto> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e)
    {
        log.error("MethodArgumentTypeMismatchException occurred: ", e);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ExceptionResponseDto.builder()
                        .code("INVALID_INPUT")
                        .message(e.getMessage())
                        .data(null)
                        .build());
    }

    // @RequestParm 누락 예외 처리 핸들러
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ExceptionResponseDto> handleMissingServletRequestParameterException(MissingServletRequestParameterException e)
    {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ExceptionResponseDto.builder()
                        .code("RequestParam이 누락되었습니다.")
                        .message(e.getMessage())
                        .data(null)
                        .build());
    }

    // @CookieValue 쿠키 누락 예외 처리 핸들러
    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<ExceptionResponseDto> handleMissingRequestCookieException(MissingRequestCookieException e)
    {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ExceptionResponseDto.builder()
                        .code("쿠키가 누락되었습니다.")
                        .message(e.getMessage())
                        .data(null)
                        .build());
    }

    // NullPointerException 처리 (자주 발생하는 500 에러)
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ExceptionResponseDto> handleNullPointerException(NullPointerException e)
    {
        log.error("NullPointerException occurred: ", e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ExceptionResponseDto.builder()
                        .code("NULL_POINTER_ERROR")
                        .message("필수 데이터가 누락되었습니다.")
                        .data(null)
                        .build());
    }

    // IllegalArgumentException 처리 핸들러
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ExceptionResponseDto> handleIllegalArgumentException(IllegalArgumentException e)
    {
        log.error("IllegalArgumentException occurred: ", e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ExceptionResponseDto.builder()
                        .code("ILLEGAL_ARGUMENT_ERROR")
                        .message("잘못된 매개변수가 전달되었습니다.")
                        .data(null)
                        .build());
    }

    // 데이터베이스 관련 예외 처리 핸들러
    @ExceptionHandler({
        org.springframework.dao.DataAccessException.class,
        jakarta.persistence.PersistenceException.class
    })
    public ResponseEntity<ExceptionResponseDto> handleDatabaseException(Exception e)
    {
        log.error("Database Exception occurred: ", e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ExceptionResponseDto.builder()
                        .code("INTERNAL_SERVER_ERROR")
                        .message("예상치 못한 서버 오류가 발생했습니다.")
                        .data(null)
                        .build());
    }

    // 런타임 예외 처리 핸들러 (NullPointerException, IllegalArgumentException 등)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ExceptionResponseDto> handleRuntimeException(RuntimeException e)
    {
        log.error("Runtime Exception occurred: ", e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ExceptionResponseDto.builder()
                        .code("INTERNAL_SERVER_ERROR")
                        .message("예상치 못한 서버 오류가 발생했습니다.")
                        .data(null)
                        .build());
    }

    // 모든 예상치 못한 예외 처리 핸들러 (최종 안전망)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponseDto> handleException(Exception e)
    {
        log.error("Unexpected Exception occurred: ", e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ExceptionResponseDto.builder()
                        .code("INTERNAL_SERVER_ERROR")
                        .message("예상치 못한 서버 오류가 발생했습니다.")
                        .data(null)
                        .build());
    }
}
