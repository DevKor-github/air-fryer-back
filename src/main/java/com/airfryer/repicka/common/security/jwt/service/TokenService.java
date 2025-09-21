package com.airfryer.repicka.common.security.jwt.service;

import com.airfryer.repicka.common.exception.CustomException;
import com.airfryer.repicka.common.exception.CustomExceptionCode;
import com.airfryer.repicka.common.security.jwt.JwtUtil;
import com.airfryer.repicka.common.security.jwt.Token;
import com.airfryer.repicka.domain.user.entity.user.User;
import com.airfryer.repicka.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TokenService
{
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    /// 서비스

    // Access token 재발급
    @Transactional(readOnly = true)
    public ResponseCookie reissueAccessToken(String refreshToken)
    {
        /// Refresh token 유효성 체크
        /// 1. Refresh token이 존재하는가?
        /// 2. Refresh token이 유효한가?

        // Refresh token 존재 여부 체크
        if(refreshToken == null) {
            throw new CustomException(CustomExceptionCode.REFRESH_TOKEN_NOT_FOUND, null);
        }

        // 토큰이 유효한지 체크
        if(!jwtUtil.checkToken(refreshToken)) {
            throw new CustomException(CustomExceptionCode.INVALID_REFRESH_TOKEN, refreshToken);
        }

        /// 계정 존재 확인

        // 사용자 ID 추출
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);

        // 계정 존재 확인 및 탈퇴 여부 체크
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(CustomExceptionCode.USER_NOT_FOUND, userId));
        if(user.getIsDeleted()) {
            throw new CustomException(CustomExceptionCode.USER_NOT_FOUND, null);
        }

        /// Access token 재발급

        // Access token 발급
        String accessToken = jwtUtil.createToken(userId, Token.ACCESS_TOKEN);

        // 토큰을 쿠키로 변환 후, 반환
        return jwtUtil.parseTokenToCookie(accessToken, Token.ACCESS_TOKEN);
    }

    // 로그아웃
    public void logout(HttpServletResponse response)
    {
        ResponseCookie accessTokenCookie = expireAccessToken();
        ResponseCookie refreshTokenCookie = expireRefreshToken();

        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
    }

    /// 공통 로직

    // Access token 만료
    public ResponseCookie expireAccessToken()
    {
        return jwtUtil.deleteTokenCookie(Token.ACCESS_TOKEN);
    }

    // Refresh token 만료
    public ResponseCookie expireRefreshToken()
    {
        return jwtUtil.deleteTokenCookie(Token.REFRESH_TOKEN);
    }
}
