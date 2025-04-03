package data.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ScheduleGroupMapper {
	//등록
	public void scheGroupInsert(Map<String,Object> paramMap);
}
