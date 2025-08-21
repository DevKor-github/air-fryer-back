package com.airfryer.repicka.common.security.oauth2;

import com.airfryer.repicka.common.exception.CustomException;
import com.airfryer.repicka.common.exception.CustomExceptionCode;
import com.airfryer.repicka.common.security.oauth2.dto.AppleResponse;
import com.airfryer.repicka.common.security.oauth2.dto.GoogleResponse;
import com.airfryer.repicka.common.security.oauth2.dto.KakaoResponse;
import com.airfryer.repicka.common.security.oauth2.dto.OAuth2Response;
import com.airfryer.repicka.domain.user.entity.user.Role;
import com.airfryer.repicka.domain.user.entity.user.User;
import com.airfryer.repicka.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService
{
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException
    {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // OAuth2 로그인 응답
        OAuth2Response oAuth2Response;

        if (registrationId.equals("google")) {
            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        } else if (registrationId.equals("kakao")) {
            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
        } else if (registrationId.contains("apple")) {

            // ID 토큰 추출
            String idToken = userRequest.getAdditionalParameters().get("id_token").toString();

            // ID 토큰으로부터 사용자 정보 디코딩
            Map<String, Object> attributes = decodeIdToken(idToken);
            attributes.put("id_token", idToken);

            oAuth2Response = new AppleResponse(attributes);

        } else {
            return null;
        }

        // 이미 존재하는 사용자인지 확인
        Optional<User> existUser = userRepository.findByOauthIdAndLoginMethod(oAuth2Response.getProviderId(), oAuth2Response.getProvider());
        User user;

        // 이미 존재하는 사용자가 아니라면 새로 추가
        if(existUser.isEmpty())
        {
            // 랜덤 4자리 숫자 생성
            Random random = new Random();
            int randomNumber = random.nextInt(9000) + 1000; // 1000~9999 범위의 4자리 숫자
            
            user = User.builder()
                    .email(oAuth2Response.getEmail())
                    .nickname("호랑이" + randomNumber)
                    .loginMethod(oAuth2Response.getProvider())
                    .oauthId(oAuth2Response.getProviderId())
                    .role(Role.USER)
                    .isKoreaUnivVerified(false)
                    .todayPostCount(0)
                    .lastAccessDate(LocalDate.now())
                    .build();

            userRepository.save(user);
        }
        else {
            user = existUser.get();
        }

        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }

    // 애플 ID 토큰으로부터 사용자 정보 추출
    public Map<String, Object> decodeIdToken(String idToken)
    {
        Map<String, Object> jwtClaims = new HashMap<>();

        try {
            String[] parts = idToken.split("\\.");
            Base64.Decoder decoder = Base64.getUrlDecoder();

            byte[] decodedBytes = decoder.decode(parts[1].getBytes(StandardCharsets.UTF_8));
            String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);

            Map<String, Object> map = objectMapper.readValue(decodedString, Map.class);
            jwtClaims.putAll(map);

        } catch (Exception e) {
            throw new CustomException(CustomExceptionCode.DECODE_ID_TOKEN_FAILED, e.getMessage());
        }
        return jwtClaims;
    }
}
