package com.airfryer.repicka.domain.user.entity;

import com.airfryer.repicka.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId; // 사용자 식별자

    @NotNull
    private String email; // 이메일

    @NotNull
    private String nickname; // 별명

    @NotNull
    private String loginMethod; // 소셜 로그인 정보

    @NotNull
    @Enumerated(EnumType.STRING)
    private Role role; // 권한

    @NotNull
    private String profileImageUrl; // 프로필 이미지 URL

    @NotNull
    private boolean isKoreaUnivVerified; // 고려대 학생 인증 여부

    @Enumerated(EnumType.STRING)
    private Gender gender; // 성별 (F,M)

    private int height; // 키
    private int weight; // 몸무게
    private String fcmToken; // 푸시알림 토큰

    @NotNull
    private int todayPostCount; // 오늘 등록한 게시글 개수

    @NotNull
    private LocalDate lastAccessDate; // 마지막 접속 날짜
}
