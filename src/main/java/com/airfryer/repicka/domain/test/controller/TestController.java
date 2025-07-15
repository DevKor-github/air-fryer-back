package com.airfryer.repicka.domain.test.controller;

import com.airfryer.repicka.common.response.SuccessResponseDto;
import com.airfryer.repicka.common.security.oauth2.CustomOAuth2User;
import com.airfryer.repicka.domain.user.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController
{
    // 로그인 여부 테스트
    @GetMapping("/test/is-login")
    public ResponseEntity<SuccessResponseDto> testUser(@AuthenticationPrincipal CustomOAuth2User oAuth2User)
    {
        User user = oAuth2User.getUser();

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("로그인을 수행한 사용자입니다.")
                        .data(user.getId())
                        .build());
    }
}
