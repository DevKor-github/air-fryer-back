package com.airfryer.repicka.common.security.exception;

import com.airfryer.repicka.common.exception.CustomExceptionCode;
import com.airfryer.repicka.common.response.ExceptionResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

// 권한이 부족한 요청 예외 처리
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler
{
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException
    {
        ObjectMapper objectMapper = new ObjectMapper();

        ExceptionResponseDto responseDto = ExceptionResponseDto.builder()
                .code(CustomExceptionCode.LOW_AUTHORITY.name())
                .message(CustomExceptionCode.LOW_AUTHORITY.getMessage())
                .data(null)
                .build();

        response.setStatus(CustomExceptionCode.LOW_AUTHORITY.getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(responseDto));
        response.getWriter().flush();
    }
}
