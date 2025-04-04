package data.mapper;

import org.apache.ibatis.annotations.Mapper;
import data.dto.JuncChatUsersGroupsDto;

import java.util.List;

/**
 * 사용자와 채팅 그룹 간의 관계 관련 데이터베이스 작업을 위한 매퍼 인터페이스
 */
@Mapper
public interface JunctionChatUsersGroupsMapper {
	public void createJunction(JuncChatUsersGroupsDto junction);         // 사용자-그룹 관계 생성
	public List<JuncChatUsersGroupsDto> readJunctionByUserId(int userId); // 사용자-그룹 관계 조회
	public List<JuncChatUsersGroupsDto> readUsersByGroupId(int groupId);  // 그룹에 속한 사용자 조회
	public void deleteJunction(JuncChatUsersGroupsDto junction);         // 사용자-그룹 관계 삭제
}
