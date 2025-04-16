package data.mapper;

import data.dto.ChatLogDto;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface ChatLogMapper {
    void createChatLog(ChatLogDto chatLogDto); // 채팅 로그 저장
    List<ChatLogDto> readAllLogsByGroupId(Long groupId); // 그룹 채팅 메시지 전체 조회
    ChatLogDto readLastGroupMessage(Long groupId); // 그룹 채팅의 마지막 메시지 조회
    ChatLogDto readLastPrivateMessage(Map<String, Integer> params); // 1:1 채팅의 마지막 메시지 조회
    List<ChatLogDto> readAllPrivateChatsWithLastMessages(Long userId); // 사용자의 모든 1:1 채팅과 마지막 메시지 조회
    List<ChatLogDto> readAllPrivateLogs(Map<String, Integer> params); // 1:1 채팅 메시지 전체 조회
    List<ChatLogDto> readAllPrivateLogsByChatId(Long chatId);
    void updateChatLog(ChatLogDto chatLogDto);
}