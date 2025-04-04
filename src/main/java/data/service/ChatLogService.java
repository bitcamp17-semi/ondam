package data.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import data.dto.ChatLogDto;
import data.mapper.ChatLogMapper;

import java.sql.Timestamp;
import java.util.List;

@Service
public class ChatLogService {

    @Autowired
    private ChatLogMapper chatLogMapper;

    // 메시지 생성 (Create)
    public void createChatLog(ChatLogDto chatLog) {
        chatLog.setCreatedAt(new Timestamp(System.currentTimeMillis())); // 생성 시간 설정
        chatLogMapper.createChatLog(chatLog);
    }

    // 그룹 내 메시지 조회 (Read)
    public List<ChatLogDto> readChatLogsByGroupId(int groupId) {
        return chatLogMapper.readChatLogsByGroupId(groupId);
    }

    // 메시지 수정 (Update)
    public void updateChatLog(ChatLogDto chatLog) {
        chatLogMapper.updateChatLog(chatLog);
    }

    // 메시지 삭제 (Delete)
    public void deleteChatLog(long id) {
        chatLogMapper.deleteChatLog(id);
    }
}