package config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // WebSocket 메시지 브로커 활성화
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 클라이언트가 구독할 수 있는 경로 설정
        config.enableSimpleBroker("/sub"); // /topic으로 시작하는 경로로 메시지 브로드캐스트
        // 클라이언트가 서버로 메시지를 보낼 때 사용할 경로 접두사
        config.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 엔드포인트 설정
    	registry.addEndpoint("/chat-websocket")
        .setAllowedOrigins("http://localhost:8080"); // 특정 출처만 허용
        
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // WebSocketInterceptor 등록
        registration.interceptors(new WebSocketInterceptor());
    }
}