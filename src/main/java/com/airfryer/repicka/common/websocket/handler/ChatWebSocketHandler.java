package com.airfryer.repicka.common.websocket.handler;

import com.airfryer.repicka.common.exception.CustomException;
import com.airfryer.repicka.common.exception.CustomExceptionCode;
import com.airfryer.repicka.common.security.jwt.JwtUtil;
import com.airfryer.repicka.domain.chat.dto.SendChatDto;
import com.airfryer.repicka.domain.chat.service.ChatService;
import com.airfryer.repicka.domain.user.entity.User;
import com.airfryer.repicka.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler implements WebSocketHandler
{
    private final UserRepository userRepository;
    private final ChatService chatService;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(WebSocketSession session)
    {
        return session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .flatMap(payload -> {
                    try {

                        // 채팅 메시지 파싱
                        SendChatDto dto = objectMapper.readValue(payload, SendChatDto.class);

                        // 쿠키에서 Access token 추출
                        String token = extractToken(session);

                        // 사용자 조회
                        Long userId = jwtUtil.getUserIdFromToken(token);
                        User user = userRepository.findById(userId)
                                .orElseThrow(() -> new CustomException(CustomExceptionCode.USER_NOT_FOUND, userId));

                        // 메시지 전송
                        return chatService.sendMessage(user, dto);

                    } catch (Exception e) {
                        return Mono.error(new CustomException(CustomExceptionCode.NOT_LOGIN, null));
                    }
                })
                .then();
    }

    // WebSocket 세션으로부터 Access token 추출
    private String extractToken(WebSocketSession session)
    {
        // 쿠키 조회
        List<String> cookieHeaders = session.getHandshakeInfo().getHeaders().get("Cookie");

        // 쿠키가 없다면 예외 처리
        if(cookieHeaders == null || cookieHeaders.isEmpty()) {
            throw new CustomException(CustomExceptionCode.ACCESS_TOKEN_NOT_FOUND, null);
        }

        // 쿠키를 순회하며 Access token 찾기
        for (String cookie : cookieHeaders)
        {
            String[] tokens = cookie.split(";");

            for (String token : tokens)
            {
                String[] pair = token.trim().split("=");

                if (pair.length == 2 && pair[0].equals("accessToken")) {
                    return pair[1];
                }
            }
        }

        throw new CustomException(CustomExceptionCode.ACCESS_TOKEN_NOT_FOUND, null);
    }
}
