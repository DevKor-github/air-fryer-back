package com.airfryer.repicka.common.security.oauth2;

import com.airfryer.repicka.common.security.jwt.JwtUtil;
import com.airfryer.repicka.common.security.jwt.Token;
import com.airfryer.repicka.domain.user.entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler
{
    private final JwtUtil jwtUtil;

    @Value("${FRONTEND_URI}")
    private String frontendURI;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException
    {
        // 계정 정보
        CustomOAuth2User userDetails = (CustomOAuth2User) authentication.getPrincipal();
        User user = userDetails.getUser();

        // 토큰 생성
        String accessToken = jwtUtil.createToken(user.getId(), Token.ACCESS_TOKEN);
        String refreshToken = jwtUtil.createToken(user.getId(), Token.REFRESH_TOKEN);

        // 토큰으로 쿠키 생성
        Cookie accessTokenCookie = jwtUtil.parseTokenToCookie(accessToken, Token.ACCESS_TOKEN);
        Cookie refreshTokenCookie = jwtUtil.parseTokenToCookie(refreshToken, Token.REFRESH_TOKEN);

        // 쿠키 저장
        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        // 리다이렉트
        response.sendRedirect(frontendURI);
    }
}
