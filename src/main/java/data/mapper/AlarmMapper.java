package data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import data.dto.AlarmDto;

@Mapper
public interface AlarmMapper {
	//등록
	public void insertAlarm(AlarmDto dto);
	
	//userId가 받은 모든 알람 조회
	public List<AlarmDto> readAllAlarm(int userId);
}
