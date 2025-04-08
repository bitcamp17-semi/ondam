package data.controller;

import data.dto.ChatLogDto;
import data.dto.ChatMessage;
import data.service.ChatLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class WebSocketChatController {

    @Autowired
    private ChatLogService chatLogService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(ChatMessage message) {
        // 1. ChatMessage → ChatLogDto 변환
        ChatLogDto chatLogDto = new ChatLogDto();
        chatLogDto.setGroupId(message.getGroupId());

        try {
            chatLogDto.setSenderId(Integer.parseInt(message.getSender()));
        } catch (NumberFormatException e) {
            System.out.println("⚠️ sender 변환 실패: " + e.getMessage());
            return;
        }

        chatLogDto.setMessage(message.getContent());
        chatLogDto.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

        // 2. DB 저장
        chatLogService.createChatLog(chatLogDto);

        // 3. 현재 시간 포맷 후 메시지에 세팅
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        message.setTime(LocalDateTime.now().format(formatter));

        // 4. 해당 그룹의 토픽으로 브로드캐스트
        messagingTemplate.convertAndSend("/topic/group/" + message.getGroupId(), message);
    }
}
