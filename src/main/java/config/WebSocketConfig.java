package config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // 메시지 브로커 구성 (/topic 으로 구독 허용)
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // 구독 주소
        config.setApplicationDestinationPrefixes("/app"); // 메시지 발송 주소
    }

    // STOMP 엔드포인트 등록 (SockJS 지원)
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-chat") // 클라이언트 연결 지점
                .setAllowedOriginPatterns("*") // 모든 Origin 허용
                .withSockJS(); // SockJS 지원
    }
}
