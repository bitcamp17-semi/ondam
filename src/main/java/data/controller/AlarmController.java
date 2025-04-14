package data.controller;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import data.repository.AlarmEmitterRepository;
import data.service.AlarmService;
import data.service.UsersService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping({"/alarm"})
@RequiredArgsConstructor
@Slf4j
public class AlarmController {
	private final AlarmEmitterRepository emitterRepository;
	final AlarmService alarmService;
	final UsersService userService;
	
	//SseEmitter 연결하기위한 컨트롤러
    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@RequestParam(name = "userId") Long userId) {
        SseEmitter emitter = new SseEmitter(60 * 10000L); // 10분
        // 이전 emitter는 무조건 삭제하고 새 emitter 저장
        emitterRepository.remove(userId);
        emitterRepository.save(userId, emitter);

        // 연결 종료 시 정리
        emitter.onCompletion(() -> emitterRepository.remove(userId));
        emitter.onTimeout(() -> emitterRepository.remove(userId));

        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("SSE 연결됨"));
            log.info("✅ SSE 연결 성공: userId = {}", userId);
        } catch (IOException e) {
            log.error("SSE 연결 초기 전송 실패", e);
        }
        return emitter;
    }
    
    //알람 전체 목록 호출
    @GetMapping("/all")
    public ResponseEntity<Object> readAllAlarms(
    		@RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
    		HttpSession session
    		) {
    	//로그인된 userId 받기
    	int userId=(Integer)session.getAttribute("userId");
    	
    	//userId가 가진 이름 받기
    	String userName=userService.readUserById(userId).getName();
    	
    	//페이징처리
        int perPage=5;//한페이지당 출력할 글의 갯수
        int perBlock=10;//한 블럭당 출력할 페이지 갯수
        int totalCount;//전체 게시글 갯수
        int totalPage;//총 페이지수
        int startNum;//각 페이지에서 가져올 시작번호 (mysql은 첫 데이타가 0번,오라클은 1번)
        int startPage;//각 블럭에서 출력할 시작페이지
        int endPage;//각 블럭에서 출력할 끝 페이지
        int no;//각 페이지에서 출력할 시작번호
    	
        //전체 알람 개수
        totalCount=alarmService.countAllAlarm(userId);
        totalPage=(int)Math.ceil((double)totalCount/perPage);
        
        //시작페이지
        startPage=(pageNum-1)/perBlock*perBlock+1;//예) 현재페이지가 7일 경우 startPage 가 (6) (perBlock이 5일경우)
        endPage=startPage+perBlock-1;//끝페이지
        //endPage 는 totalPage를 넘을수 없다.
        if(endPage>totalPage) {
            endPage=totalPage;
        }

        //각 페이지에서 불러올
        startNum=(pageNum-1)*perPage; //mysql은 첫글이 0번(오라클은 1번이므로 +1해야한다)
        
        //각페이지의 글앞에 출력할 시작번호(예: 총글이 20개일 경우 1페이지는 20,2페이즈는 15..)
        no=totalCount-(pageNum-1)*perPage;
        
        Map<String, Object> response = new LinkedHashMap<>();
        try {
        	//response.put("userName", userName);
        	response.put("totalCount", totalCount);
        	response.put("totalPage", totalPage);
        	response.put("currentPage", pageNum);
        	response.put("startPage", startPage);
        	response.put("endPage", endPage);
        	response.put("no", no);
        	
            response.put("status", "ok");
            response.put("result", alarmService.allAlarm(userId,startNum,perPage));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
        	e.printStackTrace();
        	response.put("status", "error");
            response.put("result", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    //읽은 알람 목록 호출
    @GetMapping("/read")
    public ResponseEntity<Object> readAlarms(
    		@RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
    		HttpSession session
    		) {
    	//로그인된 userId 받기
    	int userId=(Integer)session.getAttribute("userId");
    	//userId가 가진 이름 받기
    	String userName=userService.readUserById(userId).getName();
    	
    	//페이징처리
        int perPage=10;//한페이지당 출력할 글의 갯수
        int perBlock=10;//한 블럭당 출력할 페이지 갯수
        int totalCount;//전체 게시글 갯수
        int totalPage;//총 페이지수
        int startNum;//각 페이지에서 가져올 시작번호 (mysql은 첫 데이타가 0번,오라클은 1번)
        int startPage;//각 블럭에서 출력할 시작페이지
        int endPage;//각 블럭에서 출력할 끝 페이지
        int no;//각 페이지에서 출력할 시작번호
    	
        //전체 알람 개수
        totalCount=alarmService.countReadAlarm(userId);
        totalPage=(int)Math.ceil((double)totalCount/perPage);
        
        //시작페이지
        startPage=(pageNum-1)/perBlock*perBlock+1;//예) 현재페이지가 7일 경우 startPage 가 (6) (perBlock이 5일경우)
        endPage=startPage+perBlock-1;//끝페이지
        //endPage 는 totalPage를 넘을수 없다.
        if(endPage>totalPage) {
            endPage=totalPage;
        }

        //각 페이지에서 불러올
        startNum=(pageNum-1)*perPage; //mysql은 첫글이 0번(오라클은 1번이므로 +1해야한다)
        
        //각페이지의 글앞에 출력할 시작번호(예: 총글이 20개일 경우 1페이지는 20,2페이즈는 15..)
        no=totalCount-(pageNum-1)*perPage;
    	
        Map<String, Object> response = new LinkedHashMap<>();
        try {
        	response.put("userName", userName);
        	response.put("totalCount", totalCount);
        	response.put("totalPage", totalPage);
        	response.put("currentPage", pageNum);
        	response.put("startPage", startPage);
        	response.put("endPage", endPage);
        	response.put("no", no);
        	
            response.put("status", "ok");
            response.put("result", alarmService.readAlarm(userId, startNum,perPage));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("result", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    //읽지않은 알람 목록 호출
    @GetMapping("/unread")
    public ResponseEntity<Object> unreadAlarms(
    		@RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
    		HttpSession session
    		) {
    	//로그인된 userId 받기
    	int userId=(Integer)session.getAttribute("userId");
    	//userId가 가진 이름 받기
    	String userName=userService.readUserById(userId).getName();
    	
    	//페이징처리
        int perPage=10;//한페이지당 출력할 글의 갯수
        int perBlock=10;//한 블럭당 출력할 페이지 갯수
        int totalCount;//전체 게시글 갯수
        int totalPage;//총 페이지수
        int startNum;//각 페이지에서 가져올 시작번호 (mysql은 첫 데이타가 0번,오라클은 1번)
        int startPage;//각 블럭에서 출력할 시작페이지
        int endPage;//각 블럭에서 출력할 끝 페이지
        int no;//각 페이지에서 출력할 시작번호
    	
        //전체 알람 개수
        totalCount=alarmService.countUnreadAlarm(userId);
        totalPage=(int)Math.ceil((double)totalCount/perPage);
        
        //시작페이지
        startPage=(pageNum-1)/perBlock*perBlock+1;//예) 현재페이지가 7일 경우 startPage 가 (6) (perBlock이 5일경우)
        endPage=startPage+perBlock-1;//끝페이지
        //endPage 는 totalPage를 넘을수 없다.
        if(endPage>totalPage) {
            endPage=totalPage;
        }

        //각 페이지에서 불러올
        startNum=(pageNum-1)*perPage; //mysql은 첫글이 0번(오라클은 1번이므로 +1해야한다)
        
        //각페이지의 글앞에 출력할 시작번호(예: 총글이 20개일 경우 1페이지는 20,2페이즈는 15..)
        no=totalCount-(pageNum-1)*perPage;
    	
        Map<String, Object> response = new LinkedHashMap<>();
        try {
        	response.put("userName", userName);
        	response.put("totalCount", totalCount);
        	response.put("totalPage", totalPage);
        	response.put("currentPage", pageNum);
        	response.put("startPage", startPage);
        	response.put("endPage", endPage);
        	response.put("no", no);
        	
            response.put("status", "ok");
            response.put("result", alarmService.unreadAlarm(userId, startNum,perPage));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("result", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    //알람 상태 읽음으로 변경
    @GetMapping("/updateIsRead")
    public ResponseEntity<Object> updateIsRead(@RequestParam(value = "id") List<Integer> ids) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
        	alarmService.updateIsRead(ids);// void 메서드 실행
            response.put("status", "ok");
            response.put("result", "알람 상태가 업데이트되었습니다.");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("result", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    
}