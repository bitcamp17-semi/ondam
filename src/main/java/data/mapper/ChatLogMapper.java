package data.mapper;

import org.apache.ibatis.annotations.Mapper;
import data.dto.ChatLogDto;

import java.util.List;

@Mapper
public interface ChatLogMapper {
	public void createChatLog(ChatLogDto chatLog);            // 메시지 생성
    public List<ChatLogDto> readChatLogsByGroupId(int groupId); // 그룹 내 메시지 조회
    public void updateChatLog(ChatLogDto chatLog);            // 메시지 수정
    public void deleteChatLog(long id);                       // 메시지 삭제
}
