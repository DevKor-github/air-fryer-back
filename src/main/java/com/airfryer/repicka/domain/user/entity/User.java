package com.airfryer.repicka.domain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long user_id; // 사용자 식별자

    @Column(nullable = false)
    private String email; // 이메일

    @Column(nullable = false)
    private String nickname; // 별명

    @Column(nullable = false)
    private String login_method; // 소셜 로그인 정보

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role; // 권한

    @Column(nullable = false)
    private String profile_image_url; // 프로필 이미지 URL

    @Column(nullable = false)
    private boolean is_korea_univ_verified; // 고려대 학생 인증 여부

    @Enumerated(EnumType.STRING)
    private Gender gender; // 성별 (F,M)

    private int height; // 키
    private int weight; // 몸무게
    private String fcm_token; // 푸시알림 토큰

    @Column(nullable = false)
    private int today_post_count; // 오늘 등록한 게시글 개수

    @Column(nullable = false)
    private LocalDate last_access_date; // 마지막 접속 날짜

    @Column(nullable = false)
    private LocalDate created_at; // 레코드 생성 날짜

    @Column(nullable = false)
    private LocalDate updated_at; // 레코드 수정 날짜
}
