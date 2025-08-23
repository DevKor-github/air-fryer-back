package com.airfryer.repicka.domain.user;

import com.airfryer.repicka.domain.item.dto.res.OwnedItemListRes;
import com.airfryer.repicka.domain.user.dto.ReportUserReq;
import com.airfryer.repicka.domain.user.entity.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.airfryer.repicka.common.security.oauth2.CustomOAuth2User;

import jakarta.validation.Valid;

import com.airfryer.repicka.common.response.SuccessResponseDto;
import com.airfryer.repicka.common.aws.s3.dto.PresignedUrlReq;
import com.airfryer.repicka.common.aws.s3.dto.PresignedUrlRes;
import com.airfryer.repicka.domain.user.dto.BaseUserDto;
import com.airfryer.repicka.domain.user.dto.UpdateUserReq;

import lombok.RequiredArgsConstructor;

import java.util.List;

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

    // 유저 신고
    @PostMapping("/report")
    public ResponseEntity<SuccessResponseDto> reportUser(@AuthenticationPrincipal CustomOAuth2User customOAuth2User,
                                                         @RequestBody @Valid ReportUserReq dto)
    {
        User reporter = customOAuth2User.getUser();
        userService.reportUser(reporter, dto);

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("유저를 성공적으로 신고하였습니다.")
                        .data(null)
                        .build());
    }

    // 소유한 제품 리스트 조회
    @GetMapping("/{userId}/item")
    public ResponseEntity<SuccessResponseDto> getOwnedItemList(@PathVariable Long userId)
    {
        List<OwnedItemListRes> data = userService.getOwnedItemList(userId);

        return ResponseEntity.ok(SuccessResponseDto.builder()
                .message("해당 사용자가 소유한 제품 리스트를 성공적으로 조회하였습니다.")
                .data(data)
                .build());
    }
}
