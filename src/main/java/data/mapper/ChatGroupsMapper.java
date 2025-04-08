package data.mapper;


import org.apache.ibatis.annotations.Mapper;
import data.dto.ChatGroupsDto;


import java.util.List;

@Mapper
public interface ChatGroupsMapper {
	public void createChatGroup(ChatGroupsDto chatGroup);  // 그룹 생성
	public ChatGroupsDto readChatGroupById(int id);        // 그룹 조회
	public List<ChatGroupsDto> readAllChatGroups();        // 모든 그룹 조회
	public void updateChatGroup(ChatGroupsDto chatGroup); // 그룹 수정
	public void deleteChatGroup(int id);                 // 그룹 삭제
}
