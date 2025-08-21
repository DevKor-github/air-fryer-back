package com.airfryer.repicka.common.security.oauth2.dto;

import com.airfryer.repicka.domain.user.entity.user.LoginMethod;
import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public class AppleResponse implements OAuth2Response
{
    private final Map<String, Object> attribute;

    @Override
    public LoginMethod getProvider() {
        return LoginMethod.APPLE;
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
        return attribute.get("id_token").toString();
    }
}
