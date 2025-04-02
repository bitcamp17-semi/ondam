package websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import data.dto.ChatLogDto;
import data.service.ChatLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Component // Spring 관리 빈으로 등록
@RequiredArgsConstructor // Lombok을 사용하여 생성자 자동 생성
public class WebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper; // JSON 변환을 위한 ObjectMapper
    private final ChatLogService chatLogService; // ChatLogService 주입
    private final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("새로운 연결: {}", session.getId());
        sessions.add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
        	// JSON 메시지 로깅
        	log.info("수신된 JSON 메시지: {}", message.getPayload());
        	
            // JSON 메시지를 ChatLogDto로 변환
            ChatLogDto chatLogDto = objectMapper.readValue(message.getPayload(), ChatLogDto.class);

            // 채팅 로그 저장
            chatLogService.saveChatLog(chatLogDto);

            // 메시지 브로드캐스트
            for (WebSocketSession webSocketSession : sessions) {
                webSocketSession.sendMessage(message);
            }
        } catch (Exception e) {
            log.error("메시지 처리 오류: {}", e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("연결 종료: {}", session.getId());
        sessions.remove(session);
    }
}
