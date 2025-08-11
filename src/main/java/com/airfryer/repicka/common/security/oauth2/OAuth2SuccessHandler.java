package com.airfryer.repicka.common.security.oauth2;

import com.airfryer.repicka.common.security.jwt.JwtUtil;
import com.airfryer.repicka.common.security.jwt.Token;
import com.airfryer.repicka.common.security.redirect.CustomAuthorizationRequestResolver;
import com.airfryer.repicka.domain.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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
                                        Authentication authentication) throws IOException
    {
        /// 계정 정보 확인

        // 계정 정보
        CustomOAuth2User userDetails = (CustomOAuth2User) authentication.getPrincipal();
        User user = userDetails.getUser();

        /// 토큰 쿠키 생성

        // 토큰 생성
        String accessToken = jwtUtil.createToken(user.getId(), Token.ACCESS_TOKEN);
        String refreshToken = jwtUtil.createToken(user.getId(), Token.REFRESH_TOKEN);

        // 토큰으로 쿠키 생성
        ResponseCookie accessTokenCookie = jwtUtil.parseTokenToCookie(accessToken, Token.ACCESS_TOKEN);
        ResponseCookie refreshTokenCookie = jwtUtil.parseTokenToCookie(refreshToken, Token.REFRESH_TOKEN);

        // 쿠키 저장
        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        /// 리다이렉트 경로 설정

        // 세션으로부터 리다이렉트 경로 추출
        HttpSession session = request.getSession();
        String redirectURI = (String) session.getAttribute(CustomAuthorizationRequestResolver.REDIRECT_URI_PARAMETER_NAME);

        // 세션에 리다이렉트 경로가 존재하지 않는다면, 기본 경로로 리다이렉트하도록 설정
        if(redirectURI == null || redirectURI.isBlank()) {
            redirectURI = frontendURI;
        }

        // 세션에서 리다이렉트 경로 제거
        session.removeAttribute(CustomAuthorizationRequestResolver.REDIRECT_URI_PARAMETER_NAME);

        // 리다이렉트
        response.sendRedirect(redirectURI + "?access-token=" + accessToken + "&refresh-token=" + refreshToken);

        // 로컬에서 WebSocket 채팅 전송 테스트 시, 리다이렉트 경로
        // response.sendRedirect("http://localhost:8080/chatTest.html" + "?access-token=" + accessToken + "&refresh-token=" + refreshToken);
    }
}
