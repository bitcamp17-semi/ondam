package data.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import data.dto.ScheduleGroupDto;

@Mapper
public interface ScheduleGroupMapper {
	//등록
	public void scheGroupInsert(Map<String,Object> paramMap);
	
	//내가 그룹장이거나 그룹 인원인 경우 그룹 목록 조회
	public List<ScheduleGroupDto> readAllGroup(int userId);
	
	//그룹명이 '개인일정'이고 'ownerId'가 본인 일정이 있는지 확인
	public ScheduleGroupDto readPrivateGroup(int userId);
	
	//그룹명이 '회사일정'이고 멤버로 사용자가 있는지 확인
	public Integer readCompanyGroupMember(int userId);
	
	//그룹명이 '회사일정'인 groupId 조회
	public Integer readCompanyGroupId();
	
	// groupId로 그룹 정보 가져오기
	public ScheduleGroupDto getGroupById(int groupId);
	
	//그룹 수정
	public void updateSchGroup(ScheduleGroupDto dto);
	
	//그룹 삭제
	public void deleteScheGroup(int groupId);
	
	//부서 그룹 조회
	public Integer readBuseoGroupId(@Param("departmentId") int departmentId);
}
