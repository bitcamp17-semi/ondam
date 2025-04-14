package data.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface JunctionChatUsersGroupsMapper {
    void createGroupUser(Long userId, Long groupId); // 그룹에 사용자 추가
    List<Long> readGroupUserIds(Long groupId); // 그룹에 속한 사용자 ID 목록 조회
    List<Long> readJoinedGroupIds(Long userId); // 사용자가 가입한 그룹 ID 목록 조회
}