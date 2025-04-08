package data.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import data.dto.ChatLogDto;

@Mapper
public interface ChatLogMapper {

    // C: 메시지 생성
    public void createChatLog(ChatLogDto chatLog);

    // R: 그룹 내 메시지 조회
    public List<ChatLogDto> readChatLogsByGroupId(int groupId);

    // U: 메시지 수정
    public void updateChatLog(ChatLogDto chatLog);

    // D: 메시지 삭제
    public void deleteChatLog(long id);
    
    
}
