package data.controller;

import java.io.Console;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.util.JSONPObject;

import data.dto.SchedulesDto;
import data.service.SchedulesService;
import data.service.UsersService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class SchedulesController {
	final SchedulesService schedulesService;
	final UsersService userService;
	
	//일정관리 페이지 진입
	@GetMapping({"/schedules"})
	//@ResponseBody
	public String scheduleMain(Model model) {
		List<SchedulesDto> list = schedulesService.readAllSche();
	    model.addAttribute("scheduleList", list); // 일정 리스트 모델에 담기
	    model.addAttribute("today",new Date());
	    return "schedules/schedules"; // schedules.html 읽어오기
	}
	
	//일정등록
	@PostMapping("/scheduleinsert")
	@ResponseBody
	public ResponseEntity<Object> scheduleInsert(
			@RequestBody SchedulesDto dto
			)
	{
		Map<String, Object> response = new LinkedHashMap<>();
		try {
			String startTimeStr = dto.getStartDate() + " " + dto.getStartTime();
	        String endTimeStr = dto.getEndDate() + " " + dto.getEndTime();

	        // 초가 포함되어 있지 않으면 ":00" 추가
	        if (!startTimeStr.matches(".*:\\d{2}:\\d{2}$")) {
	            startTimeStr += ":00";
	        }
	        if (!endTimeStr.matches(".*:\\d{2}:\\d{2}$")) {
	            endTimeStr += ":00";
	        }

	        Timestamp startTimestamp = Timestamp.valueOf(startTimeStr);
	        Timestamp endTimestamp = Timestamp.valueOf(endTimeStr);

	        Map<String, Object> map = new HashMap<>();
	        map.put("userId", dto.getUserId());
	        map.put("name", dto.getName());
	        map.put("content", dto.getContent());
	        map.put("sort", dto.getSort());
	        map.put("isAlltime", dto.getIsAlltime());
	        map.put("startTime", startTimestamp);
	        map.put("endTime", endTimestamp);
	        map.put("startDate", dto.getStartDate());
	        map.put("endDate", dto.getEndDate());

	        schedulesService.scheduleInsert(map);
	        //return "일정 등록 완료";
	        
	        response.put("status", "ok");
            response.put("result", map);
            return new ResponseEntity<>(response, HttpStatus.OK);
	    } catch (Exception e) {
	    	response.put("status", "error");
            response.put("result", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	//전체 일정 조회
	@GetMapping("/schedulelist")
	@ResponseBody
	public ResponseEntity<List<SchedulesDto>> getAllSchedules() {
	    List<SchedulesDto> schedules = schedulesService.readAllSche();
	    return new ResponseEntity<>(schedules, HttpStatus.OK);
	}
	
	//일정 상세
	@GetMapping("/scheDetail")
	public String Detail(@RequestParam(value="id") int id,Model model)
	{
		SchedulesDto dto=schedulesService.readOneSche(id);
		
		//System.out.println("isAlltime from DB: " + dto.getIsAlltime());
		
		//시작 날짜 및 시간 분리
		String[] startDateParts = dto.getStartTime().split(" ");
	    String startDate = startDateParts[0]; //2025-03-31 시작날짜
		String startTime = startDateParts[1];//15:52 시작 시간
	    
		//종료 날짜 및 시간 분리
		String[] endDateParts = dto.getEndTime().split(" ");
		String endDate=endDateParts[0];
		String endTime=endDateParts[1];
	    
		model.addAttribute("dto",dto);
		model.addAttribute("StartDate", startDate); //시작날짜
		model.addAttribute("StartTime",startTime); //시작 시간
		model.addAttribute("endDate",endDate);//마감 날짜
		model.addAttribute("endTime",endTime);//마감 시간
		return "schedules/schedetail";
	}
	
	//일정 삭제
	@GetMapping("/scheDelete")
	public ResponseEntity<Void> deleteSchedules(@RequestParam(value="id") int id) {
	    schedulesService.deleteSche(id);
	    return new ResponseEntity<>(HttpStatus.OK);
	}
	
	//일정 수정
	@PostMapping("/scheUpdate")
	public String scheduleUpdate(
			@ModelAttribute SchedulesDto dto
			)
	{
			//기존 저장된 날짜와 시간 불러오기
			String startDate=dto.getStartDate();
			String startTime=dto.getStartTime();
			String endDate=dto.getEndDate();
			String endTime=dto.getEndTime();
			
			String startTimeStr = startDate + " " + startTime;
	        String endTimeStr = endDate + " " + endTime;

	        // 초가 포함되어 있지 않으면 ":00" 추가
	        if (!startTimeStr.matches(".*:\\d{2}:\\d{2}$")) {
	            startTimeStr += ":00";
	        }
	        if (!endTimeStr.matches(".*:\\d{2}:\\d{2}$")) {
	            endTimeStr += ":00";
	        }
	        Timestamp startTimestamp = Timestamp.valueOf(startTimeStr);
	        Timestamp endTimestamp = Timestamp.valueOf(endTimeStr);
	        
	        // 필요시 dto에 다시 설정 가능
	        dto.setStartTime(startTimeStr);
	        dto.setEndTime(endTimeStr);
	        
			schedulesService.updateSchedule(dto);
			
			return "redirect:./scheDetail?id="+dto.getId();
	}
	
}
