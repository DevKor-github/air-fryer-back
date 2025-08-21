package com.airfryer.repicka.common.security.oauth2.dto;

import com.airfryer.repicka.domain.user.entity.user.LoginMethod;
import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public class KakaoResponse implements OAuth2Response
{
    private Map<String, Object> attributes;

    @Override
    public LoginMethod getProvider() {
        return LoginMethod.KAKAO;
    }

    @Override
    public String getProviderId() {
        return attributes.get("id").toString();
    }

    @Override
    public String getEmail() {
        return ((Map<?, ?>) attributes.get("kakao_account")).get("email").toString();
    }

    @Override
    public String getName() {
        return ((Map<?, ?>) ((Map<?, ?>) attributes.get("kakao_account")).get("profile")).get("nickname").toString();
    }
}
