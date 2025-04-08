package data.mapper;

import data.dto.ChatGroupsDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ChatGroupsMapper {

    // C: 그룹 생성
    public void createChatGroup(ChatGroupsDto chatGroup);

    // R: 그룹 ID로 조회
    public ChatGroupsDto readChatGroupById(int id);

    // R: 모든 그룹 조회
    public List<ChatGroupsDto> readAllChatGroups();

    // U: 그룹 수정
    public void updateChatGroup(ChatGroupsDto chatGroup);

    // D: 그룹 삭제
    public void deleteChatGroup(int id);
    
    public ChatGroupsDto readGroupById(int groupId);
    
    public List<ChatGroupsDto> readGroupsByUserId(int userId);
}
