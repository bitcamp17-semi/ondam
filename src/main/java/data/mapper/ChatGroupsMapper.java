package data.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import data.dto.ChatGroupsDto;

import java.util.List;

@Mapper
public interface ChatGroupsMapper {
	// ID로 채팅 그룹 조회
    public ChatGroupsDto readById(@Param("id") int id);
    // 사용자 ID로 참여 중인 채팅 그룹 조회
    public List<ChatGroupsDto> readByUserId(@Param("userId") int userId);
    // 채팅 그룹 생성
    public void createGroupChat(ChatGroupsDto chatGroup);
    // 사용자를 그룹에 추가
    public void createUserToGroup(@Param("userId") int userId, @Param("groupId") int groupId);
}
