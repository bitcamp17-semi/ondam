package data.service;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import data.dto.ChatLogDto;
import data.mapper.ChatLogMapper;

@Service
public class ChatLogService {

    @Autowired
    private ChatLogMapper chatLogMapper;

    // C: 메시지 생성
    public void createChatLog(ChatLogDto chatLog) {
        chatLog.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        chatLogMapper.createChatLog(chatLog);
    }

    // R: 그룹 메시지 조회
    public List<ChatLogDto> readChatLogsByGroupId(int groupId) {
        return chatLogMapper.readChatLogsByGroupId(groupId);
    }

    // U: 메시지 수정
    public void updateChatLog(ChatLogDto chatLog) {
        chatLogMapper.updateChatLog(chatLog);
    }

    // D: 메시지 삭제
    public void deleteChatLog(long id) {
        chatLogMapper.deleteChatLog(id);
    }   
 
}
