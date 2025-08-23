package com.airfryer.repicka.domain.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ReviewReq {
    @NotNull(message = "약속 ID를 입력해주세요.")
    private Long appointmentId;

    @NotNull(message = "평점을 입력해주세요.")
    @Min(value = 1, message = "평점은 1점 이상이어야 합니다.")
    @Max(value = 5, message = "평점은 5점 이하이어야 합니다.")
    private int rating;

    private String content;
}
