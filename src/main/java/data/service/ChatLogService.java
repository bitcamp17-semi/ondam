package data.service;

import data.dto.ChatLogDto;
import data.mapper.ChatLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j // Lombok 로깅 어노테이션 추가
public class ChatLogService {

    private final ChatLogMapper chatLogMapper;

    // 그룹 ID로 채팅 로그 조회
    public List<ChatLogDto> getLogsByGroupId(int groupId) {
        return chatLogMapper.readByGroupId(groupId);
    }

    // 두 사용자 간의 개인 메시지 조회
    public List<ChatLogDto> getPrivateMessages(int user1Id, int user2Id) {
        return chatLogMapper.readPrivateMessages(user1Id, user2Id);
    }

    // 채팅 로그 저장
    @Transactional
    public void saveChatLog(ChatLogDto chatLogDto) {
        try {
            chatLogMapper.createChatLog(chatLogDto);
            log.info("채팅 로그 저장 성공: {}", chatLogDto);
        } catch (Exception e) {
            log.error("채팅 로그 저장 실패: {}", e.getMessage());
            throw e; // 예외를 다시 던져서 상위 레벨에서 처리 가능하도록 함
        }
    }

    // 메시지를 읽음 상태로 변경
    public void markAsRead(int receiverId, int senderId) {
        chatLogMapper.readChat(receiverId, senderId);
    }

    // 그룹 ID로 페이징된 채팅 로그 조회
    public List<ChatLogDto> getLogsByGroupWithPagination(int groupId, int pageSize, int offset) {
        return chatLogMapper.readByGroupIdWithPagination(groupId, pageSize, offset);
    }

    // 두 사용자 간의 페이징된 개인 메시지 조회
    public List<ChatLogDto> getPrivateMessagesWithPagination(int user1Id, int user2Id, int pageSize, int offset) {
        return chatLogMapper.readPrivateMessagesWithPagination(user1Id, user2Id, pageSize, offset);
    }    
    
}
