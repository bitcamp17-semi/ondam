package data.service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import data.dto.AlarmDto;
import data.mapper.AlarmMapper;
import data.repository.AlarmEmitterRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AlarmService {
	final AlarmEmitterRepository emitterRepository;
	final AlarmMapper alarmMapper;
	
	//알람 발생
    public void sendScheduleNotification(Long userId, String scheduleTitle) {
        //SseEmitter emitter = emitterRepository.get(userId);
    	List<SseEmitter> originalEmitters = emitterRepository.get((long) userId);
        List<SseEmitter> emitters = new ArrayList<>(originalEmitters); // 복사본 생성
    	for (SseEmitter emitter : emitters) {
    	//if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("schedule")
                        .data("새 일정 등록: " + scheduleTitle));
            } catch (IOException e) {
            	emitter.completeWithError(e);//명시적으로 연결 종료
            	emitterRepository.remove((long) userId, emitter);
            }
        }
    }
    
    //알람 db에 저장
    public void insertAlarm(AlarmDto dto)
    {
    	alarmMapper.insertAlarm(dto);
    }
    
    //알람보낼 id, 발생 id, 내용 받아서 db에 저장
    public void insertAlarm(int userId, int causedBy, String content)
    {
    	AlarmDto dto =new AlarmDto();
    	dto.setType(AlarmDto.AlarmType.SYSTEM);//기본값으로 저장되도록
    	dto.setUserId(userId);
    	dto.setCausedBy(causedBy);
    	dto.setContent(content);
    	dto.setCreatedAt(new Timestamp(System.currentTimeMillis()));
    	dto.setRead(false); //기본으로 읽지않은 상태 저장
    	
    	insertAlarm(dto);
    }
    
    //userId가 받은 모든 알람 조회
  	public List<AlarmDto> allAlarm(int userId, int startNum, int perPage)
  	{
  		return alarmMapper.allAlarm(userId, startNum,perPage);
  	}
  	
  	//읽지 않은 알람 조회
  	public List<AlarmDto> unreadAlarm(int userId, int startNum, int perPage)
  	{
  		return alarmMapper.unreadAlarm(userId, startNum,perPage);
  	}
  	
  	//읽은 알람 조회
  	public List<AlarmDto> readAlarm(int userId, int startNum, int perPage)
  	{
  		return alarmMapper.readAlarm(userId, startNum,perPage);
  	}
  	
  	//모든 알람 개수 조회
  	public int countAllAlarm(int userId)
  	{
  		return alarmMapper.countAllAlarm(userId);
  	}
  	
  	//읽지 않은 알람 개수 조회
  	public int countUnreadAlarm(int userId)
  	{
  		return alarmMapper.countUnreadAlarm(userId);
  	}
  	
  	//읽은 알람 개수 조회
  	public int countReadAlarm(int userId)
  	{
  		return alarmMapper.countReadAlarm(userId);
  	}
  	
  	//알람 읽음 상태 수정
  	public void updateIsRead(int id)
  	{
  		alarmMapper.updateIsRead(id);
  	}
  	
  	//일정 등록 시 선택된 그룹의 멤버들(본인포함)한테 알람전송
  	public void sendScheduleAlarmGroupMem(List<Integer> groupMems, int causedBy, String content)
  	{
  		for(int groupMem : groupMems)
  		{
  			//알람 dto 생성
  			AlarmDto dto =new AlarmDto();
  	    	dto.setType(AlarmDto.AlarmType.SCHEDULE);
  	    	dto.setUserId(groupMem);
  	    	dto.setCausedBy(causedBy);
  	    	dto.setContent(content);
  	    	dto.setCreatedAt(new Timestamp(System.currentTimeMillis()));
  	    	dto.setRead(false); //기본으로 읽지않은 상태 저장
  	    	
  			//alarm DB 저장
  			insertAlarm(dto);
  			
  			//SSE 전송
  			List<SseEmitter> originalEmitters = emitterRepository.get((long) groupMem);
  	        List<SseEmitter> emitters = new ArrayList<>(originalEmitters); // 복사본 생성
  			for (SseEmitter emitter : emitters) {
  			//if(emitter !=null) {
  				try {
  					//System.out.println("SSE 전송 시도: userId = " + groupMem);
  				  	emitter.send(SseEmitter.event().name("alarm").data(content));
  				  	//System.out.println(" SSE 전송 성공: " + content);
				} catch (Exception e) {
					// TODO: handle exception
					//System.out.println("SSE 전송 실패: userId = " + groupMem);
					emitter.completeWithError(e);//명시적으로 연결종료
					emitterRepository.remove((long) groupMem, emitter);
			        e.printStackTrace();
				}
  			} 
  		}
  	}
  	
  	

}
