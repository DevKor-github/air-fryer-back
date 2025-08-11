package com.airfryer.repicka.domain.user.dto;

import com.airfryer.repicka.domain.user.entity.Gender;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UpdateUserReq {
    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하이어야 합니다.")
    private String nickname;    // 닉네임

    private String profileImageUrl;    // 프로필 이미지

    private Gender gender;    // 성별

    @Min(value = 100, message = "키는 100cm 이상이어야 합니다.")
    @Max(value = 250, message = "키는 250cm 이하이어야 합니다.")
    private Integer height;    // 키

    @Min(value = 30, message = "몸무게는 30kg 이상이어야 합니다.")
    @Max(value = 200, message = "몸무게는 200kg 이하이어야 합니다.")
    private Integer weight;    // 몸무게
}
