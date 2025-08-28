package com.airfryer.repicka.domain.notification;

import com.airfryer.repicka.common.response.SuccessResponseDto;
import com.airfryer.repicka.common.security.oauth2.CustomOAuth2User;
import com.airfryer.repicka.domain.notification.dto.GetNotificationsReq;
import com.airfryer.repicka.domain.notification.dto.GetNotificationsRes;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
public class NotificationController {
    
    private final NotificationService notificationService;
    
    // 알림 목록 조회
    @GetMapping
    public ResponseEntity<SuccessResponseDto> getNotifications(@AuthenticationPrincipal CustomOAuth2User user,
                                                               @Valid GetNotificationsReq dto)
    {
        GetNotificationsRes data = notificationService.getNotifications(user.getUser().getId(), dto);
        
        return ResponseEntity.status(HttpStatus.OK)
            .body(SuccessResponseDto.builder()
                .message("알림 목록을 성공적으로 조회하였습니다.")
                .data(data)
                .build());
    }
}
