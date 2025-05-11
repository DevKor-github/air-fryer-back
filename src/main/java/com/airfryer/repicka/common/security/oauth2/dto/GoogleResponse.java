package com.airfryer.repicka.common.security.oauth2.dto;

import com.airfryer.repicka.domain.user.entity.LoginMethod;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class GoogleResponse implements OAuth2Response
{
    private final Map<String, Object> attribute;

    @Override
    public LoginMethod getProvider() {
        return LoginMethod.GOOGLE;
    }

    @Override
    public String getProviderId() {
        return attribute.get("sub").toString();
    }

    @Override
    public String getEmail() {
        return attribute.get("email").toString();
    }

    @Override
    public String getName() {
        return attribute.get("name").toString();
    }
}
