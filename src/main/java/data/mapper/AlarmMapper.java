package data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import data.dto.AlarmDto;

@Mapper
public interface AlarmMapper {
	//등록
	public void insertAlarm(AlarmDto dto);
	
	//userId가 받은 모든 알람 조회
	public List<AlarmDto> allAlarm(@Param("userId")int userId, @Param("startNum") int startNum,@Param("perPage") int perPage);
	
	//모든 알람 개수 조회
	public int countAllAlarm(int userId);
	
	//읽지 않은 알람 조회
	public List<AlarmDto> unreadAlarm(@Param("userId")int userId, @Param("startNum") int startNum,@Param("perPage") int perPag);
	
	//읽지 않은 알람 개수 조회
	public int countUnreadAlarm(int userId);
	
	//읽은 알람 조회
	public List<AlarmDto> readAlarm(@Param("userId")int userId, @Param("startNum") int startNum,@Param("perPage") int perPag);
	
	//읽은 알람 개수 조회
	public int countReadAlarm(int userId);
	
	//알람 확인여부 업데이트
	public int updateIsRead(List<Integer> ids);
}
