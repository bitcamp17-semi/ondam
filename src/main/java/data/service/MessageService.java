package data.service;

import data.dto.MessagesDto;
import data.mapper.MessageMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MessageService {

    private final MessageMapper messageMapper;

    public MessageService(MessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }


    // 특정 사용자가 받은 메시지 조회 (Read)
    public List<MessagesDto> getMessagesForReceiver(int receiverId) {
        List<MessagesDto> messages = messageMapper.readMessagesForReceiver(receiverId);
        if (messages.isEmpty()) {
            throw new IllegalArgumentException("No messages found for receiver ID: " + receiverId);
        }
        return messages;
    }

    // 메시지를 읽음으로 처리 (Update)
    public void markMessageAsRead(int messageId) {
        try {
            messageMapper.updateMessageAsRead(messageId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to mark message as read. ID: " + messageId, e);
        }
    }

    // 마지막 방문 시간 업데이트 (Update)
    public void updateLastVisit(int userId) {
        messageMapper.updateUserLastVisitTime(userId);
    }

    public void deleteMessage(int messageId) {
        try {
            messageMapper.deleteMessageById(messageId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete message. ID: " + messageId, e);
        }
    }

    // 새로운 메시지 생성 (Create)
    public void createMessage(MessagesDto messageDto) {
        messageMapper.createMessage(messageDto);
    }
    // 검색 기능
    public List<MessagesDto> readMessages(String keyword, String category) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Keyword cannot be null or empty");
        }
        Map<String, Object> params = new HashMap<>();
        params.put("keyword", keyword.trim());
        params.put("category", category);

        List<MessagesDto> messages = messageMapper.readMessages(params);
        if (messages.isEmpty()) {
            throw new IllegalArgumentException("No messages found for the given keyword: " + keyword);
        }
        return messages;
    }
    // 쪽지 상세 조회(Service)
    public MessagesDto readMessageDetail(int messageId) {
        MessagesDto message = messageMapper.readMessageDetail(messageId);
        if (message == null) {
            throw new IllegalArgumentException("Message with ID " + messageId + " not found");
        }
        return message;
    }



}
