package data.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import data.dto.SchedulesDto;

@Mapper
public interface SchedulesMapper {
	
	//등록
	public void scheduleInsert(Map<String,Object> map);
	
	//전체 검색
	public List<SchedulesDto> readAllSche();
	
	//일정 하나 검색
	public SchedulesDto readOneSche(int id);
	
	//일정 삭제
	public void deleteSche(int id);
	
	//일정 수정
	public void updateSchedule(SchedulesDto dto);
}
