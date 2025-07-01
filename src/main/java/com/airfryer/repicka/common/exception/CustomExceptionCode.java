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
    ACCESS_DENIED(HttpStatus.UNAUTHORIZED, "접근 인가에 실패하였습니다."),

    // 토큰 관련 예외
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "Refresh token이 존재하지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 Refresh token입니다."),

    // 사용자 관련 예외
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자 데이터를 찾을 수 없습니다."),

    // 제품(Item) 관련 예외
    DEAL_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "가격 협의가 불가능한 제품입니다."),
    ALREADY_SALE_RESERVED(HttpStatus.CONFLICT, "이미 판매가 예정된 제품입니다."),

    // 게시글(Post) 관련 예외
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글 데이터를 찾을 수 없습니다."),
    NOT_RENTAL_POST(HttpStatus.BAD_REQUEST, "대여 게시글이 아닙니다."),
    NOT_SALE_POST(HttpStatus.BAD_REQUEST, "판매 게시글이 아닙니다."),
    SAME_WRITER_AND_REQUESTER(HttpStatus.BAD_REQUEST, "게시글 작성자와 대여 및 구매 요청자는 달라야 합니다."),

    // 약속(Appointment) 관련 예외
    NOT_APPOINTMENT_PARTICIPANT(HttpStatus.FORBIDDEN, "약속 관계자가 아닙니다."),
    APPOINTMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "약속 데이터를 찾을 수 없습니다."),
    UPDATE_IN_PROGRESS_APPOINTMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "대여 중인 약속 변경 제시 데이터를 찾을 수 없습니다."),
    RETURN_DATE_NOT_FOUND(HttpStatus.NOT_FOUND, "대여 약속 제시에서 반납 일시는 필수적입니다."),
    RENTAL_DATE_IS_LATER_THAN_RETURN_DATE(HttpStatus.BAD_REQUEST, "대여 일시는 반납 일시보다 이전이어야 합니다."),
    CURRENT_DATE_IS_LATER_THAN_RETURN_DATE(HttpStatus.BAD_REQUEST, "반납 일시는 현재 이후여야 합니다."),
    INVALID_LOCATION(HttpStatus.BAD_REQUEST, "입력한 장소 형식이 올바르지 않습니다."),
    PRICE_IS_NEGATIVE(HttpStatus.BAD_REQUEST, "가격은 0 또는 양수여야 합니다."),
    CREATOR_CANNOT_AGREE(HttpStatus.BAD_REQUEST, "본인이 제시한 약속을 본인이 확정할 수는 없습니다."),
    ALREADY_RENTAL_RESERVED_PERIOD(HttpStatus.CONFLICT, "해당 구간 동안 이미 대여 약속이 예정되어 있습니다."),
    ALREADY_SALE_RESERVED_PERIOD(HttpStatus.CONFLICT, "해당 구간 동안 이미 판매 약속이 예정되어 있습니다."),
    NOT_PENDING_APPOINTMENT(HttpStatus.CONFLICT, "제시 중인 약속이 아닙니다."),
    NOT_CONFIRMED_APPOINTMENT(HttpStatus.CONFLICT, "확정된 약속이 아닙니다."),
    NOT_IN_PROGRESS_APPOINTMENT(HttpStatus.CONFLICT, "대여 중인 약속이 아닙니다."),
    APPOINTMENT_CANNOT_CANCELLED(HttpStatus.CONFLICT, "취소할 수 없는 상태입니다."),

    // 파일 관련 예외
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
    INVALID_FILE_FORMAT(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "파일 크기가 너무 큽니다."),
    FILE_NOT_FOUND(HttpStatus.BAD_REQUEST, "파일을 찾을 수 없습니다."),

    // 내부 로직 오류 (발생하면 안됨!)
    SALE_POST_NOT_FOUND(HttpStatus.NOT_FOUND, "제품은 판매 예정인데, 판매 게시글 데이터를 찾을 수 없습니다. (내부 로직 오류)"),
    SALE_APPOINTMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "제품은 판매 예정인데, 판매 약속 데이터를 찾을 수 없습니다. (내부 로직 오류)");

    private final HttpStatus httpStatus;    // HTTP 상태 코드
    private final String message;           // 메시지
}
