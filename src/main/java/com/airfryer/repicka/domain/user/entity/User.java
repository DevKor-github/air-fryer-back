package com.airfryer.repicka.domain.user.entity;

import com.airfryer.repicka.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"email", "login_method"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class User extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 사용자 식별자

    @NotNull
    private String email; // 이메일

    @NotNull
    private String nickname; // 별명

    @NotNull
    @Enumerated(EnumType.STRING)
    private LoginMethod loginMethod; // 소셜 로그인 정보

    @NotNull
    private String oauthId; // OAuth2 제공자에서 발급해주는 ID

    @NotNull
    @Enumerated(EnumType.STRING)
    private Role role; // 권한

    private String profileImageUrl; // 프로필 이미지 URL

    @NotNull
    private Boolean isKoreaUnivVerified; // 고려대 학생 인증 여부

    @Enumerated(EnumType.STRING)
    private Gender gender; // 성별 (F,M)

    private Integer height; // 키
    private Integer weight; // 몸무게
    private String fcmToken; // 푸시알림 토큰

    @NotNull
    private int todayPostCount; // 오늘 등록한 게시글 개수

    @NotNull
    private LocalDate lastAccessDate; // 마지막 접속 날짜

    /// 객체 비교

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;
        return id != null && id.equals(user.getId());
    }

    /// 해시 코드

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
