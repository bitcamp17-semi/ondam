package data.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import data.dto.ScheduleGroupDto;

@Mapper
public interface ScheduleGroupMapper {
	//등록
	public void scheGroupInsert(Map<String,Object> paramMap);
	
	//내가 그룹장이거나 그룹 인원인 경우 그룹 목록 조회
	public List<ScheduleGroupDto> readAllGroup(int userId);
}
