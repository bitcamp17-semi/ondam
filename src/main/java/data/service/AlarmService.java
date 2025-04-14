package data.service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.ObjectMapper;

import data.dto.AlarmDto;
import data.mapper.AlarmMapper;
import data.repository.AlarmEmitterRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AlarmService {
	final AlarmEmitterRepository emitterRepository;
	final AlarmMapper alarmMapper;
	
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
    	dto.setIsRead('0'); //기본으로 읽지않은 상태 저장
    	
    	insertAlarm(dto);
    }
    
    //모든 알람 조회
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
  	
  	//알림 상태 읽음으로 변경
  	public void updateIsRead(List<Integer> ids)
  	{
  		alarmMapper.updateIsRead(ids);
  	}
  	
  	
  	//일정 등록 시 선택된 그룹의 멤버들(본인포함)한테 알람전송
  	public void sendScheduleAlarmGroupMem(List<Integer> groupMems, int groupOwnerId, int causedBy)
  	{
  		String content = "일정이 등록되었습니다.";
  		
  		// 중복 제거: groupMems + groupOwnerId
  	    Set<Integer> allTargets = new HashSet<>(groupMems);
  	    allTargets.add(groupOwnerId); // owner도 알림 대상에 추가
  		
  		for(int userId : allTargets)
  		{
  			//알람 dto 생성
  			AlarmDto dto =new AlarmDto();
  	    	dto.setType(AlarmDto.AlarmType.SCHEDULE);
  	    	dto.setUserId(userId); // 알림 받을 사람
  	        dto.setCausedBy(causedBy); // 알림 발생자 (일정 등록자)
  	    	dto.setContent(content);
  	    	dto.setCreatedAt(new Timestamp(System.currentTimeMillis()));
  	    	dto.setIsRead(0); //기본으로 읽지않은 상태 저장
  	    	
  			//alarm DB 저장
  			insertAlarm(dto);
  			
  			//SSE 전송
  			SseEmitter emitter = emitterRepository.get((long) userId);
  	        if (emitter != null) {
  	            try {
  	                emitter.send(SseEmitter.event().name("alarm").data(content));
  	            } catch (Exception e) {
  	                emitter.completeWithError(e);
  	                emitterRepository.remove((long) userId, emitter);
  	                e.printStackTrace();
  	            }
  			} 
  		}
  	}
  	
  	//쪽지 받으면 받는 사람한테만 알람 발생하도록하기
  	public void receivedMessageAlarm(int userId, int causedBy)
  	{
  			String content = "쪽지가 도착했습니다.";//알람 문구 지정 > 수정해도 됨		
  			
  			//알람 dto 생성
  			AlarmDto dto =new AlarmDto();
  	    	dto.setType(AlarmDto.AlarmType.MESSAGE);
  	    	dto.setUserId(userId); //받은 사람 id 저장해야함
  	    	dto.setCausedBy(causedBy);//보낸 사람 저장해야함
  	    	dto.setContent(content);//알람 내용
  	    	dto.setCreatedAt(new Timestamp(System.currentTimeMillis()));//쪽지 받은 시간 저장하기
  	    	dto.setIsRead(0); //기본으로 읽지않은 상태 저장
  	    	
  			//alarm DB 저장
  			insertAlarm(dto);
  			
  			//SSE 전송
  			SseEmitter emitter = emitterRepository.get((long) userId);
  			if(emitter !=null) {
  				try {
  					//토스트로 알람 노출 시키는 부분
  					Map<String, Object> alarmData = new HashMap<>();
  					alarmData.put("type", "MESSAGE");
  					alarmData.put("content", content);
  					alarmData.put("causedBy", causedBy);
  					
  					//System.out.println("SSE 전송 시도: userId = " + groupMem);
  				  	emitter.send(SseEmitter.event().name("alarm").data(content));
  				  	//System.out.println(" SSE 전송 성공: " + content);
				} catch (Exception e) {
					// TODO: handle exception
					//System.out.println("SSE 전송 실패: userId = " + groupMem);
					emitter.completeWithError(e);//명시적으로 연결종료
					emitterRepository.remove((long) userId, emitter);
			        e.printStackTrace();
				}
  			} 
  	}
  	
  	//내가 작성한 게시글에 댓글이 달린경우 알람 발생
  	public void addRepleMyBoard(int userId, int causedBy)
  	{
  			String content = "댓글이 달렸습니다.";//알람 문구 지정 > 수정해도 됨		
  			
  			//알람 dto 생성
  			AlarmDto dto =new AlarmDto();
  	    	dto.setType(AlarmDto.AlarmType.MESSAGE);
  	    	dto.setUserId(userId); //게시글 작성한 사람의 id를 저장
  	    	dto.setCausedBy(causedBy);//댓글 작성한 사람 id 저장
  	    	dto.setContent(content);//알람 내용
  	    	dto.setCreatedAt(new Timestamp(System.currentTimeMillis()));//쪽지 받은 시간 저장하기
  	    	dto.setIsRead(0); //기본으로 읽지않은 상태 저장
  	    	
  			//alarm DB 저장
  			insertAlarm(dto);
  			
  			//SSE 전송
  			SseEmitter emitter = emitterRepository.get((long) userId);
  			if(emitter !=null) {
  				try {
  					//토스트로 알람 노출 시키는 부분
  					Map<String, Object> alarmData = new HashMap<>();
  					alarmData.put("type", "BOARD");
  					alarmData.put("content", content);
  					alarmData.put("causedBy", causedBy);
  					
  					//System.out.println("SSE 전송 시도: userId = " + groupMem);
  				  	emitter.send(SseEmitter.event().name("alarm").data(content));
  				  	//System.out.println(" SSE 전송 성공: " + content);
				} catch (Exception e) {
					// TODO: handle exception
					//System.out.println("SSE 전송 실패: userId = " + groupMem);
					emitter.completeWithError(e);//명시적으로 연결종료
					emitterRepository.remove((long) userId, emitter);
			        e.printStackTrace();
				}
  			} 
  	}
  	
  	//내가 해야할 결제가 생긴(내 차례가 된) 경우 알람 발생
  	public void approvalTurnAlarm(int userId, int causedBy)
  	{
  			String content = "확인 할 결제가 생겼습니다.";//알람 문구 지정 > 수정해도 됨		
  			
  			//알람 dto 생성
  			AlarmDto dto =new AlarmDto();
  	    	dto.setType(AlarmDto.AlarmType.APPROVAL);
  	    	dto.setUserId(userId); //결제를 해야하는 사람의 id
  	    	dto.setCausedBy(causedBy);//결제를 올린사람
  	    	dto.setContent(content);//알람 내용
  	    	dto.setCreatedAt(new Timestamp(System.currentTimeMillis()));//쪽지 받은 시간 저장하기
  	    	dto.setIsRead(0); //기본으로 읽지않은 상태 저장

  	    	System.out.println("알림 발생 시도: userId=" + userId + ", causedBy=" + causedBy + ", type=" + dto.getType());
  			//alarm DB 저장
  			insertAlarm(dto);
  			System.out.println("insertAlarm 호출 완료");
  			
  			
  			//SSE 전송
  			SseEmitter emitter = emitterRepository.get((long) userId);
  			if(emitter !=null) {
  				try {
  					//토스트로 알람 노출 시키는 부분
  					Map<String, Object> alarmData = new HashMap<>();
  					alarmData.put("type", "APPROVAL");
  					alarmData.put("content", content);
  					alarmData.put("causedBy", causedBy);
  					
  					//System.out.println("SSE 전송 시도: userId = " + groupMem);
  				  	emitter.send(SseEmitter.event().name("alarm").data(content));
  				  	//System.out.println(" SSE 전송 성공: " + content);
				} catch (Exception e) {
					// TODO: handle exception
					//System.out.println("SSE 전송 실패: userId = " + groupMem);
					emitter.completeWithError(e);//명시적으로 연결종료
					emitterRepository.remove((long) userId, emitter);
			        e.printStackTrace();
				}
  			} 
  	}
  	
  	//결제가 최종 승인된 경우 알람 발생
  	public void confirmedApprovalAlarm(int userId, int causedBy)
  	{
  			String content = "결제가 최종 승인되었습니다.";//알람 문구 지정 > 수정해도 됨		
  			
  			//알람 dto 생성
  			AlarmDto dto =new AlarmDto();
  	    	dto.setType(AlarmDto.AlarmType.SYSTEM);
  	    	dto.setUserId(userId); //결제올린 사람의 id
  	    	dto.setCausedBy(causedBy);//최종 승인한 사람 id
  	    	dto.setContent(content);//알람 내용
  	    	dto.setCreatedAt(new Timestamp(System.currentTimeMillis()));//쪽지 받은 시간 저장하기
  	    	dto.setIsRead(0); //기본으로 읽지않은 상태 저장
  	    	
  			//alarm DB 저장
  			insertAlarm(dto);
  			
  			//SSE 전송
  			SseEmitter emitter = emitterRepository.get((long) userId);
  			if(emitter !=null) {
  				try {
  					//토스트로 알람 노출 시키는 부분
  					Map<String, Object> alarmData = new HashMap<>();
  					alarmData.put("type", "SYSTEM");
  					alarmData.put("content", content);
  					alarmData.put("causedBy", causedBy);
  					
  					//System.out.println("SSE 전송 시도: userId = " + groupMem);
  				  	emitter.send(SseEmitter.event().name("alarm").data(content));
  				  	//System.out.println(" SSE 전송 성공: " + content);
				} catch (Exception e) {
					// TODO: handle exception
					//System.out.println("SSE 전송 실패: userId = " + groupMem);
					emitter.completeWithError(e);//명시적으로 연결종료
					emitterRepository.remove((long) userId, emitter);
			        e.printStackTrace();
				}
  			} 
  	}
  	
  	//결제가 반려된 경우 알람 발생
  	public void rejectedApprovalAlarm(int userId, int causedBy)
  	{
  			String content = "결제가 반려되었습니다.";//알람 문구 지정 > 수정해도 됨		
  			
  			//알람 dto 생성
  			AlarmDto dto =new AlarmDto();
  	    	dto.setType(AlarmDto.AlarmType.APPROVAL);
  	    	dto.setUserId(userId); //
  	    	dto.setCausedBy(causedBy);//결제 올린 사람 id
  	    	dto.setContent(content);//알람 내용
  	    	dto.setCreatedAt(new Timestamp(System.currentTimeMillis()));//반려한 사람의 id 저장
  	    	dto.setIsRead(0); //기본으로 읽지않은 상태 저장
  	    	
  			//alarm DB 저장
  			insertAlarm(dto);
  			
  			//SSE 전송
  			SseEmitter emitter = emitterRepository.get((long) userId);
  			if(emitter !=null) {
  				try {
  					//토스트로 알람 노출 시키는 부분
  					Map<String, Object> alarmData = new HashMap<>();
  					alarmData.put("type", "APPROVAL");
  					alarmData.put("content", content);
  					alarmData.put("causedBy", causedBy);
  					
  					//System.out.println("SSE 전송 시도: userId = " + groupMem);
  				  	emitter.send(SseEmitter.event().name("alarm").data(content));
  				  	//System.out.println(" SSE 전송 성공: " + content);
				} catch (Exception e) {
					// TODO: handle exception
					//System.out.println("SSE 전송 실패: userId = " + groupMem);
					emitter.completeWithError(e);//명시적으로 연결종료
					emitterRepository.remove((long) userId, emitter);
			        e.printStackTrace();
				}
  			} 
  	}

}
