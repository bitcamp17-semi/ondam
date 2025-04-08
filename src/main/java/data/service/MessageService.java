package data.service;

import data.dto.MessagesDto;
import data.dto.UsersDto;
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
    public List<MessagesDto> readMessagesForReceiver(int receiverId) {
        return messageMapper.readMessagesForReceiver(receiverId);
    }

    // 메시지를 읽음으로 처리 (Update)
    public void markMessageAsRead(int messageId) {
        messageMapper.updateMessageAsRead(messageId);
    }

    // 마지막 방문 시간 업데이트 (Update)
    public void updateLastVisit(int userId) {
        messageMapper.updateUserLastVisitTime(userId);
    }

    public void deleteMessage(int messageId) {
        messageMapper.deleteMessageById(messageId);
    }

    // 새로운 메시지 생성 (Create)
    public void createMessage(MessagesDto messageDto) {
        messageMapper.createMessage(messageDto);
    }
    // 검색 기능
    public List<MessagesDto> readSearchMessagesByKeyword(String keyword, String category) {
        Map<String, Object> params = new HashMap<>();
        params.put("keyword", keyword.trim());
        params.put("category", category.trim());
        return messageMapper.readSearchMessagesByKeyword(params);
    }
    // 쪽지 상세 조회(Service)=
    public MessagesDto readMessageDetail(int messageId) {
        return messageMapper.readMessageDetail(messageId);
    }
    public void markMessageAsImportant(int messageId, boolean isImportant) {
        messageMapper.updateMessageImportance(messageId, isImportant);
    }
    public boolean readCountUnreadMessages(int receiverId) {
        return messageMapper.readCountUnreadMessages(receiverId) > 0;
    }
    public List<String> readAllDepartment() {
        return messageMapper.readAllDepartment();
    }
    public List<UsersDto> readUsersByDepartment(String department) {
        return messageMapper.readUsersByDepartment(department);
    }


}
