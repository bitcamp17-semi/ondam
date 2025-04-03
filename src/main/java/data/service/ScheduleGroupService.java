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
}
