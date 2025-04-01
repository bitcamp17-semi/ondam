package data.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import data.dto.SchedulesDto;
import data.mapper.SchedulesMapper;


@Service
public class SchedulesService {
	@Autowired
	SchedulesMapper schedulesMapper;
	
	//일정 등록
	public void scheduleInsert(Map<String, Object> map) {
    	schedulesMapper.scheduleInsert(map);
    }
	
	//전체 일정검색
	public List<SchedulesDto> readAllSche(){
		return schedulesMapper.readAllSche();
	}
	
	//일정 하나 검색
	public SchedulesDto readOneSche(int id)
	{
		return schedulesMapper.readOneSche(id);
	}
	
	//일정 삭제
	public void deleteSche(int id)
	{
		schedulesMapper.deleteSche(id);
	}
	
	//일정 수정
	public void updateSchedule(SchedulesDto dto)
	{
		schedulesMapper.updateSchedule(dto);
	}
	
}
