package data.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import data.dto.ScheduleGroupMembersDto;
import data.mapper.ScheduleGroupMembersMapper;

@Service
public class ScheduleGroupMembersService {
	@Autowired
	ScheduleGroupMembersMapper scheduleGroupMemberMapper;
	
	//등록(여러명 등록이 가능하도록 처리하기)
	public void scheGroupMemberInsert(Map<String, Object> paramMap) {
	    scheduleGroupMemberMapper.scheGroupMemberInsert(paramMap);
	}
	
	//그룹아이디가 가진 그룹멤버 조회
	public List<ScheduleGroupMembersDto> readGroupMemByGroupId(int groupId) {
		return scheduleGroupMemberMapper.readGroupMemByGroupId(groupId);
	}
	
	//그룹멤버 전체 삭제
	public void deleteScheGroupMem(int groupId) {
		scheduleGroupMemberMapper.deleteScheGroupMem(groupId);
	}
	
	//특정 그룹에 유저가 멤버로 있는지 확인
	public Integer readGroupMemExist (@Param("groupId") int groupId, @Param("userId") int userId)
	{
		return scheduleGroupMemberMapper.readGroupMemExist(groupId, userId);
	}
}
