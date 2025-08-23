package com.airfryer.repicka.domain.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class BlockUserReq
{
    @NotNull(message = "차단할 사용자 ID를 입력해주세요.")
    private Long blockedUserId;
}
