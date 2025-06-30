package com.airfryer.repicka.common.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.ZonedDateTime;
import java.util.Date;

@Component
public class JwtUtil
{
    private final Key key;

    // 생성자
    public JwtUtil(@Value("${JWT_SECRET}") String secret)
    {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // 토큰 생성
    public String createToken(Long userId, Token tokenType)
    {
        Claims claims = Jwts.claims();
        claims.put("userId", userId);

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime expiresAt = now.plusSeconds(tokenType.getValidTime());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(Date.from(now.toInstant()))
                .setExpiration(Date.from(expiresAt.toInstant()))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰을 쿠키로 변환
    public ResponseCookie parseTokenToCookie(String token, Token tokenType)
    {
        return ResponseCookie.from(tokenType.getName(), token)
                .httpOnly(tokenType.isHttpOnly())
                .domain("devkor-github.github.io")
                .sameSite("None")
                .secure(true)
                .path("/")
                .maxAge(tokenType.getValidTime())
                .build();
    }

    // 토큰 검증
    public boolean checkToken(String token)
    {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 토큰에서 User id 추출
    public Long getUserIdFromToken(String token)
    {
        if(checkToken(token))
        {
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
            return claims.get("userId", Long.class);
        }

        return null;
    }
}
