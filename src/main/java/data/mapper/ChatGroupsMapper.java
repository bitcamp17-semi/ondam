package data.mapper;

import data.dto.ChatGroupsDto;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface ChatGroupsMapper {
    void createGroup(ChatGroupsDto chatGroupsDto); // 그룹 생성
    List<ChatGroupsDto> readAllGroupsWithLastMessages(Long userId); // 사용자가 속한 그룹과 마지막 메시지 조회
    ChatGroupsDto readGroupById(Integer firstChatId); // 특정 그룹 조회
    void updateGroup(ChatGroupsDto chatGroupsDto); // 그룹 정보 수정
    void deleteGroup(Long groupId); // 그룹 삭제
    void createGroupUser(Long userId, Long groupId); // 그룹에 사용자 추가
    List<Long> readGroupUserIds(Long groupId); // 그룹에 속한 사용자 ID 목록 조회
    List<Long> readJoinedGroupIds(Long userId); // 사용자가 가입한 그룹 ID 목록 조회
    List<ChatGroupsDto> readAllGroupsByUserId(Long userId); // 사용자가 속한 모든 그룹 조회
    ChatGroupsDto readPrivateChatBetweenUsers(Long userId1, Long userId2);
	Integer getLastGroupId();
    
}