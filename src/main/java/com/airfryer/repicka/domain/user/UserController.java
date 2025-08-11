package com.airfryer.repicka.domain.user;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.airfryer.repicka.common.security.oauth2.CustomOAuth2User;

import jakarta.validation.Valid;

import com.airfryer.repicka.common.response.SuccessResponseDto;
import com.airfryer.repicka.common.aws.s3.dto.PresignedUrlReq;
import com.airfryer.repicka.common.aws.s3.dto.PresignedUrlRes;
import com.airfryer.repicka.domain.user.dto.BaseUserDto;
import com.airfryer.repicka.domain.user.dto.UpdateUserReq;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // fcm 토큰 업데이트
    @PatchMapping("/fcm-token")
    public ResponseEntity<SuccessResponseDto> updateFcmToken(@AuthenticationPrincipal CustomOAuth2User user,
                                                             @RequestBody String fcmToken) {
        userService.updateFcmToken(user.getUser().getId(), fcmToken);
        return ResponseEntity.ok(SuccessResponseDto.builder()
            .message("푸시 알림 설정이 성공적으로 변경되었습니다.")
            .build());
    }

    // 푸시 알림 설정 업데이트
    @PatchMapping("/push-enabled")
    public ResponseEntity<SuccessResponseDto> updatePush(@AuthenticationPrincipal CustomOAuth2User user,
                                                             @RequestBody Boolean isPushEnabled) {
        userService.updatePush(user.getUser().getId(), isPushEnabled);
        return ResponseEntity.ok(SuccessResponseDto.builder()
            .message("푸시 알림 설정이 성공적으로 변경되었습니다.")
            .build());
    }

    // S3 Presigned URL 조회
    @GetMapping("/presigned-url")
    public ResponseEntity<SuccessResponseDto> getPresignedUrl(@Valid PresignedUrlReq req) {
        PresignedUrlRes presignedUrlRes = userService.getPresignedUrl(req);
        return ResponseEntity.ok(SuccessResponseDto.builder()
            .message("프로필 이미지 업로드 준비가 완료되었습니다.")
            .data(presignedUrlRes)
            .build());
    }

    // 프로필 조회
    @GetMapping
    public ResponseEntity<SuccessResponseDto> getProfile(@AuthenticationPrincipal CustomOAuth2User user) {
        BaseUserDto userDetail = userService.getProfile(user.getUser().getId());
        return ResponseEntity.ok(SuccessResponseDto.builder()
            .message("프로필을 성공적으로 조회하였습니다.")
            .data(userDetail)
            .build());
    }

    // 프로필 업데이트
    @PutMapping
    public ResponseEntity<SuccessResponseDto> updateProfile(@AuthenticationPrincipal CustomOAuth2User user,
                                                             @Valid @RequestBody UpdateUserReq req) {
        BaseUserDto userDetail = userService.updateProfile(user.getUser().getId(), req);
        return ResponseEntity.ok(SuccessResponseDto.builder()
            .message("프로필을 성공적으로 수정하였습니다.")
            .data(userDetail)
            .build());
    }
}
