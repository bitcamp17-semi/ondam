package data.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import data.dto.JuncChatUsersGroupsDto;

@Mapper
public interface JunctionChatUsersGroupsMapper {

    // C: 사용자-그룹 관계 생성
    void createJunction(JuncChatUsersGroupsDto junction);

    // R: 사용자 ID로 관계 조회
    List<JuncChatUsersGroupsDto> readJunctionByUserId(@Param("userId") int userId);

    // R: 그룹 ID로 사용자 조회
    List<JuncChatUsersGroupsDto> readUsersByGroupId(@Param("groupId") int groupId);

    // R: 특정 사용자-그룹 관계 존재 여부
    boolean readExistsJunction(@Param("userId") int userId, @Param("groupId") int groupId);

    // D: 사용자-그룹 관계 삭제
    void deleteJunction(JuncChatUsersGroupsDto junction);
    
    // C: 사용자 초대
    void createUserGroupLink(JuncChatUsersGroupsDto dto);
}
