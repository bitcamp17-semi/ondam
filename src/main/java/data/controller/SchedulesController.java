package data.controller;

import java.io.Console;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.mysql.cj.xdevapi.JsonArray;

import data.dto.SchedulesDto;
import data.service.SchedulesService;
import data.service.UsersService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class SchedulesController {
	final SchedulesService schedulesService;
	final UsersService userService;
	
	//일정관리 페이지 진입
	@GetMapping({"/schedules"})
	//@ResponseBody
	public String scheduleMain(ModelMap model) {
		List<SchedulesDto> list = schedulesService.readAllSche();
	    model.addAttribute("scheduleList", list); // 일정 리스트 모델에 담기
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
	        map.put("isAlltime", dto.isAlltime() ? 1 : 0);
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
	
	
}
