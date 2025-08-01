package com.airfryer.repicka.common.security.redirect;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver
{
    private final OAuth2AuthorizationRequestResolver defaultResolver;

    public static final String REDIRECT_URI_PARAMETER_NAME = "redirectURI";

    public CustomAuthorizationRequestResolver(ClientRegistrationRepository repo) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(repo, "/oauth2/authorization");
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest req = defaultResolver.resolve(request);
        return customizeAuthorizationRequest(request, req);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest req = defaultResolver.resolve(request, clientRegistrationId);
        return customizeAuthorizationRequest(request, req);
    }

    private OAuth2AuthorizationRequest customizeAuthorizationRequest(HttpServletRequest request,
                                                                     OAuth2AuthorizationRequest authRequest)
    {
        if(authRequest != null)
        {
            // 요청 파라미터로부터 리다이렉트 경로 추출
            String redirectURI = request.getParameter(REDIRECT_URI_PARAMETER_NAME);

            // 세션 등록
            if(redirectURI != null) {
                request.getSession().setAttribute(REDIRECT_URI_PARAMETER_NAME, redirectURI);
            }
        }

        return authRequest;
    }
}
