package config;


import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import data.dto.UsersDto;
import jakarta.servlet.http.HttpSession;

import java.util.Map;

public class WebSocketInterceptor implements ChannelInterceptor, HandshakeInterceptor {

    // WebSocket 핸드셰이크 전 처리
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            HttpSession session = servletRequest.getServletRequest().getSession(false);
            if (session != null) {
                UsersDto user = (UsersDto) session.getAttribute("user");
                if (user == null) {
                    return false; // 사용자가 로그인하지 않은 경우 연결 거부
                }
                // WebSocket 세션 속성에 사용자 정보 추가
                attributes.put("user", user);
            } else {
                return false; // 세션이 없는 경우 연결 거부
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 핸드셰이크 후 처리 (필요 시 구현)
    }

    // STOMP 메시지 처리 전 인터셉트
    @Override
    public org.springframework.messaging.Message<?> preSend(org.springframework.messaging.Message<?> message,
                                                           org.springframework.messaging.MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (accessor.getCommand() != null && accessor.getCommand().name().equals("CONNECT")) {
            // CONNECT 프레임 처리 시 세션에서 사용자 정보 확인
            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
            if (sessionAttributes != null) {
                UsersDto user = (UsersDto) sessionAttributes.get("user");
                if (user != null) {
                    // STOMP 헤더에 사용자 정보 추가
                    accessor.setUser(new SimplePrincipal(user.getId() + ""));
                }
            }
        }
        return message;
    }
}

// STOMP 메시지에 사용자 정보를 추가하기 위한 간단한 Principal 구현
class SimplePrincipal implements java.security.Principal {
    private final String name;

    public SimplePrincipal(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}