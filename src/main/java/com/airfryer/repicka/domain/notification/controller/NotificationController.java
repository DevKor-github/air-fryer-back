package com.airfryer.repicka.domain.notification.controller;

import com.airfryer.repicka.common.response.SuccessResponseDto;
import com.airfryer.repicka.common.security.oauth2.CustomOAuth2User;
import com.airfryer.repicka.domain.notification.dto.NotificationRes;
import com.airfryer.repicka.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
public class NotificationController {
    
    private final NotificationService notificationService;
    
    /**
     * 인증된 사용자의 알림 목록 조회
     * @param user 인증된 사용자 정보
     * @return 알림 목록
     */
    @GetMapping
    public ResponseEntity<SuccessResponseDto> getNotifications(@AuthenticationPrincipal CustomOAuth2User user) {
        List<NotificationRes> notifications = notificationService.getNotifications(user.getUser().getId());
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("알림 목록을 성공적으로 조회하였습니다.")
                        .data(notifications)
                        .build());
    }
}