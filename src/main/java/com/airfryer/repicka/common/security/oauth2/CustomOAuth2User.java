package com.airfryer.repicka.common.security.oauth2;

import com.airfryer.repicka.domain.user.entity.User;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
@EqualsAndHashCode
public class CustomOAuth2User implements OAuth2User
{
    private final User user;
    private Map<String, Object> attributes;

    public CustomOAuth2User(User user) {
        this.user = user;
    }

    // OAuth 2.0 로그인 생성자
    public CustomOAuth2User(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority(user.getRole().name()));
    }

    @Override
    public String getName() {
        return user.getId().toString();
    }
}
