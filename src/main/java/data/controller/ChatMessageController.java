package data.controller;

import data.dto.ChatLogDto;
import data.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
public class ChatMessageController {

    private static final Logger logger = LoggerFactory.getLogger(ChatMessageController.class);

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    // 생성자 주입만 사용
    public ChatMessageController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 그룹 채팅 메시지 처리
     *
     * @param chatLogDto 메시지 데이터
     * @param groupId    그룹 ID
     */
    @MessageMapping("/chat.send.group/{groupId}")
    public void sendGroupMessage(
            @Payload ChatLogDto chatLogDto,
            @DestinationVariable("groupId") String groupId) {
        logger.info("Received group message for groupId: {}, content: {}", groupId, chatLogDto);

        validateChatLogDto(chatLogDto, groupId);
        validateGroupIdMatch(chatLogDto, groupId);

        try {
            chatService.saveChatMessage(chatLogDto);
            String destination = "/topic/group/" + groupId;
            messagingTemplate.convertAndSend(destination, chatLogDto);
            logger.debug("Broadcasted message to: {}", destination);
        } catch (Exception e) {
            logger.error("Failed to process group message for groupId: {}", groupId, e);
            throw new RuntimeException("Failed to process group message", e);
        }
    }

    /**
     * 1:1 채팅 메시지 처리
     *
     * @param chatLogDto 메시지 데이터
     * @return ChatLogDto
     */
    @MessageMapping("/chat.send.private")
    @SendToUser("/queue/private")
    public ChatLogDto sendPrivateMessage(@Payload ChatLogDto chatLogDto) {
        logger.info("Received private message: {}", chatLogDto);

        validateChatLogDto(chatLogDto, null);

        try {
            chatService.saveChatMessage(chatLogDto);
            return chatLogDto;
        } catch (Exception e) {
            logger.error("Failed to process private message", e);
            throw new RuntimeException("Failed to process private message", e);
        }
    }

    /**
     * ChatLogDto 유효성 검사
     *
     * @param chatLogDto 검사할 DTO
     * @param groupId    그룹 ID (null일 수 있음)
     */
    private void validateChatLogDto(ChatLogDto chatLogDto, String groupId) {
        if (chatLogDto == null) {
            logger.error("ChatLogDto is null for groupId: {}", groupId);
            throw new IllegalArgumentException("ChatLogDto cannot be null");
        }
        if (chatLogDto.getMessage() == null || chatLogDto.getMessage().trim().isEmpty()) {
            logger.error("Message content is empty for groupId: {}, ChatLogDto: {}", groupId, chatLogDto);
            throw new IllegalArgumentException("Message content cannot be empty");
        }
        if (chatLogDto.getSenderId() == null) {
            logger.error("Sender ID is null for groupId: {}, ChatLogDto: {}", groupId, chatLogDto);
            throw new IllegalArgumentException("Sender ID cannot be null");
        }
    }

    /**
     * groupId와 ChatLogDto의 roomId 일치 여부 검사
     *
     * @param chatLogDto 메시지 데이터
     * @param groupId    그룹 ID
     */
    private void validateGroupIdMatch(ChatLogDto chatLogDto, String groupId) {
        if (chatLogDto.getRoomId() == null) {
            logger.error("Room ID is null for groupId: {}", groupId);
            throw new IllegalArgumentException("Room ID cannot be null");
        }
        if (!groupId.equals(String.valueOf(chatLogDto.getRoomId()))) {
            logger.error("Group ID {} does not match ChatLogDto roomId: {}", groupId, chatLogDto.getRoomId());
            throw new IllegalArgumentException("Group ID and roomId must match");
        }
    }
}