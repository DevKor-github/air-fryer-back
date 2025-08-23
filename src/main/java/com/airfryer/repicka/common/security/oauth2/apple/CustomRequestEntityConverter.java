package com.airfryer.repicka.common.security.oauth2.apple;

import com.airfryer.repicka.common.exception.CustomException;
import com.airfryer.repicka.common.exception.CustomExceptionCode;
import io.jsonwebtoken.Jwts;
import io.micrometer.core.instrument.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

// 애플 로그인 시, client-secret을 동적으로 생성해주는 클래스
@Service
@Slf4j
public class CustomRequestEntityConverter implements Converter<OAuth2AuthorizationCodeGrantRequest, RequestEntity<?>>
{
    private final OAuth2AuthorizationCodeGrantRequestEntityConverter defaultConverter;

    public CustomRequestEntityConverter() {
        defaultConverter = new OAuth2AuthorizationCodeGrantRequestEntityConverter();
    }

    @Value("${APPLE_CLIENT_ID}")
    private String APPLE_CLIENT_ID;

    @Value("${APPLE_CLIENT_SECRET}")
    private String APPLE_CLIENT_SECRET;

    @Value("${APPLE_TEAM_ID}")
    private String APPLE_TEAM_ID;

    @Value("${APPLE_KEY_ID}")
    private String APPLE_KEY_ID;

    // 커스텀 client-secret을 생성하여 RequestEntity 반환
    @Override
    public RequestEntity<?> convert(OAuth2AuthorizationCodeGrantRequest request)
    {
        // OAuth2 클라이언트 정보 추출
        String registrationId = request.getClientRegistration().getRegistrationId();

        // 기본 converter로 RequestEntity 생성
        RequestEntity<?> entity = defaultConverter.convert(request);
        MultiValueMap<String, String> params = (MultiValueMap<String, String>) Objects.requireNonNull(entity).getBody();

        // 애플 로그인의 경우
        if(registrationId.contains("apple"))
        {
            // client-secret 생성 및 대입
            try {
                Objects.requireNonNull(params).set("client_secret", createClientSecret());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new CustomException(CustomExceptionCode.CREATE_CLIENT_SECRET_FAILED, e.getMessage());
            }
        }

        return new RequestEntity<>(
                params, entity.getHeaders(),
                entity.getMethod(), entity.getUrl()
        );
    }

    // 커스텀 client-secret 생성
    public String createClientSecret() throws IOException
    {
        // JWT 헤더
        Map<String, Object> jwtHeader = new HashMap<>();
        jwtHeader.put("kid", APPLE_KEY_ID);
        jwtHeader.put("alg", "ES256");

        return Jwts.builder()
                .setHeaderParams(jwtHeader)
                .setIssuer(APPLE_TEAM_ID)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(Date.from(LocalDateTime.now().plusDays(30).atZone(ZoneId.systemDefault()).toInstant()))
                .setAudience("https://appleid.apple.com")
                .setSubject(APPLE_CLIENT_ID)
                .signWith(getPrivateKey())
                .compact();
    }

    // 애플 key를 사용하여 서명
    public PrivateKey getPrivateKey() throws IOException
    {
        String keyPath = "/app/secrets/" + APPLE_CLIENT_SECRET;
        FileSystemResource resource = new FileSystemResource(keyPath);

        try (InputStream in = resource.getInputStream();
             PEMParser pemParser = new PEMParser(new StringReader(IOUtils.toString(in, StandardCharsets.UTF_8)))) {
            PrivateKeyInfo object = (PrivateKeyInfo) pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            return converter.getPrivateKey(object);
        }
    }
}
