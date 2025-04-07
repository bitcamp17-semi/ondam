package data.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import data.dto.ScheduleGroupDto;
import data.mapper.ScheduleGroupMapper;

@Service
public class ScheduleGroupService {
	@Autowired
	ScheduleGroupMapper scheduleGroupMapper;
	
	//등록
	public void scheGroupInsert(Map<String, Object> map) {
		scheduleGroupMapper.scheGroupInsert(map);
	}
	
	//내가 그룹장이거나 그룹 인원인 그룹목록 조회 
	public List<ScheduleGroupDto> readAllGroup(int userId){
		return scheduleGroupMapper.readAllGroup(userId);
	}
	
	//그룹명이 '개인일정'이고 'ownerId'가 본인 일정이 있는지 확인
	public ScheduleGroupDto readPrivateGroup(int userId)
	{
		return scheduleGroupMapper.readPrivateGroup(userId);
	}
	
	//그룹명이 '회사일정'이고 멤버로 사용자가 있는지 확인
	public Integer readCompanyGroupMember(int userId)
	{
		return scheduleGroupMapper.readCompanyGroupMember(userId);
	}
	
	//그룹명이 '회사일정'인 groupId 조회
	public Integer readCompanyGroupId()
	{
		return scheduleGroupMapper.readCompanyGroupId();
	}
	
	//그룹 수정
	public void updateSchGroup(ScheduleGroupDto dto) 
	{
		scheduleGroupMapper.updateSchGroup(dto);
	}
	
	//그룹 삭제
	public void deleteScheGroup(int groupId)
	{
		scheduleGroupMapper.deleteScheGroup(groupId);
	}
}
