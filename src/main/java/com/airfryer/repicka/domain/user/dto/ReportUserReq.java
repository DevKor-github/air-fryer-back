package com.airfryer.repicka.domain.user.dto;

import com.airfryer.repicka.domain.user.entity.user_report.ReportCategory;
import com.airfryer.repicka.domain.user.entity.user_report.ReportLocation;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ReportUserReq
{
    @NotNull(message = "피신고자 ID를 입력해주세요.")
    private Long reportedUserId;

    @NotNull(message = "신고를 수행 중인 제품 ID를 입력해주세요.")
    private Long itemId;

    @NotNull(message = "신고 퍼널을 입력해주세요.")
    private ReportLocation location;

    @NotNull(message = "신고 사유를 입력해주세요.")
    private ReportCategory[] categories;

    @Size(max = 1024, message = "설명은 최대 1024자까지 입력할 수 있습니다.")
    private String description;
}
