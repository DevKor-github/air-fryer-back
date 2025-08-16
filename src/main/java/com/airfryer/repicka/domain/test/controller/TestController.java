package com.airfryer.repicka.domain.test.controller;

import com.airfryer.repicka.common.response.SuccessResponseDto;
import com.airfryer.repicka.common.security.oauth2.CustomOAuth2User;
import com.airfryer.repicka.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.airfryer.repicka.common.firebase.dto.FCMNotificationReq;
import com.airfryer.repicka.common.firebase.service.FCMService;
import com.airfryer.repicka.common.firebase.type.NotificationType;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/test")
public class TestController
{
    private final FCMService fcmService;

    // 로그인 여부 테스트
    @GetMapping("/is-login")
    public ResponseEntity<SuccessResponseDto> testUser(@AuthenticationPrincipal CustomOAuth2User oAuth2User)
    {
        User user = oAuth2User.getUser();

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("로그인을 수행한 사용자입니다.")
                        .data(user.getId())
                        .build());
    }
    
    // FCM 테스트
    // TODO: 테스트 완료 후 프로덕션 배포 전 삭제
    @PostMapping("/fcm")
    public ResponseEntity<SuccessResponseDto> testFCM(@RequestBody String fcmToken) {
        fcmService.sendNotification(fcmToken, FCMNotificationReq.of(NotificationType.CHAT_MESSAGE, "1234567890", "test"));

        return ResponseEntity.ok(SuccessResponseDto.builder()
            .message("FCM 테스트 메시지를 성공적으로 전송하였습니다.")
            .build());
    }
}
