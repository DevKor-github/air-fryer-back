package com.airfryer.repicka.domain.user.dto;

import com.airfryer.repicka.domain.user.entity.User;
import com.airfryer.repicka.domain.user.entity.Gender;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BaseUserDto
{
    private Long id;                        // 사용자 식별자
    private String nickname;                // 사용자 별명
    private String profileImageUrl;         // 프로필 사진
    private Boolean isKoreanUnivVerified;   // 고려대 학생 인증 여부

    private Gender gender;                // 성별
    private Integer height;                // 키
    private Integer weight;                // 몸무게

    public static BaseUserDto from(User user)
    {
        return BaseUserDto.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .isKoreanUnivVerified(user.getIsKoreaUnivVerified())
                .gender(user.getGender())
                .height(user.getHeight())
                .weight(user.getWeight())
                .build();
    }
}
