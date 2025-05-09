package com.airfryer.repicka.common.security.oauth2;

import com.airfryer.repicka.common.security.oauth2.dto.GoogleResponse;
import com.airfryer.repicka.common.security.oauth2.dto.OAuth2Response;
import com.airfryer.repicka.domain.user.entity.User;
import com.airfryer.repicka.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.parameters.P;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = null;
        if (registrationId.equals("google")) {
            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        }
        else {
            return null;
        }

        Optional<User> existUser = userRepository.findByOauthIdAndLoginMethod(oAuth2Response.getProviderId(), oAuth2Response.getProvider());
        if (existUser == null) {
            // TODO: 사용자 추가
        }
        else {
            // TODO: 이미 존재하는 사용자에 대한 처리
        }

        return oAuth2User;
    }
}
