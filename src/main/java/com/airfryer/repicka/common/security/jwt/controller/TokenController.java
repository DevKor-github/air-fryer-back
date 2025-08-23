package com.airfryer.repicka.common.security.jwt.controller;

import com.airfryer.repicka.common.response.SuccessResponseDto;
import com.airfryer.repicka.common.security.jwt.service.TokenService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TokenController
{
    private final TokenService tokenService;

    // Access token 재발급
    @PostMapping("/refresh-token")
    @PreAuthorize("permitAll()")
    public ResponseEntity<SuccessResponseDto> reissueAccessToken(@CookieValue(name = "refreshToken") String refreshToken,
                                                                 HttpServletResponse response)
    {
        ResponseCookie cookie = tokenService.reissueAccessToken(refreshToken);
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("Access token을 성공적으로 재발급하였습니다.")
                        .data(null)
                        .build());
    }

    @PostMapping("/logout")
    @PreAuthorize("permitAll()")
    public ResponseEntity<SuccessResponseDto> logout(HttpServletResponse response)
    {
        ResponseCookie accessTokenCookie = tokenService.expireAccessToken();
        ResponseCookie refreshTokenCookie = tokenService.expireRefreshToken();

        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponseDto.builder()
                        .message("로그아웃 성공")
                        .data(null)
                        .build());
    }
}
