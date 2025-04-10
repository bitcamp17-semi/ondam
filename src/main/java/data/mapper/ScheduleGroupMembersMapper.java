package data.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import data.dto.ScheduleGroupMembersDto;

@Mapper
public interface ScheduleGroupMembersMapper {
	//등록
	public void scheGroupMemberInsert(Map<String,Object> map);
	//그룹아이디가 가진 그룹멤버 조회
	public List<ScheduleGroupMembersDto> readGroupMemByGroupId(int groupId);
	//그룹멤버 전체 삭제
	public void deleteScheGroupMem(int groupId);
}
