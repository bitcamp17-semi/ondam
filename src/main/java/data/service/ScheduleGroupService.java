package data.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import data.mapper.ScheduleGroupMapper;

@Service
public class ScheduleGroupService {
	@Autowired
	ScheduleGroupMapper scheduleGroupMapper;
	
	//등록
	public void scheGroupInsert(Map<String, Object> map) {
		scheduleGroupMapper.scheGroupInsert(map);
	}
}
