package com.airfryer.repicka.domain.user;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.airfryer.repicka.common.security.oauth2.CustomOAuth2User;
import com.airfryer.repicka.common.response.SuccessResponseDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PatchMapping("/fcm-token")
    public ResponseEntity<SuccessResponseDto> updateFcmToken(@AuthenticationPrincipal CustomOAuth2User user,
                                                             @RequestBody String fcmToken) {
        userService.updateFcmToken(user.getUser().getId(), fcmToken);
        return ResponseEntity.ok(SuccessResponseDto.builder()
                .message("푸시 알림 설정이 성공적으로 변경되었습니다.")
                .build());
    }

    @PatchMapping("/push-enabled")
    public ResponseEntity<SuccessResponseDto> updatePush(@AuthenticationPrincipal CustomOAuth2User user,
                                                             @RequestBody Boolean isPushEnabled) {
        userService.updatePush(user.getUser().getId(), isPushEnabled);
        return ResponseEntity.ok(SuccessResponseDto.builder()
                .message("푸시 알림 설정이 성공적으로 변경되었습니다.")
                .build());
    }
}
