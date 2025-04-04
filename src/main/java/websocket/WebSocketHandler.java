package websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import data.dto.ChatLogDto;
import data.service.ChatLogService;

@Controller
public class WebSocketHandler {

	@Autowired
    private SimpMessagingTemplate messagingTemplate; // 메시지 전송을 위한 템플릿

    @Autowired
    private ChatLogService chatLogService; // 채팅 로그 저장 서비스

    // 클라이언트가 /app/sendMessage로 메시지를 보내면 이 메서드가 호출됨
    @MessageMapping("/sendMessage")
    public void handleMessage(@Payload ChatLogDto chatLog, StompHeaderAccessor headerAccessor) {
        // WebSocketInterceptor에서 추가한 사용자 정보 확인
        if (headerAccessor.getUser() != null) {
            String userId = headerAccessor.getUser().getName();
            chatLog.setSenderId(Integer.parseInt(userId));
        }

        // 데이터베이스에 채팅 로그 저장
        chatLogService.createChatLog(chatLog);

        // 해당 그룹의 모든 사용자에게 메시지 브로드캐스트
        messagingTemplate.convertAndSend("/topic/group/" + chatLog.getGroupId(), chatLog);
    }
}