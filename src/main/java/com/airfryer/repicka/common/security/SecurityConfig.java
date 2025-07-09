package com.airfryer.repicka.common.security;

import com.airfryer.repicka.common.security.exception.CustomAccessDeniedHandler;
import com.airfryer.repicka.common.security.exception.CustomAuthenticationEntryPoint;
import com.airfryer.repicka.common.security.jwt.JwtFilter;
import com.airfryer.repicka.common.security.oauth2.CustomOAuth2UserService;
import com.airfryer.repicka.common.security.oauth2.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig
{
    // OAuth 처리 서비스
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    // JWT 필터
    private final JwtFilter jwtFilter;

    // 예외 처리 클래스
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception
    {
        // 기본 설정
        httpSecurity
                .httpBasic(HttpBasicConfigurer::disable)
                .csrf(CsrfConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .headers(HeadersConfigurer::disable)
                .sessionManagement(c -> c.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS));

        // Oauth 2.0 설정
        httpSecurity
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                                .userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                );

        // URL 기반 권한 설정
        httpSecurity
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/v1/item/presigned-url").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/item/**").permitAll()
                        .requestMatchers("/login/**", "/oauth2/**").permitAll()
                        .anyRequest().authenticated()
                );

        // 예외 처리 설정
        httpSecurity
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler));

        // JWT 필터 추가
        httpSecurity.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    // JWT 필터 서블릿 등록 비활성화
    @Bean
    public FilterRegistrationBean<JwtFilter> jwtFilterRegistration()
    {
        FilterRegistrationBean<JwtFilter> registration = new FilterRegistrationBean<>(jwtFilter);
        registration.setEnabled(false);
        return registration;
    }

    // CORS 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource()
    {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "https://devkor-github.github.io"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setExposedHeaders(List.of("Set-Cookie"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
