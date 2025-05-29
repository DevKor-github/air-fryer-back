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

    // 게시글(Post) 관련 예외
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글 데이터를 찾을 수 없습니다."),

    // 약속(Appointment) 관련 예외
    RENTAL_DATE_IS_LATER_THAN_RETURN_DATE(HttpStatus.BAD_REQUEST, "대여 일시는 반납 일시보다 이전이어야 합니다."),
    INVALID_LOCATION(HttpStatus.BAD_REQUEST, "입력한 장소 형식이 올바르지 않습니다."),
    PRICE_IS_NEGATIVE(HttpStatus.BAD_REQUEST, "가격은 0 또는 양수여야 합니다."),
    SAME_OWNER_AND_BORROWER(HttpStatus.BAD_REQUEST, "제품 소유자와 대여자는 달라야 합니다."),

    EXAMPLE_EXCEPTION(HttpStatus.NOT_FOUND, "예외 예시입니다.");

    private final HttpStatus httpStatus;    // HTTP 상태 코드
    private final String message;           // 메시지
}
