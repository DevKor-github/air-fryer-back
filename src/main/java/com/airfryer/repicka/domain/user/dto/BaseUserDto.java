package com.airfryer.repicka.domain.user.dto;

import com.airfryer.repicka.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BaseUserDto {
    private Long id; // 사용자 식별자
    private String nickname; // 사용자 별명
    private String profileImageUrl; // 프로필 사진
    private Boolean isKoreanUnivVerified; // 고려대 학생 인증 여부

    // question: MapStruct를 적용해보는 게 나을까요?
    public static BaseUserDto from(User user) {
        return BaseUserDto.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .isKoreanUnivVerified(user.getIsKoreaUnivVerified())
                .build();
    }
}
