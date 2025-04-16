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
	public List<AlarmDto> allAlarm(@Param("userId")int userId, @Param("type")String type, @Param("startNum") int startNum,@Param("perPage") int perPage);
	
	//모든 알람 개수 조회
	public int countAllAlarm(@Param("userId")int userId,@Param("type") String type);
	
	//읽지 않은 알람 조회
	public List<AlarmDto> unreadAlarm(@Param("userId") int userId, @Param("type")String type, @Param("startNum") int startNum,@Param("perPage") int perPag);
	
	//읽지 않은 알람 개수 조회
	public int countUnreadAlarm(@Param("userId")int userId,@Param("type") String type);
	
	//읽은 알람 조회
	public List<AlarmDto> readAlarm(@Param("userId")int userId, @Param("type")String type, @Param("startNum") int startNum,@Param("perPage") int perPag);
	
	//읽은 알람 개수 조회
	public int countReadAlarm(@Param("userId")int userId,@Param("type") String type);
	
	//알람 확인여부 업데이트
	public int updateIsRead(List<Integer> ids);

	public void deleteAlarm(List<Integer> ids);
}
