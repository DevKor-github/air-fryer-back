package com.airfryer.repicka.common.security.jwt;

import com.airfryer.repicka.common.security.oauth2.CustomOAuth2User;
import com.airfryer.repicka.domain.user.entity.user.User;
import com.airfryer.repicka.domain.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter
{
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException
    {
        // 모든 쿠키 가져오기
        Cookie[] cookies = request.getCookies();

        if(cookies != null)
        {
            // 쿠키 순회
            for (Cookie cookie : cookies)
            {
                // 쿠키 이름 조회
                String name = cookie.getName();

                if (name.equals("accessToken"))
                {
                    // 쿠키 값 조회
                    String accessToken = cookie.getValue();

                    // 토큰이 유효한지 체크
                    if(jwtUtil.checkToken(accessToken))
                    {
                        Long id = jwtUtil.getUserIdFromToken(accessToken);
                        User user = userRepository.findById(id).orElse(null);

                        // 존재하는 계정인지 체크
                        if(user != null)
                        {
                            OAuth2User oAuth2User = new CustomOAuth2User(user);

                            // 접근 권한 인증 토큰 생성
                            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(oAuth2User, null, oAuth2User.getAuthorities());

                            // 현재 요청의 security context에 접근 권한 부여
                            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                        }
                    }
                }
            }
        }

        // 다음 필터로 전달
        filterChain.doFilter(request, response);
    }
}
